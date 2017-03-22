package org.kkarad.contextprop;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

interface JdkCollections {

    @SafeVarargs
    static <T> Set<T> asSet(T... items) {
        return new HashSet<>(asList(items));
    }
 }
