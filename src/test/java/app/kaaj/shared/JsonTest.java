package app.kaaj.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    void writesAFlatObjectWithEscapedText() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("name", "Karim \"Transport\"\n");
        values.put("score", 0.8);
        values.put("note", null);
        assertThat(Json.object(values)).isEqualTo("{\"name\":\"Karim \\\"Transport\\\"\\n\",\"score\":0.8,\"note\":null}");
    }
}
