package org.kkarad.contextprop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class PropertyResolver {

    private final DomainPredicates predicates;

    PropertyResolver(DomainPredicates predicates) {
        this.predicates = predicates;
    }

    String resolve(ContextProperty property) {
        List<Match> matches = new ArrayList<>();
        for (Context context : property.contexts()) {
            int noOfMatchedKeys = findMatches(context, predicates);
            if (noOfMatchedKeys > 0) {
                matches.add(new Match(noOfMatchedKeys, context));
            }
        }

        matches.sort((left, right) -> right.noOfKeys() - left.noOfKeys());

        return matches.isEmpty()
                ? property.defaultValue() :
                matches.iterator().next().propertyContext().propertyValue();
    }

    private int findMatches(Context context, DomainPredicates predicates) {
        int matches = 0;
        for (Condition condition : context.conditions()) {
            String domainPredicate = predicates.value(condition.domainKey());
            Objects.requireNonNull(domainPredicate, "Unknown domain key: " + condition.domainKey());
            if (!condition.containsValue(domainPredicate)) {
                return 0;
            }
            matches++;
        }
        return matches;
    }
}
