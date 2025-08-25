package io.github.pstickney.jmerge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonMerger extends Merger {

    protected ObjectMapper mapper;

    public JsonMerger() {
        super();
        mapper = new ObjectMapper();
    }

    /**
     * Merges two JSON strings and returns the merged result as a JSON string.
     *
     * @param base    the base JSON string
     * @param overlay the overlay JSON string to merge into the base
     * @return the merged JSON as a string
     * @throws JsonProcessingException if the input strings cannot be parsed as JSON
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
     * Provides a new Jackson {@link ObjectNode} instance for representing JSON objects.
     *
     * @return a new {@link ObjectNode}
     */
    @Override
    protected ObjectNode getObjectNode() {
        return mapper.createObjectNode();
    }

    /**
     * Provides a new Jackson {@link ArrayNode} instance for representing JSON arrays.
     *
     * @return a new {@link ArrayNode}
     */
    @Override
    protected ArrayNode getArrayNode() {
        return mapper.createArrayNode();
    }
}
