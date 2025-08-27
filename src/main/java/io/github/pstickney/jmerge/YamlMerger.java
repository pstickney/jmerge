package io.github.pstickney.jmerge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class YamlMerger extends Merger {

    protected YAMLMapper mapper;

    /**
     * Constructs a Merger with the default merge configuration.
     */
    public YamlMerger() {
        super();
        mapper = new YAMLMapper();
        mapper.enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS);
    }

    /**
     * Constructs a Merger with the specified merge configuration.
     *
     * @param config the merge configuration to apply during merging
     */
    public YamlMerger(MergeConfig config) {
        super(config);
        mapper = new YAMLMapper();
        mapper.enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS);
    }

    /**
     * Merges two YAML strings and returns the merged result as a YAML string.
     *
     * @param base    the base YAML string
     * @param overlay the overlay YAML string to merge into the base
     * @return the merged YAML as a string
     * @throws JsonProcessingException if the input strings cannot be parsed as YAML
     */
    @Override
    public String merge(String base, String overlay) throws JsonProcessingException {
        JsonNode baseNode = mapper.readTree(base);
        JsonNode overlayNode = mapper.readTree(overlay);

        JsonNode node = merge(baseNode, overlayNode);

        ObjectWriter writer = config.getPrettyPrint() ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();
        return writer.writeValueAsString(node);
    }

    /**
     * Merges two objects of type T according to the configured strategies.
     *
     * @param base        the base object
     * @param overlay     the overlay object
     * @param outputClass the class of the object
     * @param <T>
     * @return the merged object
     * @throws JsonProcessingException if parsing or processing fails
     */
    @Override
    public <T> T merge(T base, T overlay, Class<T> outputClass) throws JsonProcessingException {
        String baseString = mapper.writeValueAsString(base);
        String overlayString = mapper.writeValueAsString(overlay);

        String merged = merge(baseString, overlayString);

        return mapper.readValue(merged, outputClass);
    }

    /**
     * Provides a new Jackson {@link ObjectNode} instance for representing YAML objects.
     *
     * @return a new {@link ObjectNode}
     */
    @Override
    protected ObjectNode getObjectNode() {
        return mapper.createObjectNode();
    }

    /**
     * Provides a new Jackson {@link ArrayNode} instance for representing YAML arrays.
     *
     * @return a new {@link ArrayNode}
     */
    @Override
    protected ArrayNode getArrayNode() {
        return mapper.createArrayNode();
    }
}
