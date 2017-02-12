package org.kkarad.contextprop;

import java.util.List;

final class ContextProperty {

    private final String key;

    private final List<ContextCriteria> contextCriteria;

    private final String defaultValue;

    public ContextProperty(String key, List<ContextCriteria> contextCriteria, String defaultValue) {
        this.key = key;
        this.contextCriteria = contextCriteria;
        this.defaultValue = defaultValue;
    }

    public String key() {
        return key;
    }

    public List<ContextCriteria> contextCriteriaList() {
        return contextCriteria;
    }

    public String defaultValue() {
        return defaultValue;
    }
}
