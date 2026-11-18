package app.kaaj.job.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.kaaj.shared.KaajException;
import org.junit.jupiter.api.Test;

/** The parts of job search that don't need a database: the page cursor and LIKE escaping. */
class JobSearchTest {

    @Test
    void theCursorRoundTripsAndRejectsJunk() {
        assertThat(JobSearch.decode(null)).isZero();
        assertThat(JobSearch.decode(JobSearch.encode(40))).isEqualTo(40);
        assertThatThrownBy(() -> JobSearch.decode("not-a-cursor")).isInstanceOf(KaajException.class);
        assertThatThrownBy(() -> JobSearch.decode(JobSearch.encode(99_999))).isInstanceOf(KaajException.class);
    }

    @Test
    void percentAndUnderscoreAreSearchedLiterally() {
        assertThat(JobSearch.escapeLike("50%_off")).isEqualTo("50\\%\\_off");
    }
}
