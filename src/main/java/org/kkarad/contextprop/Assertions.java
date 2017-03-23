package org.kkarad.contextprop;

import java.util.function.Supplier;

interface Assertions {
    static void assertState(boolean condition, Supplier<String> message) {
        if (!condition) {
            throw new IllegalStateException(message.get());
        }
    }
}
