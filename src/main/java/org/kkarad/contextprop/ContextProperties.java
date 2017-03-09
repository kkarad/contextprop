package org.kkarad.contextprop;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Stream;

public final class ContextProperties {

    private static final String CONTEXT_START_PATTERN = ".CTXT(";

    private static final char CONTEXT_END_PATTERN = ')';

    private static final char CRITERIA_VALUE_START_PATTERN = '[';

    private static final char CRITERIA_VALUE_END_PATTERN = ']';

    private static final char CRITERIA_DELIMITER = ',';

    private static final String CRITERIA_VALUE_DELIMITER = ",";

    private final PropertyParser propertyParser;

    private final DomainPredicates predicates;

    private boolean requiresDefault = false;

    public static ContextProperties create(DomainPredicates predicates) {
        ParseVisitor visitor = new LogAndDelegateVisitor(true, new ContextVisitor());

        PropertyParser propertyParser = new PropertyParser(
                visitor,
                new ContextPattern(
                        CONTEXT_START_PATTERN,
                        CONTEXT_END_PATTERN,
                        new CriteriaPattern(
                                CRITERIA_VALUE_START_PATTERN,
                                CRITERIA_VALUE_END_PATTERN,
                                visitor,
                                CRITERIA_VALUE_DELIMITER,
                                CRITERIA_DELIMITER),
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
