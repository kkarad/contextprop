package org.kkarad.contextprop;

import java.util.List;

import static java.lang.String.format;

public class PropertyValidator {
    private final Context context;
    private final boolean requiresDefault;

    public PropertyValidator(Context context, boolean requiresDefault) {

        this.context = context;
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
        property.propertyContexts()
                .stream()
                .flatMap(ctxCriteria -> ctxCriteria.criteria().stream())
                .map(Criterion::key)
                .forEach(criteriaKey -> {
                    if (!context.contains(criteriaKey)) {
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
        for (String ctxKey : context.orderedKeys()) {
            if (contextKeyExists(property, ctxKey)) {
                for (PropertyContext ctxCriteria : property.propertyContexts()) {
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
        for (PropertyContext ctxCriteria : property.propertyContexts()) {
            for (Criterion criterion : ctxCriteria.criteria()) {
                if (criterion.key().equals(ctxKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean contextKeyMissingFrom(String ctxKey, PropertyContext ctxCriteria) {
        for (Criterion criterion : ctxCriteria.criteria()) {
            if (criterion.key().equals(ctxKey)) {
                return false;
            }
        }
        return true;
    }

    private boolean lowerOrderContextKeyExists(String ctxKey, PropertyContext ctxCriteria) {
        int indexOfHighOrderKey = context.orderedKeys().indexOf(ctxKey);
        List<String> lowOrderKeys = context.orderedKeys()
                .subList(indexOfHighOrderKey, context.orderedKeys().size() - 1);

        for (Criterion criterion : ctxCriteria.criteria()) {
            if (lowOrderKeys.contains(criterion.key())) {
                return true;
            }
        }
        return false;
    }

    private String toString(PropertyContext ctxCriteria) {
        StringBuilder b = new StringBuilder();
        for (String ctxKey : context.orderedKeys()) {
            Criterion criterion = findCriteria(ctxKey, ctxCriteria);
            if (criterion != null) {
                b.append(b.length() != 0 ? "," : "");
                b.append(criterion.key()).append("(");
                appendValues(b, criterion.values());
                b.append(")");
            }
        }
        return b.toString();
    }

    private Criterion findCriteria(String ctxKey, PropertyContext ctxCriteria) {
        for (Criterion criterion : ctxCriteria.criteria()) {
            if (criterion.key().equals(ctxKey)) {
                return criterion;
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
