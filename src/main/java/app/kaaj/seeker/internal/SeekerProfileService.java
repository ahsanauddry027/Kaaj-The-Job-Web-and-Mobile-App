package app.kaaj.seeker.internal;

import app.kaaj.seeker.SeekerDirectory;
import app.kaaj.seeker.SeekerSummary;
import app.kaaj.shared.DocumentCode;
import app.kaaj.shared.KaajException;
import app.kaaj.shared.Taxonomy;
import java.time.Clock;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Saves and reads job seeker profiles; other modules use it as {@link SeekerDirectory}. */
@Service
class SeekerProfileService implements SeekerDirectory {

    /** The shifts a job seeker can be available for (the job post's shifts plus part-time). */
    static final Set<String> SHIFTS = Set.of("DAY", "NIGHT", "ROTATING", "PART_TIME", "FLEXIBLE");

    private final SeekerProfileRepository profiles;
    private final Taxonomy taxonomy;
    private final Clock clock;

    SeekerProfileService(SeekerProfileRepository profiles, Taxonomy taxonomy, Clock clock) {
        this.profiles = profiles;
        this.taxonomy = taxonomy;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Optional<SeekerProfile> get(UUID userId) {
        return profiles.findById(userId);
    }

    /** Creates the profile the first time, then updates it. Unknown occupations, skills or codes are refused. */
    @Transactional
    public SeekerProfile save(UUID userId, SeekerProfile.Details details) {
        check(details);
        return profiles.findById(userId)
                .map(profile -> {
                    profile.update(details, clock.instant());
                    return profile;
                })
                .orElseGet(() -> profiles.save(SeekerProfile.create(userId, details, clock.instant())));
    }

    private void check(SeekerProfile.Details details) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (UUID id : details.occupationIds()) {
            if (taxonomy.occupation(id).isEmpty()) {
                errors.put("occupationIds", "seeker.occupation.unknown");
            }
        }
        if (taxonomy.skills(details.skillIds()).size() != details.skillIds().stream().distinct().count()) {
            errors.put("skillIds", "seeker.skill.unknown");
        }
        if (!details.documents().stream().allMatch(DocumentCode::isValid)) {
            errors.put("documents", "seeker.document.unknown");
        }
        if (!SHIFTS.containsAll(details.shifts())) {
            errors.put("shifts", "seeker.shift.unknown");
        }
        if ((details.homeLatitude() == null) != (details.homeLongitude() == null)) {
            errors.put("homeLatitude", "seeker.home.incomplete");
        }
        if ((details.expectedWage() == null) != (details.wagePeriod() == null)) {
            errors.put("expectedWage", "seeker.wage.incomplete");
        }
        if (!errors.isEmpty()) {
            throw KaajException.invalid(errors);
        }
    }

    // ---------------------------------------------------------------- SeekerDirectory

    @Override
    @Transactional(readOnly = true)
    public Optional<SeekerSummary> find(UUID userId) {
        return profiles.findById(userId).map(SeekerProfile::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, SeekerSummary> findAll(Collection<UUID> userIds) {
        List<SeekerProfile> found = profiles.findAllById(userIds);
        return found.stream().map(SeekerProfile::toSummary)
                .collect(Collectors.toMap(SeekerSummary::userId, Function.identity()));
    }
}
