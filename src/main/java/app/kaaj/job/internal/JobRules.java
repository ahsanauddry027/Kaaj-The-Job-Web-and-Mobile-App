package app.kaaj.job.internal;

import app.kaaj.shared.DocumentCode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The posting rules, in plain Java so they are easy to test (context file decision 12: business rules in
 * code). Each rule answers with field name → message code; the web app shows the message by the field.
 * <ul>
 *   <li>A draft needs only a title, but whatever is filled in must be valid.</li>
 *   <li>A submitted post also needs a description, a category, a wage range with its period (wages are
 *       always shown up front), a location or "remote", and working hours.</li>
 * </ul>
 */
final class JobRules {

    static final Pattern HOURS = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d-([01]\\d|2[0-3]):[0-5]\\d$");
    static final int MAX_WAGE = 10_000_000;

    private JobRules() {
    }

    static Map<String, String> checkDraft(JobDetails d) {
        Map<String, String> errors = new LinkedHashMap<>();
        String title = firstNonBlank(d.titleBn(), d.titleEn());
        if (title == null) {
            errors.put("titleBn", "job.title.required");
        } else if (title.strip().length() < 2) {
            errors.put("titleBn", "job.title.length");
        }
        if (d.openings() != null && (d.openings() < 1 || d.openings() > 1000)) {
            errors.put("openings", "job.openings.range");
        }
        if (d.wageMin() != null && (d.wageMin() <= 0 || d.wageMin() > MAX_WAGE)) {
            errors.put("wageMin", "job.wage.positive");
        }
        if (d.wageMax() != null && (d.wageMax() <= 0 || d.wageMax() > MAX_WAGE)) {
            errors.put("wageMax", "job.wage.positive");
        }
        if (d.wageMin() != null && d.wageMax() != null && d.wageMin() > d.wageMax()) {
            errors.put("wageMax", "job.wage.range");
        }
        if (d.hours() != null && !d.hours().isBlank() && !HOURS.matcher(d.hours().strip()).matches()) {
            errors.put("hours", "job.hours.invalid");
        }
        if (d.minExperienceYears() != null && (d.minExperienceYears() < 0 || d.minExperienceYears() > 50)) {
            errors.put("minExperienceYears", "job.experience.range");
        }
        if ((d.latitude() == null) != (d.longitude() == null)) {
            errors.put("latitude", "job.location.incomplete");
        }
        if (d.requiredDocuments() != null && d.requiredDocuments().stream().anyMatch(code -> !DocumentCode.isValid(code))) {
            errors.put("requiredDocuments", "job.document.unknown");
        }
        return errors;
    }

    static Map<String, String> checkSubmit(JobDetails d) {
        Map<String, String> errors = checkDraft(d);
        if (firstNonBlank(d.descriptionBn(), d.descriptionEn()) == null) {
            errors.putIfAbsent("descriptionBn", "job.description.required");
        }
        if (d.occupationId() == null) {
            errors.putIfAbsent("occupationId", "job.category.required");
        }
        if (d.wageMin() == null) {
            errors.putIfAbsent("wageMin", "job.wage.required");
        }
        if (d.wageMax() == null) {
            errors.putIfAbsent("wageMax", "job.wage.required");
        }
        if (d.wagePeriod() == null) {
            errors.putIfAbsent("wagePeriod", "job.wage.period.required");
        }
        if (!Boolean.TRUE.equals(d.remote()) && (d.latitude() == null || d.longitude() == null)) {
            errors.putIfAbsent("latitude", "job.location.required");
        }
        if (d.hours() == null || d.hours().isBlank()) {
            errors.putIfAbsent("hours", "job.hours.required");
        }
        return errors;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return b != null && !b.isBlank() ? b : null;
    }
}
