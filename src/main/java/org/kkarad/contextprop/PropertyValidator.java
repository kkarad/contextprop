package org.kkarad.contextprop;

import java.util.List;

import static java.lang.String.format;

public class PropertyValidator {
    private final Domain domain;
    private final boolean requiresDefault;

    public PropertyValidator(Domain domain, boolean requiresDefault) {

        this.domain = domain;
        this.requiresDefault = requiresDefault;
    }

    public void validate(ContextProperty property) {
        validateCriteriaKeys(property);
        if (requiresDefault) {
            validateDefaultRequirement(property);
        }
        validateCriteriaOrder(property);
        validateCriteriaScope(property);
    }

    private void validateCriteriaKeys(ContextProperty property) {
        String propertyKey = property.key();
        property.contexts()
                .stream()
                .flatMap(ctxCriteria -> ctxCriteria.conditions().stream())
                .map(Condition::domainKey)
                .forEach(criteriaKey -> {
                    if (!domain.contains(criteriaKey)) {
                        throw invalidCriteriaKey(propertyKey, criteriaKey);
                    }
                });
    }

    private IllegalArgumentException invalidCriteriaKey(String propertyKey, String criteriaKey) {
        String msg = format("Unrecognised criteria key: '%s' in property: '%s'", criteriaKey, propertyKey);
        return new IllegalArgumentException(msg);
    }

    private void validateDefaultRequirement(ContextProperty property) {
        if (property.defaultValue() == null) {
            throw missingDefaultProperty(property.key());
        }
    }

    private IllegalArgumentException missingDefaultProperty(String propertyKey) {
        return new IllegalArgumentException(format("default context is missing from property: '%s'", propertyKey));
    }

    private void validateCriteriaOrder(ContextProperty property) {
        for (String ctxKey : domain.orderedKeys()) {
            if (contextKeyExists(property, ctxKey)) {
                for (Context ctxCriteria : property.contexts()) {
                    if (contextKeyMissingFrom(ctxKey, ctxCriteria) &&
                            lowerOrderContextKeyExists(ctxKey, ctxCriteria)) {
                        String ctxCriteriaAsString = toString(ctxCriteria);
                        throw missingHighOrderContextKey(ctxKey, property.key(), ctxCriteriaAsString);
                    }
                }
            }
        }
    }

    private boolean contextKeyExists(ContextProperty property, String ctxKey) {
        for (Context ctxCriteria : property.contexts()) {
            for (Condition condition : ctxCriteria.conditions()) {
                if (condition.domainKey().equals(ctxKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean contextKeyMissingFrom(String ctxKey, Context ctxCriteria) {
        for (Condition condition : ctxCriteria.conditions()) {
            if (condition.domainKey().equals(ctxKey)) {
                return false;
            }
        }
        return true;
    }

    private boolean lowerOrderContextKeyExists(String ctxKey, Context ctxCriteria) {
        int indexOfHighOrderKey = domain.orderedKeys().indexOf(ctxKey);
        List<String> lowOrderKeys = domain.orderedKeys()
                .subList(indexOfHighOrderKey, domain.orderedKeys().size() - 1);

        for (Condition condition : ctxCriteria.conditions()) {
            if (lowOrderKeys.contains(condition.domainKey())) {
                return true;
            }
        }
        return false;
    }

    private String toString(Context ctxCriteria) {
        StringBuilder b = new StringBuilder();
        for (String ctxKey : domain.orderedKeys()) {
            Condition condition = findCriteria(ctxKey, ctxCriteria);
            if (condition != null) {
                b.append(b.length() != 0 ? "," : "");
                b.append(condition.domainKey()).append("(");
                appendValues(b, condition.values());
                b.append(")");
            }
        }
        return b.toString();
    }

    private Condition findCriteria(String ctxKey, Context ctxCriteria) {
        for (Condition condition : ctxCriteria.conditions()) {
            if (condition.domainKey().equals(ctxKey)) {
                return condition;
            }
        }
        return null;
    }

    private void appendValues(StringBuilder b, List<String> values) {
        int offset = b.length();
        for (String value : values) {
            b.append(b.length() != offset ? "," : "").append(value);
        }
    }

    private IllegalArgumentException missingHighOrderContextKey(String ctxKey,
                                                                String propertyKey,
                                                                String ctxCriteria) {
        String msg = String.format("High order context key: '%s' is missing from property: '%s' with context: '%s'",
                ctxKey, propertyKey, ctxCriteria);
        return new IllegalArgumentException(msg);
    }

    private void validateCriteriaScope(ContextProperty property) {
        //todo
    }
}
