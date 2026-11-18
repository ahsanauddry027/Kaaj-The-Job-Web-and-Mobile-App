package app.kaaj.application.internal;

import app.kaaj.job.JobDirectory;
import app.kaaj.job.JobSummary;
import app.kaaj.seeker.SeekerSummary;
import app.kaaj.shared.Taxonomy;
import app.kaaj.shared.Term;
import app.kaaj.shared.WagePeriod;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** The JSON shapes of the application API, for both sides. */
@Component
class ApplicationViews {

    /** The post, as the job seeker's "My applications" shows it. */
    record JobBrief(UUID id, String titleBn, String titleEn, String employerName, boolean employerVerified,
                    Integer wageMin, Integer wageMax, WagePeriod wagePeriod, String areaName, boolean remote,
                    boolean open) {
    }

    record TimelineEntry(ApplicationStatus fromStatus, ApplicationStatus toStatus, ApplicationStatus.Actor actor,
                         String note, Instant at) {
    }

    /** One of the job seeker's applications. {@code timeline} is filled on the detail page only. */
    record MyApplicationView(UUID id, ApplicationStatus status, String message, Instant appliedAt, Instant updatedAt,
                             Instant interviewAt, String interviewPlace, JobBrief job, boolean canWithdraw,
                             List<TimelineEntry> timeline) {
    }

    /** The job seeker as the employer sees them. The phone number appears only after shortlisting. */
    record SeekerBrief(String displayName, String areaName, int yearsExperience, String education,
                       List<Term> occupations, List<Term> skills, List<String> documents, Integer expectedWage,
                       WagePeriod wagePeriod, List<String> shifts, String phone) {
    }

    /** One applicant, with the match score, reasons and gaps (the ranked list and the detail page). */
    record ApplicantView(UUID applicationId, ApplicationStatus status, String message, Instant appliedAt,
                         Instant updatedAt, Instant interviewAt, String interviewPlace, SeekerBrief seeker,
                         double score, List<Match.Reason> reasons, List<Match.Reason> gaps, Double distanceKm,
                         Set<ApplicationStatus> nextStatuses, List<TimelineEntry> timeline) {
    }

    private final Taxonomy taxonomy;
    private final JobDirectory jobs;
    private final ApplicationEvents events;

    ApplicationViews(Taxonomy taxonomy, JobDirectory jobs, ApplicationEvents events) {
        this.taxonomy = taxonomy;
        this.jobs = jobs;
        this.events = events;
    }

    MyApplicationView mine(Application application, JobSummary job, boolean withTimeline, Instant now) {
        JobBrief brief = job == null ? null : new JobBrief(job.id(), job.titleBn(), job.titleEn(), job.employerName(),
                job.employerVerified(), job.wageMin(), job.wageMax(), job.wagePeriod(), job.areaName(), job.remote(),
                job.openForApplications(now));
        return new MyApplicationView(application.getId(), application.getStatus(), application.getMessage(),
                application.getCreatedAt(), application.getUpdatedAt(), application.getInterviewAt(),
                application.getInterviewPlace(), brief, application.getStatus().isOpen(),
                withTimeline ? timeline(application) : List.of());
    }

    MyApplicationView mine(Application application, boolean withTimeline, Instant now) {
        return mine(application, jobs.findById(application.getJobId()).orElse(null), withTimeline, now);
    }

    ApplicantView applicant(Application application, JobSummary job, SeekerSummary seeker, boolean withTimeline) {
        Match match = seeker == null ? new Match(0, List.of(), List.of(), null) : Match.of(job, seeker);
        SeekerBrief brief = seeker == null ? null : new SeekerBrief(seeker.displayName(), seeker.homeAreaName(),
                seeker.yearsExperience(), seeker.education(), occupations(seeker.occupationIds()),
                taxonomy.skills(seeker.skillIds()), seeker.documents(), seeker.expectedWage(), seeker.wagePeriod(),
                seeker.shifts(), application.getStatus().sharesPhone() ? seeker.phone() : null);
        return new ApplicantView(application.getId(), application.getStatus(), application.getMessage(),
                application.getCreatedAt(), application.getUpdatedAt(), application.getInterviewAt(),
                application.getInterviewPlace(), brief, match.score(), match.reasons(), match.gaps(),
                match.distanceKm(), application.getStatus().employerChoices(),
                withTimeline ? timeline(application) : List.of());
    }

    private List<Term> occupations(List<UUID> ids) {
        return ids.stream().map(taxonomy::occupation).flatMap(Optional::stream).toList();
    }

    private List<TimelineEntry> timeline(Application application) {
        return events.timeline(application.getId()).stream()
                .map(e -> new TimelineEntry(e.fromStatus(), e.toStatus(), e.actor(), e.note(), e.at()))
                .toList();
    }
}
