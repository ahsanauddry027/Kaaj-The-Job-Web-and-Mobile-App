package app.kaaj.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WagesTest {

    @Test
    void convertsEveryPeriodToAMonthlyEquivalent() {
        assertThat(Wages.monthly(100, WagePeriod.HOUR)).isEqualTo(20_800);   // × 208
        assertThat(Wages.monthly(700, WagePeriod.DAY)).isEqualTo(18_200);    // × 26
        assertThat(Wages.monthly(4_000, WagePeriod.WEEK)).isEqualTo(17_333); // × 52 ÷ 12, rounded
        assertThat(Wages.monthly(18_000, WagePeriod.MONTH)).isEqualTo(18_000);
    }

    @Test
    void pieceRatesAndMissingAmountsHaveNoMonthlyEquivalent() {
        assertThat(Wages.monthly(15, WagePeriod.PIECE)).isNull();
        assertThat(Wages.monthly(null, WagePeriod.MONTH)).isNull();
        assertThat(Wages.monthly(18_000, null)).isNull();
    }
}
