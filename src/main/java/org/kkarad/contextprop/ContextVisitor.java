package org.kkarad.contextprop;

import java.util.*;
import java.util.stream.Stream;

final class ContextVisitor implements ParseVisitor {

    private final Map<String, ContextPropertyBuilder> propertyMap = new HashMap<>();

    private final PropertyContextBuilder current = new PropertyContextBuilder();

    @Override
    public void startParse() {
        propertyMap.clear();
    }

    @Override
    public void startProperty(String key) {
        propertyMap.putIfAbsent(key, new ContextPropertyBuilder(key));
        current.reset();
    }

    @Override
    public void propertyCriteria(String propertyKey, String criteriaKey, String[] criteriaValues) {
        current.add(new Condition(criteriaKey, Arrays.asList(criteriaValues)));
    }

    @Override
    public void endProperty(String key, String value) {
        propertyMap.get(key).add(current.build(value));
    }

    @Override
    public void endParse() {
        //validate
    }

    public Stream<ContextProperty> properties() {
        List<ContextProperty> properties = new ArrayList<>();
        for (ContextPropertyBuilder propertyBuilder : propertyMap.values()) {
            properties.add(propertyBuilder.build());
        }
        return properties.stream();
    }

    private static class ContextPropertyBuilder {
        private final String key;

        public ContextPropertyBuilder(String key) {
            this.key = key;
        }

        public void add(Context context) {

        }

        public ContextProperty build() {
            return new ContextProperty(key, null, null);
        }
    }

    private class PropertyContextBuilder {

        private final List<Condition> criteria = new ArrayList<>();
        public void add(Condition condition) {
            criteria.add(condition);
        }

        public Context build(String value) {
            return new Context(criteria, value);
        }

        public void reset() {

        }
    }
}
