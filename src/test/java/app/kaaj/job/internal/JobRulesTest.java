package app.kaaj.job.internal;

import static org.assertj.core.api.Assertions.assertThat;

import app.kaaj.job.JobType;
import app.kaaj.job.Shift;
import app.kaaj.shared.WagePeriod;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JobRulesTest {

    static JobDetails complete() {
        return new JobDetails("পিকআপ ড্রাইভার", "Pickup driver", "টঙ্গীতে পিকআপ চালাতে হবে", null,
                UUID.randomUUID(), JobType.FULL_TIME, Shift.DAY, "08:00-18:00", 2, 16_000, 20_000, WagePeriod.MONTH,
                23.89, 90.40, "Tongi, Gazipur", false, 2, List.of(), List.of("DRIVING_PRO_LIGHT"));
    }

    static JobDetails with(JobDetails d, Integer wageMin, Integer wageMax, WagePeriod period,
                           Double lat, Double lng, Boolean remote, String hours) {
        return new JobDetails(d.titleBn(), d.titleEn(), d.descriptionBn(), d.descriptionEn(), d.occupationId(),
                d.jobType(), d.shift(), hours, d.openings(), wageMin, wageMax, period, lat, lng, d.areaName(), remote,
                d.minExperienceYears(), d.skillIds(), d.requiredDocuments());
    }

    @Test
    void aCompletePostCanBeSubmitted() {
        assertThat(JobRules.checkSubmit(complete())).isEmpty();
    }

    @Test
    void theWageIsAlwaysRequired() {
        JobDetails noWage = with(complete(), null, null, null, 23.89, 90.40, false, "08:00-18:00");
        assertThat(JobRules.checkSubmit(noWage))
                .containsEntry("wageMin", "job.wage.required")
                .containsEntry("wageMax", "job.wage.required")
                .containsEntry("wagePeriod", "job.wage.period.required");
        assertThat(JobRules.checkDraft(noWage)).isEmpty(); // a draft may leave it out for now
    }

    @Test
    void theMinimumWageCannotBeAboveTheMaximum() {
        JobDetails upsideDown = with(complete(), 20_000, 16_000, WagePeriod.MONTH, 23.89, 90.40, false, "08:00-18:00");
        assertThat(JobRules.checkDraft(upsideDown)).containsEntry("wageMax", "job.wage.range");
    }

    @Test
    void aPostNeedsAMapPinUnlessItIsRemote() {
        JobDetails noPin = with(complete(), 16_000, 20_000, WagePeriod.MONTH, null, null, false, "08:00-18:00");
        assertThat(JobRules.checkSubmit(noPin)).containsEntry("latitude", "job.location.required");
        JobDetails remote = with(complete(), 16_000, 20_000, WagePeriod.MONTH, null, null, true, "08:00-18:00");
        assertThat(JobRules.checkSubmit(remote)).isEmpty();
    }

    @Test
    void workingHoursUseTheFormat0800to1800() {
        assertThat(JobRules.checkSubmit(with(complete(), 16_000, 20_000, WagePeriod.MONTH, 23.89, 90.40, false, "8am-6pm")))
                .containsEntry("hours", "job.hours.invalid");
        assertThat(JobRules.checkSubmit(with(complete(), 16_000, 20_000, WagePeriod.MONTH, 23.89, 90.40, false, null)))
                .containsEntry("hours", "job.hours.required");
        assertThat(JobRules.checkSubmit(with(complete(), 16_000, 20_000, WagePeriod.MONTH, 23.89, 90.40, false, "20:00-06:00")))
                .isEmpty(); // a night shift crosses midnight
    }

    @Test
    void aDraftNeedsOnlyATitle() {
        JobDetails titleOnly = new JobDetails("লোডার", null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
        assertThat(JobRules.checkDraft(titleOnly)).isEmpty();
        assertThat(JobRules.checkSubmit(titleOnly)).containsKeys("descriptionBn", "occupationId", "wageMin", "latitude", "hours");

        JobDetails noTitle = new JobDetails(" ", "", null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
        assertThat(JobRules.checkDraft(noTitle)).containsEntry("titleBn", "job.title.required");
    }

    @Test
    void requiredDocumentsMustBeKnownCodes() {
        JobDetails d = complete();
        JobDetails unknown = new JobDetails(d.titleBn(), d.titleEn(), d.descriptionBn(), d.descriptionEn(),
                d.occupationId(), d.jobType(), d.shift(), d.hours(), d.openings(), d.wageMin(), d.wageMax(),
                d.wagePeriod(), d.latitude(), d.longitude(), d.areaName(), d.remote(), d.minExperienceYears(),
                d.skillIds(), List.of("Driving licence"));
        assertThat(JobRules.checkSubmit(unknown)).containsEntry("requiredDocuments", "job.document.unknown");
    }
}
