package org.kkarad.contextprop;

final class Error {

    enum Type {
        INVALID_DOMAIN, MISSING_DEFAULT, CONDITION_ORDER_VIOLATION, CONTEXT_SCOPE_CONFLICT
    }

    private final String message;

    private final Type type;

    Error(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    public String message() {
        return message;
    }

    Type type() {
        return type;
    }

    @Override
    public String toString() {
        return type + "(" + message + ")";
    }
}
