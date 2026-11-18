package app.kaaj.job;

/**
 * The job post status flow, kept in one place:
 * <pre>
 *   DRAFT ──submit──▶ IN_REVIEW ──screening or auto-publish──▶ PUBLISHED ◀──resume── PAUSED
 *                         │                                       │   └──pause──────────▲
 *                         └──▶ BLOCKED (scam screening, M3-6)     ├──edit──▶ IN_REVIEW
 *                                                                  └──close / 30 days──▶ CLOSED ──repost──▶ new DRAFT
 * </pre>
 */
public enum JobStatus {
    DRAFT, IN_REVIEW, PUBLISHED, PAUSED, CLOSED, BLOCKED;

    public boolean canMoveTo(JobStatus next) {
        return switch (this) {
            case DRAFT -> next == IN_REVIEW;
            case IN_REVIEW -> next == PUBLISHED || next == BLOCKED || next == CLOSED;
            case PUBLISHED -> next == PAUSED || next == CLOSED || next == IN_REVIEW;
            case PAUSED -> next == PUBLISHED || next == CLOSED || next == IN_REVIEW;
            case CLOSED, BLOCKED -> false;
        };
    }
}
