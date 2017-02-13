package org.kkarad.contextprop;

class Match implements Comparable<Match> {

    private final int noOfKeys;

    private final PropertyContext propertyContext;

    public Match(int noOfKeys, PropertyContext propertyContext) {
        this.noOfKeys = noOfKeys;
        this.propertyContext = propertyContext;
    }

    public int noOfKeys() {
        return noOfKeys;
    }

    public PropertyContext propertyContext() {
        return propertyContext;
    }

    @Override
    public int compareTo(Match other) {
        return noOfKeys - other.noOfKeys;
    }
}
