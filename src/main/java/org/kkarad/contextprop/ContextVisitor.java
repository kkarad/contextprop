package org.kkarad.contextprop;

import java.util.Map;
import java.util.stream.Stream;

final class ContextVisitor implements ParseVisitor {

    @Override
    public void startParse() {

    }

    @Override
    public void startProperty(String key) {

    }

    @Override
    public void propertyCriteria(String propertyKey, String criteriaKey, String[] criteriaValues) {

    }

    @Override
    public void endProperty(String key, String value) {

    }

    @Override
    public void endParse() {

    }

    public Stream<ContextProperty> properties() {
        return null;
    }
}
