package org.kkarad.contextprop;

import java.util.Objects;
import java.util.Set;

final class Condition {
    private final String domainKey;
    private final Set<String> values;

    Condition(String domainKey, Set<String> values) {
        this.domainKey = domainKey;
        this.values = values;
    }

    String domainKey() {
        return domainKey;
    }

    Set<String> values() {
        return values;
    }

    boolean containsValue(String value) {
        return values.contains(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condition condition = (Condition) o;
        return Objects.equals(domainKey, condition.domainKey) &&
                Objects.equals(values, condition.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domainKey, values);
    }
}
