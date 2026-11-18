package app.kaaj.shared;

import java.util.List;

/** One page of a list; {@code nextCursor} is null on the last page. */
public record CursorPage<T>(List<T> items, String nextCursor) {
}
