package org.kkarad.contextprop;

import java.util.Collection;
import java.util.Properties;

public final class ContextProperties {

    private static final String CONTEXT_IDENTIFIER = ".CTXT";

    private static final char CONTEXT_START_PATTERN = '(';

    private static final char CONTEXT_END_PATTERN = ')';

    private static final char CONDITION_VALUE_START_PATTERN = '[';

    private static final char CONDITION_VALUE_END_PATTERN = ']';

    private static final char CONDITION_DELIMITER = ',';

    private static final String CONDITION_VALUE_DELIMITER = ",";

    private final PropertyParser propertyParser;

    private final DomainPredicates predicates;

    private boolean requiresDefault = false;

    public static ContextProperties create(DomainPredicates predicates) {
        ParseVisitor visitor = new LogAndDelegateVisitor(true, new ContextVisitor());

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
        return new ContextProperties(predicates, propertyParser);
    }

    private ContextProperties(DomainPredicates predicates,
                              PropertyParser propertyParser) {
        this.predicates = predicates;
        this.propertyParser = propertyParser;
    }

    public ContextProperties requiresDefault(boolean requiresDefault) {
        this.requiresDefault = requiresDefault;
        return this;
    }

    public Properties resolve(Properties unresolved) {
        Collection<ContextProperty> contextualisedProperties = propertyParser.parse(unresolved);

        ContextPropertyResolver contextPropertyResolver = new ContextPropertyResolver(
                new PropertyValidator(predicates.domain(), requiresDefault),
                new PropertyResolver(predicates));
        Properties resolved = new Properties();
        contextPropertyResolver.resolve(contextualisedProperties, resolved);
        return resolved;
    }
}
