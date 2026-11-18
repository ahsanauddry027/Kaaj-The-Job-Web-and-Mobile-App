package app.kaaj.employer;

/**
 * The verification status flow, kept in one place:
 * <pre>
 *   UNVERIFIED ──send profile + document──▶ PENDING ──approve──▶ VERIFIED
 *                                             │  ▲                  │
 *                                     reject  ▼  │ fix and resend   │ business name or document changed
 *                                          REJECTED                 ▼
 *                                                                PENDING
 * </pre>
 */
public enum VerificationStatus {
    UNVERIFIED, PENDING, VERIFIED, REJECTED;

    public boolean canMoveTo(VerificationStatus next) {
        return switch (this) {
            case UNVERIFIED, REJECTED, VERIFIED -> next == PENDING;
            case PENDING -> next == VERIFIED || next == REJECTED;
        };
    }
}
