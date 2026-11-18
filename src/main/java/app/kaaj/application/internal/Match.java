package app.kaaj.application.internal;

import app.kaaj.job.JobSummary;
import app.kaaj.seeker.SeekerSummary;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * How well a job seeker fits a post, with the reasons and the gaps — the same shape the AI ranking (M3-2)
 * will return, but computed with plain, explainable rules so the employer can see why:
 * <ul>
 *   <li>occupation is the post's category: 0.35</li>
 *   <li>share of the asked skills the person has: up to 0.25</li>
 *   <li>enough years of experience (or none asked): 0.15</li>
 *   <li>holds every required document: 0.15</li>
 *   <li>lives within their travel radius of the job, or the job is remote: 0.10</li>
 * </ul>
 */
record Match(double score, List<Reason> reasons, List<Reason> gaps, Double distanceKm) {

    /** A reason or a gap: a code the app translates, with numbers to fill in ("{km} km from home"). */
    record Reason(String code, Map<String, Object> values) {
        static Reason of(String code) {
            return new Reason(code, Map.of());
        }

        static Reason of(String code, String key, Object value) {
            return new Reason(code, Map.of(key, value));
        }
    }

    static Match of(JobSummary job, SeekerSummary seeker) {
        double score = 0;
        List<Reason> reasons = new ArrayList<>();
        List<Reason> gaps = new ArrayList<>();

        if (job.occupationId() != null && seeker.occupationIds().contains(job.occupationId())) {
            score += 0.35;
            reasons.add(Reason.of("OCCUPATION_MATCH"));
        } else {
            gaps.add(Reason.of("OTHER_OCCUPATION"));
        }

        List<UUID> asked = job.skillIds();
        if (asked.isEmpty()) {
            score += 0.25;
        } else {
            Set<UUID> has = new HashSet<>(seeker.skillIds());
            long matched = asked.stream().filter(has::contains).count();
            score += 0.25 * matched / asked.size();
            if (matched > 0) {
                Map<String, Object> values = new LinkedHashMap<>();
                values.put("matched", matched);
                values.put("asked", asked.size());
                reasons.add(new Reason("SKILLS_MATCH", values));
            }
            if (matched < asked.size()) {
                gaps.add(Reason.of("SKILLS_MISSING", "count", asked.size() - matched));
            }
        }

        Integer years = job.minExperienceYears();
        if (years == null || years == 0) {
            score += 0.15;
        } else if (seeker.yearsExperience() >= years) {
            score += 0.15;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("years", seeker.yearsExperience());
            values.put("asked", years);
            reasons.add(new Reason("EXPERIENCE_OK", values));
        } else {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("years", seeker.yearsExperience());
            values.put("asked", years);
            gaps.add(new Reason("EXPERIENCE_SHORT", values));
        }

        List<String> documents = job.requiredDocuments();
        if (documents.isEmpty()) {
            score += 0.15;
        } else {
            List<String> missing = documents.stream().filter(code -> !seeker.documents().contains(code)).toList();
            if (missing.isEmpty()) {
                score += 0.15;
                reasons.add(Reason.of("DOCUMENTS_OK"));
            } else {
                gaps.add(Reason.of("DOCUMENTS_MISSING", "codes", missing));
            }
        }

        Double distance = null;
        if (job.remote()) {
            score += 0.10;
            reasons.add(Reason.of("REMOTE"));
        } else if (job.hasLocation() && seeker.hasHome()) {
            distance = Math.round(km(job.latitude(), job.longitude(), seeker.homeLatitude(), seeker.homeLongitude()) * 10) / 10.0;
            if (distance <= seeker.travelRadiusKm()) {
                score += 0.10;
                reasons.add(Reason.of("NEAR_HOME", "km", distance));
            } else {
                gaps.add(Reason.of("FAR_FROM_HOME", "km", distance));
            }
        }

        double rounded = Math.round(Math.min(1.0, score) * 100) / 100.0;
        return new Match(rounded, List.copyOf(reasons), List.copyOf(gaps), distance);
    }

    /** Great-circle distance in km (haversine), the same formula as the search SQL. */
    static double km(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLng / 2), 2);
        return 6371 * 2 * Math.asin(Math.sqrt(a));
    }
}
