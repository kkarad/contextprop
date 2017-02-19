package org.kkarad.contextprop;

import java.util.List;

final class ContextProperty {

    private final String key;

    private final List<Context> contexts;

    private final String defaultValue;

    ContextProperty(String key, List<Context> contexts, String defaultValue) {
        this.key = key;
        this.contexts = contexts;
        this.defaultValue = defaultValue;
    }

    public String key() {
        return key;
    }

    List<Context> contexts() {
        return contexts;
    }

    String defaultValue() {
        return defaultValue;
    }
}
