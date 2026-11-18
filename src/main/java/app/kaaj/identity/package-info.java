/**
 * Identity module: users, roles, sessions and the audit log.
 * <p>
 * Member-3 owns the parts built here in the shared setup: the roles, the first admin account (S6) and the
 * audit log. The simple email-and-password sign-in in {@code internal.AuthController} is a stand-in so the
 * features can be built and demonstrated; Member-1's Google sign-in (S4) and Member-2's email codes (S5)
 * replace it, using the same session and the same {@link app.kaaj.identity.CurrentUser} API.
 */
package app.kaaj.identity;
