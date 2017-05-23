package org.kkarad.contextprop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.String.format;

final class PropertyResolver {

    private final Consumer<String> debugMsgResolver;

    PropertyResolver(Consumer<String> debugMsgResolver) {
        this.debugMsgResolver = debugMsgResolver;
    }

    String resolve(ContextProperty property, DomainPredicates predicates) {
        String value;
        List<Match> matches = new ArrayList<>();
        for (Context context : property.contexts()) {
            int noOfMatchedKeys = findMatches(context, predicates);
            if (noOfMatchedKeys > 0) {
                matches.add(new Match(noOfMatchedKeys, context));
            }
        }

        matches.sort((left, right) -> right.noOfKeys() - left.noOfKeys());

        if (matches.isEmpty()) {
            value = property.defaultValue();
            debugMsgResolver.accept(format("PropertyResolver.resolve -> property '%s' resolved to default value '%s'", property.key(), value));
        } else {
            Context context = matches.iterator().next().propertyContext();
            value = context.propertyValue();
            debugMsgResolver.accept(format("PropertyResolver.resolve -> property '%s' resolved to '%s' based on context (%s)",
                    property.key(), value, context));
        }
        return value;
    }

    private int findMatches(Context context, DomainPredicates predicates) {
        int matches = 0;
        for (Condition condition : context.conditions()) {
            String domainPredicate = predicates.value(condition.domainKey());
            Objects.requireNonNull(domainPredicate, "Unknown domain key: " + condition.domainKey());
            if (!condition.containsValue(domainPredicate)) {
                debugMsgResolver.accept(format("PropertyResolver.match -> condition (%s) does not match predicate (%s) (context: %s)",
                        condition, domainPredicate, context));
                return 0;
            }
            debugMsgResolver.accept(format("PropertyResolver.match -> condition (%s) matches predicate (%s) (context: %s)",
                    condition, domainPredicate, context));
            matches++;
        }
        debugMsgResolver.accept(format("PropertyResolver.match -> context (%s) matched %s times", context, matches));
        return matches;
    }
}
