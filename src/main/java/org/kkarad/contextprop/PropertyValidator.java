package org.kkarad.contextprop;

import java.util.List;

import static java.lang.String.format;

final class PropertyValidator {
    private final Domain domain;
    private final boolean requiresDefault;

    PropertyValidator(Domain domain, boolean requiresDefault) {

        this.domain = domain;
        this.requiresDefault = requiresDefault;
    }

    void validate(ContextProperty property) {
        validateConditionKeys(property);
        if (requiresDefault) {
            validateDefaultRequirement(property);
        }
        validateConditionOrder(property);
        validateConditionScope(property);
    }

    private void validateConditionKeys(ContextProperty property) {
        String propertyKey = property.key();
        property.contexts()
                .stream()
                .flatMap(ctxCondition -> ctxCondition.conditions().stream())
                .map(Condition::domainKey)
                .forEach(conditionKey -> {
                    if (!domain.contains(conditionKey)) {
                        throw invalidConditionKey(propertyKey, conditionKey);
                    }
                });
    }

    private IllegalArgumentException invalidConditionKey(String propertyKey, String conditionKey) {
        String msg = format("Unrecognised condition key: '%s' in property: '%s'", conditionKey, propertyKey);
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

    private void validateConditionOrder(ContextProperty property) {
        for (String ctxKey : domain.orderedKeys()) {
            if (contextKeyExists(property, ctxKey)) {
                for (Context ctxCondition : property.contexts()) {
                    if (contextKeyMissingFrom(ctxKey, ctxCondition) &&
                            lowerOrderContextKeyExists(ctxKey, ctxCondition)) {
                        String ctxConditionAsString = toString(ctxCondition);
                        throw missingHighOrderContextKey(ctxKey, property.key(), ctxConditionAsString);
                    }
                }
            }
        }
    }

    private boolean contextKeyExists(ContextProperty property, String ctxKey) {
        for (Context ctxCondition : property.contexts()) {
            for (Condition condition : ctxCondition.conditions()) {
                if (condition.domainKey().equals(ctxKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean contextKeyMissingFrom(String ctxKey, Context ctxCondition) {
        for (Condition condition : ctxCondition.conditions()) {
            if (condition.domainKey().equals(ctxKey)) {
                return false;
            }
        }
        return true;
    }

    private boolean lowerOrderContextKeyExists(String ctxKey, Context ctxCondition) {
        int indexOfHighOrderKey = domain.orderedKeys().indexOf(ctxKey);
        List<String> lowOrderKeys = domain.orderedKeys()
                .subList(indexOfHighOrderKey, domain.orderedKeys().size() - 1);

        for (Condition condition : ctxCondition.conditions()) {
            if (lowOrderKeys.contains(condition.domainKey())) {
                return true;
            }
        }
        return false;
    }

    private String toString(Context ctxCondition) {
        StringBuilder b = new StringBuilder();
        for (String ctxKey : domain.orderedKeys()) {
            Condition condition = findCondition(ctxKey, ctxCondition);
            if (condition != null) {
                b.append(b.length() != 0 ? "," : "");
                b.append(condition.domainKey()).append("(");
                appendValues(b, condition.values());
                b.append(")");
            }
        }
        return b.toString();
    }

    private Condition findCondition(String ctxKey, Context ctxCondition) {
        for (Condition condition : ctxCondition.conditions()) {
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
                                                                String ctxCondition) {
        String msg = String.format("High order context key: '%s' is missing from property: '%s' with context: '%s'",
                ctxKey, propertyKey, ctxCondition);
        return new IllegalArgumentException(msg);
    }

    private void validateConditionScope(ContextProperty property) {
        //TODO
    }
}
