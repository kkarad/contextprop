package org.kkarad.contextprop;

import java.util.Arrays;

final class LogAndParseVisitor implements ParseVisitor {

    private boolean debug;

    private final ParseVisitor delegate;

    public LogAndParseVisitor(boolean debug, ParseVisitor delegate) {
        this.debug = debug;
        this.delegate = delegate;
    }

    @Override
    public void startParse() {
        log("ParseVisitor.startParse");
        delegate.startParse();
    }

    @Override
    public void startProperty(String key) {
        log("ParseVisitor.startProperty -> key = [" + key + "]");
    }

    @Override
    public void propertyCriteria(String propertyKey, String criteriaKey, String[] criteriaValues) {
        log("ParseVisitor.propertyCriteria -> propertyKey = [" + propertyKey + "], criteriaKey = [" +
                criteriaKey + "], criteriaValues = " + Arrays.toString(criteriaValues));
    }

    @Override
    public void endProperty(String key, String value) {
        log("ParseVisitor.endProperty -> key = [" + key + "], value = [" + value + "]");
    }

    @Override
    public void endParse() {
        log("ParseVisitor.endParse");
    }

    private void log(String message) {
        if (debug) {
            System.out.println(message);
        }
    }
}
