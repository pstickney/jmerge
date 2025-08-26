package io.github.pstickney.jmerge;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YamlMergerTest {

    private final YAMLMapper mapper = new YAMLMapper();

    static Stream<Scenario> scenarios() throws IOException {
        return Scenario.discover("/scenarios", "yaml");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void testScenarios(Scenario scenario) throws IOException {
        MergeConfig config = scenario.getConfig() == null ? new MergeConfig() : mapper.readValue(scenario.getConfig(), MergeConfig.class);
        YamlMerger merger = new YamlMerger(config);
        String merged = merger.merge(scenario.getBase().trim(), scenario.getOverlay().trim());

        assertEquals(scenario.getExpected().trim(), merged.trim(), "Failed on " + scenario.getFolder());
    }
}
