package org.kkarad.contextprop;

import java.util.*;

import static java.lang.String.format;

class ContextVisitor implements ParseVisitor {

    private final Map<String, ContextProperty.Builder> propertyMap = new HashMap<>();

    private final Map<String, Context.Builder> currentContexts = new HashMap<>();

    @Override
    public void startParse() {
        propertyMap.clear();
        currentContexts.clear();
    }

    @Override
    public void startProperty(String key) {
        currentContexts
                .computeIfAbsent(key, propKey -> Context.Builder.context())
                .reset();
    }

    @Override
    public void propertyCondition(String propertyKey, String domainKey, String[] conditionValues) {
        HashSet<String> valueSet = new HashSet<>(conditionValues.length);
        Collections.addAll(valueSet, conditionValues);

        if (conditionValues.length != valueSet.size()) {
            throw new ContextPropParseException(format("Duplicate values found in '%s' condition of '%s'",
                    domainKey, propertyKey));
        }

        Context.Builder builder = currentContexts.get(propertyKey);
        if (builder.containsCondition(domainKey)) {
            throw new ContextPropParseException(format("Duplicate condition for domain key '%s' found in property '%s'",
                    domainKey, propertyKey));
        }

        builder.condition(domainKey, valueSet);
    }

    @Override
    public void endProperty(String key, String value) {
        ContextProperty.Builder builder = propertyMap.computeIfAbsent(key, ContextProperty.Builder::contextProperty);
        Context.Builder current = currentContexts.get(key);

        if (current.isEmpty()) {
            builder.defaultValue(value);
            return;
        }

        builder.add(current.getWithValue(value));
    }

    @Override
    public void endParse() {
        //validate
    }

    @Override
    public Collection<ContextProperty> properties() {
        List<ContextProperty> properties = new ArrayList<>();
        for (ContextProperty.Builder propertyBuilder : propertyMap.values()) {
            properties.add(propertyBuilder.get());
        }
        return properties;
    }

}
