package app.kaaj.seeker.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * The profile completeness meter (M1-2): which of the key fields are filled, as a percentage, and the
 * names of the missing ones (the app translates them).
 */
record Completeness(int percent, List<String> missing) {

    /** The fields the meter counts, in the order the app lists what is missing. */
    static final List<String> FIELDS = List.of(
            "displayName", "occupations", "skills", "experience", "home", "expectedWage", "shifts");

    static Completeness of(SeekerProfile profile) {
        List<String> missing = new ArrayList<>();
        if (profile.getDisplayName() == null || profile.getDisplayName().isBlank()) missing.add("displayName");
        if (profile.getOccupationIds().isEmpty()) missing.add("occupations");
        if (profile.getSkillIds().isEmpty()) missing.add("skills");
        if (profile.getYearsExperience() == 0 && profile.getEducation() == null) missing.add("experience");
        if (profile.getHomeLatitude() == null) missing.add("home");
        if (profile.getExpectedWage() == null) missing.add("expectedWage");
        if (profile.getShifts().isEmpty()) missing.add("shifts");
        int filled = FIELDS.size() - missing.size();
        return new Completeness(Math.round(filled * 100f / FIELDS.size()), List.copyOf(missing));
    }
}
