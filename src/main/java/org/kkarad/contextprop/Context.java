package org.kkarad.contextprop;

import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.singleton;
import static org.kkarad.contextprop.Assertions.assertState;

final class Context {

    private final Map<String, Condition> conditions;

    private final String propertyValue;

    private Context(Map<String, Condition> conditions, String propertyValue) {
        this.conditions = conditions;
        this.propertyValue = propertyValue;
    }

    Collection<Condition> conditions() {
        return conditions.values();
    }

    public Condition condition(String domainKey) {
        return conditions.get(domainKey);
    }

    boolean containsCondition(String domainKey) {
        return conditions.containsKey(domainKey);
    }

    boolean equalDomain(Context that) {
        return conditions.keySet().equals(that.conditions.keySet());
    }

    String propertyValue() {
        return propertyValue;
    }

    String toStringOrderBy(List<String> orderedKeys) {
        StringBuilder b = new StringBuilder();
        for (String domainKey : orderedKeys) {
            Condition condition = conditions.get(domainKey);
            if (condition != null) {
                b.append(b.length() != 0 ? "," : "");
                b.append(condition.domainKey()).append("(");
                appendValues(b, condition.values());
                b.append(")");
            }
        }
        return b.toString();
    }

    private void appendValues(StringBuilder b, Collection<String> values) {
        int offset = b.length();
        for (String value : values) {
            b.append(b.length() != offset ? "," : "").append(value);
        }
    }

    static class Builder {

        private final Map<String, Condition> conditions = new HashMap<>();

        static Builder context() {
            return new Builder();
        }

        private Builder() {
        }

        Builder condition(String domainKey, Set<String> values) {
            Condition removed = conditions.put(domainKey, new Condition(domainKey, values));
            assertState(removed == null, () -> format("condition already exists for domain key: %s", domainKey));
            return this;
        }

        Builder condition(String domainKey, String value) {
            return condition(domainKey, singleton(value));
        }

        boolean isEmpty() {
            return conditions.isEmpty();
        }

        Context getWithValue(String value) {
            return new Context(new HashMap<>(conditions), value);
        }

        void reset() {
            conditions.clear();
        }

        boolean containsCondition(String domainKey) {
            return conditions.containsKey(domainKey);
        }
    }
}
