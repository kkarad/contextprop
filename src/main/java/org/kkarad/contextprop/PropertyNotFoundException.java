package org.kkarad.contextprop;

public final class PropertyNotFoundException extends RuntimeException {
    PropertyNotFoundException(String property) {
        super(String.format("'%s'", property));
    }
}
