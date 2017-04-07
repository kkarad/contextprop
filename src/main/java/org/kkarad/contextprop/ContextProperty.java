package org.kkarad.contextprop;

import java.util.ArrayList;
import java.util.List;

final class ContextProperty {

    private final String key;

    private final List<Context> contexts;

    private final String defaultValue;

    private ContextProperty(String key, List<Context> contexts, String defaultValue) {
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

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(key);
        b.append('(');
        if (defaultValue != null) {
            b.append("[]=").append(defaultValue).append(!contexts.isEmpty() ? "," : "");
        }
        if (!contexts.isEmpty()) {
            b.append(contexts);
        }
        b.append(')');
        return b.toString();
    }

    static class Builder {

        private final String key;

        private String defaultValue = null;

        private List<Context> contexts = new ArrayList<>();

        static Builder contextProperty(String key) {
            return new Builder(key);
        }

        private Builder(String key) {
            this.key = key;
        }

        Builder add(Context context) {
            contexts.add(context);
            return this;
        }

        Builder defaultValue(String value) {
            this.defaultValue = value;
            return this;
        }

        ContextProperty get() {
            return new ContextProperty(key, contexts, defaultValue);
        }
    }
}
