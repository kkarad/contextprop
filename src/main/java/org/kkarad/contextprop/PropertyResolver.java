package org.kkarad.contextprop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.String.format;

final class PropertyResolver {

    private final DomainPredicates predicates;

    private final Consumer<String> debugMsgResolver;

    PropertyResolver(DomainPredicates predicates, Consumer<String> debugMsgResolver) {
        this.predicates = predicates;
        this.debugMsgResolver = debugMsgResolver;
    }

    String resolve(ContextProperty property) {
        debugMsgResolver.accept(format("PropertyResolver.startResolve -> %s", property.key()));
        List<Match> matches = new ArrayList<>();
        for (Context context : property.contexts()) {
            int noOfMatchedKeys = findMatches(context, predicates);
            if (noOfMatchedKeys > 0) {
                matches.add(new Match(noOfMatchedKeys, context));
            }
        }

        matches.sort((left, right) -> right.noOfKeys() - left.noOfKeys());

        String value = matches.isEmpty()
                ? property.defaultValue() :
                matches.iterator().next().propertyContext().propertyValue();

        debugMsgResolver.accept(format("PropertyResolver.endResolve -> %s", property.key()));
        return value;
    }

    private int findMatches(Context context, DomainPredicates predicates) {
        int matches = 0;
        for (Condition condition : context.conditions()) {
            String domainPredicate = predicates.value(condition.domainKey());
            Objects.requireNonNull(domainPredicate, "Unknown domain key: " + condition.domainKey());
            if (!condition.containsValue(domainPredicate)) {
                debugMsgResolver.accept(format("PropertyResolver.findMatch -> condition (%s) does not match with context (%s)", condition, context));
                return 0;
            }
            debugMsgResolver.accept(format("PropertyResolver.findMatch -> condition (%s) matches with context (%s)", condition, context));
            matches++;
        }
        return matches;
    }
}
