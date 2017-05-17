package org.kkarad.contextprop;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;

final class Domain {

    private final List<String> orderedKeys;

    static <E extends Enum> Domain create(Class<E> domainClass) {
        return new Domain(toOrderedList(domainClass));
    }

    private Domain(List<String> orderedKeys) {
        this.orderedKeys = orderedKeys;
    }

    private static <E extends Enum> List<String> toOrderedList(Class<E> keys) {
        return unmodifiableList(stream(keys.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList()));
    }

    boolean contains(String key) {
        return orderedKeys.contains(key);
    }


    int numberOfKeys() {
        return orderedKeys.size();
    }

    @Override
    public String toString() {
        return orderedKeys.toString();
    }

    List<String> orderedKeys() {
        return orderedKeys;
    }
}
