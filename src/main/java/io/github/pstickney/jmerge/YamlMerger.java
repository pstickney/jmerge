package io.github.pstickney.jmerge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class YamlMerger extends Merger {

    protected YAMLMapper mapper;

    public YamlMerger() {
        super();
        mapper = new YAMLMapper();
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

        ObjectWriter writer = config.prettyPrint() ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();
        return writer.writeValueAsString(node);
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
