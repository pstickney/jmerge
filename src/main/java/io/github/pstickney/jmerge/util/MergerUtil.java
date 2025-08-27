package io.github.pstickney.jmerge.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MergerUtil {
    @SafeVarargs
    public static Set<String> combineIterators(Iterator<String> ...iterators) {
        Set<String> result = new HashSet<>();
        for (Iterator<String> iterator : iterators) {
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        }
        return result;
    }
}
