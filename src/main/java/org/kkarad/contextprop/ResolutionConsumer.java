package org.kkarad.contextprop;

@FunctionalInterface
public interface ResolutionConsumer {
    void onResolution(String property, boolean systemOverride, String value, boolean isLast);
}
