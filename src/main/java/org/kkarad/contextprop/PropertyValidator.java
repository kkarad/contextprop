package org.kkarad.contextprop;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;

final class PropertyValidator {
    private final Domain domain;
    private final boolean requiresDefault;

    PropertyValidator(Domain domain, boolean requiresDefault) {
        this.domain = domain;
        this.requiresDefault = requiresDefault;
    }

    Optional<Error> validate(ContextProperty property) {
        return findFirst(
                () -> validateConditionKeys(property),
                () -> validateDefaultRequirement(property),
                () -> validateConditionOrder(property),
                () -> validateConditionScope(property));
    }

    @SafeVarargs
    private final Optional<Error> findFirst(Supplier<Optional<Error>>... validators) {
        return Arrays.stream(validators)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<Error> validateConditionKeys(ContextProperty property) {
        Optional<String> invalidConditionKey = property.contexts()
                .stream()
                .flatMap(ctxCondition -> ctxCondition.conditions().stream())
                .map(Condition::domainKey)
                .filter(conditionKey -> !domain.contains(conditionKey))
                .findFirst();

        return invalidConditionKey
                .map(conditionKey -> invalidConditionKey(property.key(), conditionKey));
    }

    private Error invalidConditionKey(String propertyKey, String conditionKey) {
        String msg = format("Unrecognised condition key: '%s' in property: '%s'", conditionKey, propertyKey);
        return new Error(msg);
    }

    private Optional<Error> validateDefaultRequirement(ContextProperty property) {
        if (requiresDefault) return Optional.empty();

        return property.defaultValue() == null
                ? Optional.of(missingDefaultProperty(property.key()))
                : Optional.empty();
    }

    private Error missingDefaultProperty(String propertyKey) {
        return new Error(format("default context is missing from property: '%s'", propertyKey));
    }

    private Optional<Error> validateConditionOrder(ContextProperty property) {
        for (String ctxKey : domain.orderedKeys()) {
            if (contextKeyExists(property, ctxKey)) {
                for (Context ctxCondition : property.contexts()) {
                    if (contextKeyMissingFrom(ctxKey, ctxCondition) &&
                            lowerOrderContextKeyExists(ctxKey, ctxCondition)) {
                        String ctxConditionAsString = toString(ctxCondition);
                        return Optional.of(missingHighOrderContextKey(ctxKey, property.key(), ctxConditionAsString));
                    }
                }
            }
        }

        return Optional.empty();
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

    private Error missingHighOrderContextKey(String ctxKey,
                                             String propertyKey,
                                             String ctxCondition) {
        String msg = format("High order context key: '%s' is missing from property: '%s' with context: '%s'",
                ctxKey, propertyKey, ctxCondition);
        return new Error(msg);
    }

    private Optional<Error> validateConditionScope(ContextProperty property) {
        //TODO
        return Optional.empty();
    }
}
