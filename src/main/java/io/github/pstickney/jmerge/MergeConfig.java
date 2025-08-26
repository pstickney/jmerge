package io.github.pstickney.jmerge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MergeConfig {

    @Builder.Default
    private Boolean prettyPrint = Boolean.FALSE;
    @Builder.Default
    private Strategy arrayStrategy = Strategy.APPEND;
    @Builder.Default
    private Strategy objectStrategy = Strategy.MERGE;
    private final List<MergeRule> rules = new ArrayList<>();

    /**
     * Adds a merge rule with the default MERGE strategy for the specified path and key field.
     *
     * @param path     the JSON path for which the rule applies
     * @param keyField the key field used to key on when doing merges
     * @return the updated MergeConfig instance
     */
    public MergeConfig addRule(String path, String keyField) {
        return addRule(path, keyField, Strategy.MERGE);
    }

    /**
     * Adds a merge rule with a custom strategy for the specified path and key field.
     *
     * @param path     the JSON path for which the rule applies
     * @param keyField the key field used to key on when doing merges
     * @param strategy the strategy to apply for this rule
     * @return the updated MergeConfig instance
     */
    public MergeConfig addRule(String path, String keyField, Strategy strategy) {
        rules.add(new MergeRule(path, keyField, strategy));
        return this;
    }

    /**
     * Finds a merge rule that matches the specified path.
     *
     * @param path the JSON path to look up
     * @return the matching MergeRule if found, otherwise null
     */
    public MergeRule findRule(String path) {
        return rules.stream()
            .filter(rule -> rule.getPath().equals(path))
            .findFirst()
            .orElse(null);
    }
}
