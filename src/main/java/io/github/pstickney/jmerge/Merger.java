package io.github.pstickney.jmerge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.pstickney.jmerge.exception.StrategyException;
import io.github.pstickney.jmerge.util.MergerUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The core merging engine responsible for merging JSON/YAML structures based on configurable strategies.
 * This abstract class provides the general merging logic and delegates JSON/YAML-specific node creation
 * to subclasses via abstract methods.
 */
public abstract class Merger {

    protected final MergeConfig config;

    /**
     * Constructs a Merger with the default merge configuration.
     */
    public Merger() {
        this(new MergeConfig());
    }

    /**
     * Constructs a Merger with the specified merge configuration.
     *
     * @param config the merge configuration to apply during merging
     */
    public Merger(MergeConfig config) {
        this.config = config;
    }

    /**
     * Merges two string representations of JSON/YAML according to the configured strategies.
     *
     * @param base    the base JSON/YAML string
     * @param overlay the overlay JSON/YAML string to merge on top of the base
     * @return the merged JSON/YAML string
     * @throws JsonProcessingException if parsing or processing fails
     */
    public abstract String merge(String base, String overlay) throws JsonProcessingException;

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
    public abstract <T> T merge(T base, T overlay, Class<T> outputClass) throws JsonProcessingException;

    /**
     * Provides a new empty ObjectNode instance specific to the JSON/YAML implementation.
     *
     * @return a new empty ObjectNode
     */
    protected abstract ObjectNode getObjectNode();

    /**
     * Provides a new empty ArrayNode instance specific to the JSON/YAML implementation.
     *
     * @return a new empty ArrayNode
     */
    protected abstract ArrayNode getArrayNode();

    /**
     * Merges two JsonNode trees recursively according to merge rules and strategies.
     * Objects and arrays are merged based on configuration; scalar values are replaced.
     *
     * @param base    the base JsonNode
     * @param overlay the overlay JsonNode to merge on top of the base
     * @return the merged JsonNode result
     */
    public JsonNode merge(JsonNode base, JsonNode overlay) {
        return mergeNodes("", base, overlay);
    }

    /**
     * Recursively merges two JsonNodes at the given path, applying merge rules.
     * Delegates to object or array merging as appropriate, or replaces scalar values.
     *
     * @param path    the current JSON path for rule lookup
     * @param base    the base JsonNode
     * @param overlay the overlay JsonNode
     * @return the merged JsonNode
     */
    private JsonNode mergeNodes(String path, JsonNode base, JsonNode overlay) {
        MergeRule rule = config.findRule(path);

        if (base.isObject() && overlay.isObject()) {
            return mergeObjects(path, (ObjectNode) base, (ObjectNode) overlay, rule);
        } else if (base.isArray() && overlay.isArray()) {
            return mergeArrays(path, (ArrayNode) base, (ArrayNode) overlay, rule);
        } else {
            return overlay.deepCopy();
        }
    }

    /**
     * Merges two ObjectNodes according to the specified strategy.
     * Supports REPLACE and MERGE strategies; merges fields recursively.
     *
     * @param path    the current JSON path for rule lookup
     * @param base    the base ObjectNode
     * @param overlay the overlay ObjectNode
     * @param rule    the merge rule applicable at this path
     * @return the merged ObjectNode
     */
    private ObjectNode mergeObjects(String path, ObjectNode base, ObjectNode overlay, MergeRule rule) {
        ObjectNode result = getObjectNode();
        Strategy strategy = rule != null ? rule.getStrategy() : config.getObjectStrategy();

        if (strategy == Strategy.REPLACE) {
            result = overlay.deepCopy();
        } else if (strategy == Strategy.MERGE) {
            result = base.deepCopy();
            Set<String> fields = MergerUtil.combineIterators(base.fieldNames(), overlay.fieldNames());

            for (String field : fields) {
                JsonNode baseVal = result.get(field);
                JsonNode overlayVal = overlay.get(field);
                String childPath = path.isEmpty() ? field : path + "." + field;
                MergeRule childRule = config.findRule(childPath);
                Strategy childStrategy = childRule != null ? childRule.getStrategy() : config.getObjectStrategy();

                if (baseVal != null && overlayVal != null) { // Both exist
                    if (childStrategy == Strategy.REPLACE) {
                        result.set(field, overlayVal.deepCopy());
                    } else if (childStrategy == Strategy.MERGE) {
                        result.set(field, mergeNodes(childPath, baseVal, overlayVal));
                    }
                } else if (baseVal == null && overlayVal != null) { // field added
                    result.set(field, overlayVal.deepCopy());
                } else if (baseVal != null && overlayVal == null) { // field removed
                    if (childStrategy == Strategy.REPLACE) {
                        result.remove(field);
                    } else if (childStrategy == Strategy.MERGE) {
                        result.set(field, baseVal.deepCopy());
                    }
                }
            }
        } else {
            throw new StrategyException(String.format("Invalid strategy '%s' for object merge at path '%s'", strategy, path.isEmpty() ? "." : path));
        }

        return result;
    }

    /**
     * Merges two ArrayNodes according to the specified strategy.
     * Supports REPLACE, APPEND, and MERGE (with key field) strategies.
     *
     * @param path    the current JSON path for rule lookup
     * @param base    the base ArrayNode
     * @param overlay the overlay ArrayNode
     * @param rule    the merge rule applicable at this path
     * @return the merged ArrayNode
     */
    private ArrayNode mergeArrays(String path, ArrayNode base, ArrayNode overlay, MergeRule rule) {
        ArrayNode result = getArrayNode();
        Strategy strategy = rule != null ? rule.getStrategy() : config.getArrayStrategy();

        if (strategy == Strategy.REPLACE) {
            result = overlay.deepCopy();
        } else if (strategy == Strategy.APPEND) {
            result.addAll(base);
            result.addAll(overlay);
        } else if (strategy == Strategy.MERGE) {
            if (rule == null)
                throw new StrategyException(String.format("Missing array merge rule for '%s'", path));

            if (rule.getKeyField() == null)
                throw new StrategyException(String.format("Missing keyField in array merge rule for '%s'", path));

            // Build up baseMap based on rule keyField
            Map<String, JsonNode> baseMap = new LinkedHashMap<>();
            for (JsonNode item : base) {
                String key = getNestedValue(item, rule.getKeyField());
                if (key != null)
                    baseMap.put(key, item);
            }

            // Add array element to result only if it exists in overlay
            // So elements that only exist in base will get removed in the result
            for (JsonNode item : overlay) {
                String key = getNestedValue(item, rule.getKeyField());
                if (key != null && baseMap.containsKey(key)) {
                    result.add(mergeNodes(path, baseMap.get(key), item));
                    baseMap.remove(key);
                } else {
                    result.add(item.deepCopy());
                }
            }
        }

        return result;
    }

    /**
     * Retrieves a nested string value from a JsonNode using a dot-separated path.
     * Supports escaping dots inside quotes.
     *
     * @param node       the JsonNode to query
     * @param nestedPath the dot-separated path to the nested value
     * @return the string value if found and is a value node; otherwise null
     */
    private String getNestedValue(JsonNode node, String nestedPath) {
        String[] parts = nestedPath.split("\\.(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        JsonNode current = node;
        for (String p : parts) {
            if (current == null)
                return null;
            p = p.replaceAll("\"", "");
            current = current.get(p);
        }
        return (current != null && current.isValueNode()) ? current.asText() : null;
    }
}
