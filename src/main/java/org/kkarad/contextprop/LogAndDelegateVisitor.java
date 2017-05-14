package org.kkarad.contextprop;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

class LogAndDelegateVisitor implements ParseVisitor {

    private final Consumer<String> debugMsgParser;

    private final ParseVisitor delegate;

    LogAndDelegateVisitor(Consumer<String> debugMsgParser, ParseVisitor delegate) {
        this.debugMsgParser = debugMsgParser;
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
        debugMsgParser.accept(message);
    }

    @Override
    public Collection<ContextProperty> properties() {
        return delegate.properties();
    }
}
