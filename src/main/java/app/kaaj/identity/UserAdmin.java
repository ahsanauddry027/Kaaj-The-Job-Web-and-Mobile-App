package app.kaaj.identity;

import java.util.UUID;

/**
 * Suspending and reactivating accounts, for moderation (scam screening suspends a scam employer). Every
 * change is written to the audit log. A suspended user can't sign in, and their open session stops working
 * on their next request. (The full user-management page, with role changes, belongs to Member-1's sign-up
 * feature in the final requirements.)
 */
public interface UserAdmin {

    /**
     * @throws app.kaaj.shared.KaajException 400 {@code user.suspend-self} for the acting admin's own account,
     *                                       400 {@code user.suspend-admin} for another admin
     */
    void suspend(UUID userId, UUID actorId, String reason);

    void reactivate(UUID userId, UUID actorId, String reason);
}
