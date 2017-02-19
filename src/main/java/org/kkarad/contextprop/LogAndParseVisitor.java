package org.kkarad.contextprop;

import java.util.Arrays;

final class LogAndParseVisitor implements ParseVisitor {

    private boolean debug;

    private final ParseVisitor delegate;

    LogAndParseVisitor(boolean debug, ParseVisitor delegate) {
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
        delegate.startProperty(key);
    }

    @Override
    public void propertyCondition(String propertyKey, String domainKey, String[] conditionValues) {
        log("ParseVisitor.propertyCondition -> propertyKey = [" + propertyKey + "], domainKey = [" +
                domainKey + "], conditionValues = " + Arrays.toString(conditionValues));
        delegate.propertyCondition(propertyKey, domainKey, conditionValues);
    }

    @Override
    public void endProperty(String key, String value) {
        log("ParseVisitor.endProperty -> key = [" + key + "], value = [" + value + "]");
        delegate.endProperty(key, value);
    }

    @Override
    public void endParse() {
        log("ParseVisitor.endParse");
        delegate.endParse();
    }

    private void log(String message) {
        if (debug) {
            System.out.println(message);
        }
    }
}
