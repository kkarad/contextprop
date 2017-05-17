package org.kkarad.contextprop;

@FunctionalInterface
public interface LogConsumer {
    void log(String property, boolean systemOverride, String value, boolean isLast);
}
