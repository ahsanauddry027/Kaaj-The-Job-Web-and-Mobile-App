package app.kaaj.job;

/**
 * The job post status flow, kept in one place:
 * <pre>
 *   DRAFT ──submit──▶ IN_REVIEW ──screening or auto-publish──▶ PUBLISHED ◀──resume── PAUSED
 *                         │                                       │   └──pause──────────▲
 *                         └──▶ BLOCKED (scam screening)            ├──edit, or reported──▶ IN_REVIEW
 *                                                                  └──close / 30 days──▶ CLOSED ──repost──▶ new DRAFT
 * </pre>
 * Moderators may also block a published or paused post (removed as a scam), and restore a blocked post
 * when they dismiss its case (every block can be reviewed by a person, context file §10.7).
 */
public enum JobStatus {
    DRAFT, IN_REVIEW, PUBLISHED, PAUSED, CLOSED, BLOCKED;

    public boolean canMoveTo(JobStatus next) {
        return switch (this) {
            case DRAFT -> next == IN_REVIEW;
            case IN_REVIEW -> next == PUBLISHED || next == BLOCKED || next == CLOSED;
            case PUBLISHED -> next == PAUSED || next == CLOSED || next == IN_REVIEW || next == BLOCKED;
            case PAUSED -> next == PUBLISHED || next == CLOSED || next == IN_REVIEW || next == BLOCKED;
            case BLOCKED -> next == PUBLISHED || next == CLOSED;
            case CLOSED -> false;
        };
    }
}
