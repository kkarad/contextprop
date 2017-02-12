package org.kkarad.contextprop;

interface ParseVisitor {
    void startParse();

    void startProperty(String key);

    void propertyCriteria(String propertyKey, String criteriaKey, String[] criteriaValues);

    void endProperty(String key, String value);

    void endParse();
}
