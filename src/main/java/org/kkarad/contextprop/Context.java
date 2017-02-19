package org.kkarad.contextprop;

import java.util.List;

final class Context {

    private final List<Condition> conditions;

    private final String propertyValue;

    public Context(List<Condition> conditions, String propertyValue) {
        this.conditions = conditions;
        this.propertyValue = propertyValue;
    }

    List<Condition> conditions() {
        return conditions;
    }

    String propertyValue() {
        return propertyValue;
    }
}
