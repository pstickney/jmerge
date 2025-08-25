package io.github.pstickney.jmerge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MergeRule {
    private final String path;
    private final String keyField;
    private final Strategy strategy;
}
