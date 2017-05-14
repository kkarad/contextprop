package org.kkarad.contextprop;

import java.util.Collection;
import java.util.Properties;
import java.util.function.Consumer;

public final class ContextProperties {

    private static final String CONTEXT_IDENTIFIER = ".CTXT";

    private static final char CONTEXT_START_PATTERN = '(';

    private static final char CONTEXT_END_PATTERN = ')';

    private static final char CONDITION_VALUE_START_PATTERN = '[';

    private static final char CONDITION_VALUE_END_PATTERN = ']';

    private static final char CONDITION_DELIMITER = ',';

    private static final String CONDITION_VALUE_DELIMITER = ",";

    private final PropertyParser propertyParser;

    private final ContextPropertyResolver propertyResolver;

    public ContextProperties(PropertyParser propertyParser, ContextPropertyResolver propertyResolver) {
        this.propertyParser = propertyParser;
        this.propertyResolver = propertyResolver;
    }

    public static ContextProperties.Builder create(DomainPredicates predicates) {
        return new Builder(predicates);
    }

    public Properties resolve(Properties unresolved) {
        Collection<ContextProperty> contextualisedProperties = propertyParser.parse(unresolved);

        Properties resolved = new Properties();
        propertyResolver.resolve(contextualisedProperties, resolved);
        return resolved;
    }

    public static class Builder {

        private final DomainPredicates predicates;

        private boolean requiresDefault = false;

        private Consumer<String> debugMsgParser = msg -> {
        };

        private Consumer<String> debugMsgResolver = msg -> {
        };

        private LogConsumer resolutionLogger = (property, value, isLast) -> {
        };

        private Builder(DomainPredicates predicates) {
            this.predicates = predicates;
        }

        public ContextProperties.Builder requiresDefault(boolean requiresDefault) {
            this.requiresDefault = requiresDefault;
            return this;
        }

        public ContextProperties.Builder debugParser(Consumer<String> debugMsgConsumer) {
            this.debugMsgParser = debugMsgConsumer;
            return this;
        }

        public ContextProperties.Builder debugResolver(Consumer<String> debugMsgConsumer) {
            this.debugMsgResolver = debugMsgConsumer;
            return this;
        }

        public ContextProperties.Builder logResolution(LogConsumer resolutionLogger) {
            this.resolutionLogger = resolutionLogger;
            return this;
        }

        public ContextProperties get() {
            ParseVisitor visitor = new LogAndDelegateVisitor(debugMsgParser, new ContextVisitor());

            PropertyParser propertyParser = new PropertyParser(
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

            ContextPropertyResolver propertyResolver = new ContextPropertyResolver(
                    new PropertyValidator(predicates.domain(), requiresDefault),
                    new PropertyResolver(predicates, debugMsgResolver),
                    resolutionLogger);

            return new ContextProperties(propertyParser, propertyResolver);
        }

        public Properties resolve(Properties unresolved) {
            return get().resolve(unresolved);
        }
    }

    @FunctionalInterface
    public interface LogConsumer {
        void log(String property, String value, boolean isLast);
    }
}
