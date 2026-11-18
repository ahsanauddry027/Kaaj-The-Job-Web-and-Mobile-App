package app.kaaj.shared;

/**
 * One rule for comparing wages paid by the hour, day, week or month (context file decision 23):
 * 26 working days of 8 hours a month. Hour × 208, day × 26, week × 52 ÷ 12 (rounded), month × 1.
 * Piece rates have no monthly equivalent.
 */
public final class Wages {

    private Wages() {
    }

    /** The monthly equivalent in whole taka, or {@code null} for piece rates or a missing amount. */
    public static Integer monthly(Integer amount, WagePeriod period) {
        if (amount == null || period == null) {
            return null;
        }
        return switch (period) {
            case HOUR -> Math.multiplyExact(amount, 208);
            case DAY -> Math.multiplyExact(amount, 26);
            case WEEK -> (int) Math.round(amount * 52 / 12.0);
            case MONTH -> amount;
            case PIECE -> null;
        };
    }
}
