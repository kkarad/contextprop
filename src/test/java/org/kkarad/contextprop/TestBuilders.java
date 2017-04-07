package org.kkarad.contextprop;

import static org.kkarad.contextprop.ContextProperty.Builder.contextProperty;

public interface TestBuilders {

    static ContextProperty.Builder aContextProperty() {
        return contextProperty("my.property.key");
    }
}
