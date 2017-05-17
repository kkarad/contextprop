package org.kkarad.contextprop;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

public final class TypedProperties {

    private final Properties properties;

    TypedProperties(Properties properties) {
        this.properties = properties;
    }

    public String getString(String property) {
        return propertyValue(property);
    }

    public Optional<String> getOptString(String property) {
        return Optional.ofNullable(nullablePropertyValue(property));
    }

    public Optional<Boolean> getOptBoolean(String property) {
        return Optional.ofNullable(nullablePropertyValue(property, TrueFalse::parse));
    }

    public boolean getBoolean(String property) {
        return propertyValue(property, TrueFalse::parse);
    }

    public Optional<Integer> getOptInteger(String property) {
        return Optional.ofNullable(nullablePropertyValue(property, Integer::valueOf));
    }

    public int getInteger(String property) {
        return propertyValue(property, Integer::valueOf);
    }

    public Optional<Long> getOptLong(String property) {
        return Optional.ofNullable(nullablePropertyValue(property, Long::valueOf));
    }

    public long getLong(String property) {
        return propertyValue(property, Long::valueOf);
    }

    public Optional<BigDecimal> getOptBigDecimal(String property) {
        return Optional.ofNullable(nullablePropertyValue(property, BigDecimal::new));
    }

    public BigDecimal getBigDecimal(String property) {
        return propertyValue(property, BigDecimal::new);
    }

    private <T> T propertyValue(String property, Function<String, T> parseFunction) {
        String value = propertyValue(property);
        return parseFunction.apply(value);
    }

    private String propertyValue(String property) {
        String value = nullablePropertyValue(property);
        if (isNullOrEmpty(value)) {
            throw new PropertyNotFoundException(property);
        }

        return value;
    }

    private <T> T nullablePropertyValue(String property, Function<String, T> parseFunction) {
        String value = nullablePropertyValue(property);
        return !isNullOrEmpty(value) ? parseFunction.apply(value) : null;
    }

    private String nullablePropertyValue(String property) {
        return properties.getProperty(property);
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
