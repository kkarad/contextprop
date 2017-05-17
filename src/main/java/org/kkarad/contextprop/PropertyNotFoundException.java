package org.kkarad.contextprop;

import static java.lang.String.format;

public final class PropertyNotFoundException extends RuntimeException {
    PropertyNotFoundException(String property) {
        super(format("'%s'", property));
    }

    PropertyNotFoundException(String property, DomainPredicates predicates) {
        super(format("'%s'; for context (%s)", property, predicates));
    }
}
