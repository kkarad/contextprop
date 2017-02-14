package org.kkarad.contextprop;

import java.util.List;
import java.util.Objects;

final class Condition {
    private final String domainKey;
    private final List<String> values;

    public Condition(String domainKey, List<String> values) {
        this.domainKey = domainKey;
        this.values = values;
    }

    public String domainKey() {
        return domainKey;
    }

    public List<String> values() {
        return values;
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
