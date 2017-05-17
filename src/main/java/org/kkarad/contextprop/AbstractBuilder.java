package org.kkarad.contextprop;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
abstract class AbstractBuilder<T extends AbstractBuilder> {

    private static final String CONTEXT_IDENTIFIER = ".CTXT";

    private static final char CONTEXT_START_PATTERN = '(';

    private static final char CONTEXT_END_PATTERN = ')';

    private static final char CONDITION_VALUE_START_PATTERN = '[';

    private static final char CONDITION_VALUE_END_PATTERN = ']';

    private static final char CONDITION_DELIMITER = ',';

    private static final String CONDITION_VALUE_DELIMITER = ",";

    boolean requiresDefault = false;

    boolean systemPropertyOverride = false;

    Consumer<String> debugMsgParser = msg -> {
    };

    Consumer<String> debugMsgResolver = msg -> {
    };

    LogConsumer resolutionLogger = (property, systemOverridden, value, isLast) -> {
    };

    AbstractBuilder() {
    }

    public T requiresDefault() {
        this.requiresDefault = true;
        return (T) this;
    }

    public T allowSystemPropertyOverride() {
        systemPropertyOverride = true;
        return (T) this;
    }

    public T debugParser(Consumer<String> debugMsgConsumer) {
        this.debugMsgParser = debugMsgConsumer;
        return (T) this;
    }

    public T debugResolver(Consumer<String> debugMsgConsumer) {
        this.debugMsgResolver = debugMsgConsumer;
        return (T) this;
    }

    public T logResolution(LogConsumer resolutionLogger) {
        this.resolutionLogger = resolutionLogger;
        return (T) this;
    }

    PropertyParser createParser() {
        ParseVisitor visitor = new LogAndDelegateVisitor(debugMsgParser, new ContextVisitor());
        return new PropertyParser(
                visitor,
                new ContextPattern(
                        CONTEXT_IDENTIFIER,
                        CONTEXT_START_PATTERN,
                        CONTEXT_END_PATTERN,
                        new ConditionPattern(
                                CONDITION_VALUE_START_PATTERN,
                                CONDITION_VALUE_END_PATTERN,
                                visitor,
                                CONDITION_VALUE_DELIMITER,
                                CONDITION_DELIMITER),
                        visitor));
    }

    PropertyValidator createValidator(Domain domain) {
        return new PropertyValidator(domain, requiresDefault);
    }

    ContextPropertyResolver createResolver() {
        return new ContextPropertyResolver(
                new PropertyResolver(debugMsgResolver),
                systemPropertyOverride,
                debugMsgResolver,
                resolutionLogger);
    }
}
