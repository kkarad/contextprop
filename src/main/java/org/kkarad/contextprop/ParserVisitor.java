package org.kkarad.contextprop;

import java.util.Arrays;

final class ParserVisitor {

    public void startParse() {
        System.out.println("ParserVisitor.startParse");
    }

    public void startProperty(String key) {
        System.out.println("ParserVisitor.startProperty -> key = [" + key + "]");
    }

    public void propertyCriteria(String propertyKey, String criteriaKey, String[] criteriaValues) {
        System.out.println("ParserVisitor.propertyCriteria -> propertyKey = [" + propertyKey + "], " +
                "criteriaKey = [" + criteriaKey + "], criteriaValues = " + Arrays.toString(criteriaValues));
    }

    public void endProperty(String key, String value) {
        System.out.println("ParserVisitor.endProperty -> key = [" + key + "], value = [" + value + "]");
    }

    public void endParse() {
        System.out.println("ParserVisitor.endParse");
    }
}
