package org.kkarad.contextprop;

import java.util.*;
import java.util.stream.Stream;

final class ContextVisitor implements ParseVisitor {

    private final Map<String, ContextPropertyBuilder> propertyMap = new HashMap<>();

    private final Map<String,ContextBuilder> currentContexts = new HashMap<>();

    @Override
    public void startParse() {
        propertyMap.clear();
        currentContexts.clear();
    }

    @Override
    public void startProperty(String key) {
        currentContexts
                .computeIfAbsent(key, propKey -> new ContextBuilder())
                .reset();
    }

    @Override
    public void propertyCondition(String propertyKey, String domainKey, String[] conditionValues) {
        currentContexts
                .get(propertyKey)
                .add(new Condition(domainKey, Arrays.asList(conditionValues)));
    }

    @Override
    public void endProperty(String key, String value) {
        ContextPropertyBuilder builder = propertyMap.computeIfAbsent(key, ContextPropertyBuilder::new);
        ContextBuilder current = currentContexts.get(key);

        if (current.isEmpty()) {
            builder.defaultValue(value);
            return;
        }

        builder.add(current.build(value));
    }

    @Override
    public void endParse() {
        //validate
    }

    Stream<ContextProperty> properties() {
        List<ContextProperty> properties = new ArrayList<>();
        for (ContextPropertyBuilder propertyBuilder : propertyMap.values()) {
            properties.add(propertyBuilder.build());
        }
        return properties.stream();
    }

    private static class ContextPropertyBuilder {

        private final String key;

        private String defaultValue = null;

        private List<Context> contexts = new ArrayList<>();

        ContextPropertyBuilder(String key) {
            this.key = key;
        }

        void add(Context context) {
            contexts.add(context);
        }

        void defaultValue(String value) {
            this.defaultValue = value;
        }

        ContextProperty build() {
            return new ContextProperty(key, contexts, defaultValue);
        }
    }

    private class ContextBuilder {

        private final List<Condition> conditions = new ArrayList<>();

        void add(Condition condition) {
            conditions.add(condition);
        }

        boolean isEmpty() {
            return conditions.isEmpty();
        }

        Context build(String value) {
            return new Context(conditions, value);
        }

        void reset() {
            conditions.clear();
        }
    }
}
