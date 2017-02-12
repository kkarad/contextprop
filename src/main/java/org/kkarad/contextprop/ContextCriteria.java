package org.kkarad.contextprop;

import java.util.List;

final class ContextCriteria {

    private final List<Criterion> criteria;

    private final String value;

    public ContextCriteria(List<Criterion> criteria, String value) {
        this.criteria = criteria;
        this.value = value;
    }

    public List<Criterion> criteria() {
        return criteria;
    }

    public String getValue() {
        return value;
    }
}
