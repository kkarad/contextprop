package org.kkarad.contextprop;

import java.util.List;
import java.util.Objects;

final class Criterion {
    private final String key;
    private final List<String> values;

    public Criterion(String key, List<String> values) {
        this.key = key;
        this.values = values;
    }

    public String key() {
        return key;
    }

    public List<String> values() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Criterion criterion = (Criterion) o;
        return Objects.equals(key, criterion.key) &&
                Objects.equals(values, criterion.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, values);
    }
}
