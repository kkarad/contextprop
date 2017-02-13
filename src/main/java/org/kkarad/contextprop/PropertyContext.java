package org.kkarad.contextprop;

import java.util.List;

final class PropertyContext {

    private final List<Criterion> criteria;

    private final String value;

    public PropertyContext(List<Criterion> criteria, String value) {
        this.criteria = criteria;
        this.value = value;
    }

    public List<Criterion> criteria() {
        return criteria;
    }

    public String value() {
        return value;
    }
}
