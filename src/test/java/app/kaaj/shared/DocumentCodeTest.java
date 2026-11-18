package app.kaaj.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentCodeTest {

    @Test
    void acceptsDrivingLicenceClassesAndSkillCertificates() {
        UUID skill = UUID.randomUUID();
        assertThat(DocumentCode.isValid("DRIVING_PRO_LIGHT")).isTrue();
        assertThat(DocumentCode.isValid("CERT:" + skill)).isTrue();
        assertThat(DocumentCode.certificate(skill).certificateSkill()).contains(skill);
    }

    @Test
    void refusesUnknownCodes() {
        assertThat(DocumentCode.isValid("PASSPORT")).isFalse();
        assertThat(DocumentCode.isValid("CERT:not-a-uuid")).isFalse();
        assertThat(DocumentCode.isValid(null)).isFalse();
        assertThatThrownBy(() -> new DocumentCode("Driving licence")).isInstanceOf(IllegalArgumentException.class);
    }
}
