package io.github.pstickney.jmerge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.function.Consumer;

public class JsonMerger extends Merger {

    protected ObjectMapper mapper;

    /**
     * Constructs a Merger with the default merge configuration.
     */
    public JsonMerger() {
        this(new MergeConfig());
    }

    /**
     * Constructs a Merger with the specified merge configuration.
     *
     * @param config the merge configuration to apply during merging
     */
    public JsonMerger(MergeConfig config) {
        this(config, mapper -> {
        });
    }

    /**
     * Constructs a Merger with the specified merge configuration and mapper customizer.
     *
     * @param config     the merge configuration to apply during merging
     * @param customizer the consumer to customize the mapper
     */
    public JsonMerger(MergeConfig config, Consumer<ObjectMapper> customizer) {
        super(config);
        mapper = new ObjectMapper();

        // apply overrides
        customizeMapper(customizer);
    }

    /**
     * Customize the mapper associated with this Merger
     *
     * @param customizer the consumer to customize the mapper
     */
    public void customizeMapper(Consumer<ObjectMapper> customizer) {
        customizer.accept(mapper);
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
