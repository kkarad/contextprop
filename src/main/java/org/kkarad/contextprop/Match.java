package org.kkarad.contextprop;

class Match implements Comparable<Match> {

    private final int noOfKeys;

    private final Context context;

    Match(int noOfKeys, Context context) {
        this.noOfKeys = noOfKeys;
        this.context = context;
    }

    int noOfKeys() {
        return noOfKeys;
    }

    Context propertyContext() {
        return context;
    }

    @Override
    public int compareTo(Match other) {
        return noOfKeys - other.noOfKeys;
    }
}
