package app.kaaj.identity.internal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Whether a signed-in user is still active, checked on every request but read from the database at most
 * every 30 seconds per user. Suspending a user clears their entry, so it takes effect on the next request.
 */
@Component
class UserStatusCache {

    private static final Duration KEEP_FOR = Duration.ofSeconds(30);

    private record Entry(boolean active, Instant checkedAt) {
    }

    private final AppUserRepository users;
    private final Clock clock;
    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();

    UserStatusCache(AppUserRepository users, Clock clock) {
        this.users = users;
        this.clock = clock;
    }

    boolean isActive(UUID userId) {
        Instant now = clock.instant();
        Entry entry = entries.get(userId);
        if (entry == null || Duration.between(entry.checkedAt(), now).compareTo(KEEP_FOR) > 0) {
            boolean active = users.findById(userId).map(user -> user.getStatus() == AppUser.Status.ACTIVE).orElse(false);
            entry = new Entry(active, now);
            entries.put(userId, entry);
        }
        return entry.active();
    }

    void forget(UUID userId) {
        entries.remove(userId);
    }
}
