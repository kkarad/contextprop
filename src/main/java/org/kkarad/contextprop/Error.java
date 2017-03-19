package org.kkarad.contextprop;

final class Error {

    private final String message;

    Error(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
