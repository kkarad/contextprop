package org.kkarad.contextprop;

import java.util.Properties;

public final class TypedProperties {

    private final Properties properties;

    TypedProperties(Properties properties) {
        this.properties = properties;
    }

    public String getString() {
        throw new UnsupportedOperationException();
    }

    public boolean getBoolean(String property) {
        return Boolean.valueOf(properties.getProperty(property));
    }

    public int getInteger() {
        throw new UnsupportedOperationException();
    }

    public long getLong() {
        throw new UnsupportedOperationException();
    }

    public double getDouble() {
        throw new UnsupportedOperationException();
    }
}
