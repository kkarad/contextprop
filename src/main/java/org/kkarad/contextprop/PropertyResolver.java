package org.kkarad.contextprop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class PropertyResolver {

    private final Context context;

    public PropertyResolver(Context context) {
        this.context = context;
    }

    String resolve(ContextProperty property) {
        List<Match> matches = new ArrayList<>();
        for (PropertyContext propertyContext : property.propertyContexts()) {
            int noOfMatchedKeys = findMatches(propertyContext, context);
            if (noOfMatchedKeys > 0) {
                matches.add(new Match(noOfMatchedKeys, propertyContext));
            }
        }

        matches.sort((left, right) -> right.noOfKeys() - left.noOfKeys());

        return matches.isEmpty()
                ? property.defaultValue() :
                matches.iterator().next().propertyContext().value();
    }

    private int findMatches(PropertyContext propertyContext, Context context) {
        int matches = 0;
        for (Criterion criterion : propertyContext.criteria()) {
            String contextValue = context.value(criterion.key());
            Objects.requireNonNull(contextValue, "Unknown context key: " + criterion.key());
            if (!criterion.values().contains(contextValue)) {
                return -1;
            }
            matches++;
        }
        return matches;
    }

}
