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
        current.add(new Criterion(criteriaKey, Arrays.asList(criteriaValues)));
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

        public void add(PropertyContext propertyContext) {

        }

        public ContextProperty build() {
            return new ContextProperty(key, null, null);
        }
    }

    private class PropertyContextBuilder {

        private final List<Criterion> criteria = new ArrayList<>();
        public void add(Criterion criterion) {
            criteria.add(criterion);
        }

        public PropertyContext build(String value) {
            return new PropertyContext(criteria, value);
        }

        public void reset() {

        }
    }
}
