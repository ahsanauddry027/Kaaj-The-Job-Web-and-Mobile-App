package app.kaaj.trust.internal;

import app.kaaj.job.Finding;
import app.kaaj.job.JobStatus;
import app.kaaj.shared.WagePeriod;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** The JSON shapes of the trust API. */
final class TrustViews {

    private TrustViews() {
    }

    /**
     * {@code POST /api/v1/reports}.
     *
     * @param subjectType {@code JOB} (messages and employers come with Member-2's chat)
     * @param reason      {@code ASKED_FOR_MONEY}, {@code FAKE_JOB}, {@code HARASSMENT} or {@code OTHER}
     * @param details     optional, up to 500 characters
     */
    record ReportRequest(String subjectType, UUID subjectId, String reason, String details) {
    }

    /** {@code REMOVE_POST}, {@code SUSPEND_EMPLOYER} (both need a note) or {@code DISMISS}. */
    record DecisionRequest(String decision, String note) {
    }

    /** A row in the admin queue. {@code codes}: the BLOCK and REVIEW findings, for the badges. */
    record CaseSummary(
            UUID id, String subjectType, UUID subjectId, String source, String screening, int reportCount,
            String status, String decision, String jobTitleBn, String jobTitleEn, String employerName,
            JobStatus jobStatus, List<String> codes, Instant createdAt, Instant decidedAt) {
    }

    /** One case with everything the moderator needs: the post, the employer, the findings, the AI check, the reports. */
    record CaseDetail(
            UUID id, String subjectType, UUID subjectId, String source, String screening, int reportCount,
            String status, String decision, String note, Instant createdAt, Instant decidedAt,
            List<Finding> findings, ScamAssessment aiAssessment, JobBrief job, EmployerBrief employer,
            List<ReportView> reports) {
    }

    record JobBrief(
            UUID id, String titleBn, String titleEn, String descriptionBn, String descriptionEn,
            Integer wageMin, Integer wageMax, WagePeriod wagePeriod, String hours, String areaName,
            JobStatus status, String statusReason) {
    }

    /** {@code ownerActive}: false once the employer's account is suspended. */
    record EmployerBrief(UUID id, String name, boolean verified, String ownerEmail, boolean ownerActive) {
    }

    /** A report as the moderator sees it. Who sent it is not shown. */
    record ReportView(String reason, String details, Instant createdAt) {
    }

    /**
     * What the employer sees about their own post: the newest case's findings, and the outcome once decided.
     * {@code caseStatus} is null when the post was never flagged or reported.
     */
    record ScreeningView(String caseStatus, String screening, List<Finding> findings, String decision, String note,
                         boolean reported) {
    }
}
