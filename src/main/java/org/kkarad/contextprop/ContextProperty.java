package org.kkarad.contextprop;

import java.util.List;

final class ContextProperty {

    private final String key;

    private final List<PropertyContext> propertyContext;

    private final String defaultValue;

    public ContextProperty(String key, List<PropertyContext> propertyContext, String defaultValue) {
        this.key = key;
        this.propertyContext = propertyContext;
        this.defaultValue = defaultValue;
    }

    public String key() {
        return key;
    }

    public List<PropertyContext> propertyContexts() {
        return propertyContext;
    }

    public String defaultValue() {
        return defaultValue;
    }
}
