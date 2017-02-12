package org.kkarad.contextprop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;

public final class Context {

    private final List<String> orderedKeys;

    private final Map<String, String> keyValues;

    public static <E extends Enum> Builder basedOn(Class<E> keys) {
        return new Builder(toOrderedList(keys));
    }

    private static <E extends Enum> List<String> toOrderedList(Class<E> keys) {
        return unmodifiableList(stream(keys.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList()));
    }

    private Context(List<String> orderedKeys, Map<String, String> keyValues) {
        this.orderedKeys = orderedKeys;
        this.keyValues = keyValues;
    }

    public boolean contains(String key) {
        return orderedKeys.contains(key);
    }

    public List<String> orderedKeys() {
        return orderedKeys;
    }


    public static class Builder {

        private final List<String> orderedKeys;

        private final HashMap<String, String> keyValues = new HashMap<>();


        private Builder(List<String> orderedKeys) {
            this.orderedKeys = orderedKeys;
        }

        public Builder entry(String key, String value) {
            if (!orderedKeys.contains(key)) {
                throw new IllegalArgumentException(format("Entry key: %s can't be found in context keys (%s)", key, orderedKeys));
            }
            keyValues.put(key, value);
            return this;
        }

        public Context create() {
            if (keyValues.size() != orderedKeys.size()) {
                String msg = format("Entry keys don't cover all context keys (entryKeys: %s, contextKeys: %s)",
                        keyValues.keySet(), orderedKeys);
                throw new IllegalArgumentException(msg);
            }
            return new Context(orderedKeys, keyValues);
        }
    }
}
