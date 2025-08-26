package io.github.pstickney.jmerge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonMergerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    static Stream<Scenario> scenarios() throws IOException {
        return Scenario.discover("/scenarios", "json");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void testScenarios(Scenario scenario) throws IOException {
        MergeConfig config = scenario.getConfig() == null ? new MergeConfig() : mapper.readValue(scenario.getConfig(), MergeConfig.class);
        JsonMerger merger = new JsonMerger(config);
        String merged = merger.merge(scenario.getBase().trim(), scenario.getOverlay().trim());

        assertEquals(scenario.getExpected().trim(), merged.trim(), "Failed on " + scenario.getFolder());
    }
}
