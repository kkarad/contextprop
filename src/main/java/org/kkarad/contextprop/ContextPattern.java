package org.kkarad.contextprop;

import java.util.Arrays;

import static java.lang.String.format;

final class ContextPattern {

    private final String startOfPattern;

    private final char endOfPattern;

    private final CriteriaPattern criteriaPattern;

    private final ParseVisitor visitor;

    private int bufferLength = 1;

    private int bufferPosition = 0;

    private char[] buffer = new char[bufferLength];

    private String errorMessage = null;

    private boolean startPatternFound = false;

    private String propertyKey = null;

    private boolean endPatternFound = false;

    public ContextPattern(String startOfPattern, char endOfPattern, CriteriaPattern criteriaPattern, ParseVisitor visitor) {
        this.startOfPattern = startOfPattern;
        this.endOfPattern = endOfPattern;
        this.criteriaPattern = criteriaPattern;
        this.visitor = visitor;
    }

    public void startProperty(int textLength) {
        bufferLength = textLength;
        bufferPosition = 0;
        setBuffer(bufferLength);
        errorMessage = null;
        startPatternFound = false;
        propertyKey = null;
        endPatternFound = false;
    }

    private void setBuffer(int length) {
        if (buffer.length < length) {
            buffer = new char[length];
        }
        Arrays.fill(buffer, '\u0000');
    }

    public void traverse(char character) {
        try {
            buffer[bufferPosition] = character;
            updateState(character);
        } finally {
            bufferPosition++;
        }
    }

    private void updateState(char character) {
        boolean startPatternFoundNow = !startPatternFound && isStartOfPattern();
        if (startPatternFoundNow && propertyKeyIsSet()) {
            visitor.startProperty(propertyKey);
            criteriaPattern.startContext(propertyKey, bufferLength - bufferPosition + 1);
        }

        endPatternFound = startPatternFound && (endPatternFound || isEndOfPattern(character));

        if (!startPatternFoundNow && startPatternFound && !endPatternFound) {
            criteriaPattern.traverse(character);
        }

        if (endPatternFound) {
            criteriaPattern.endContext();
        }
    }

    private boolean isStartOfPattern() {
        if (bufferPosition >= startOfPattern.length() - 1) {
            int offset = bufferPosition - (startOfPattern.length() - 1);
            startPatternFound = findStartPattern(buffer, offset, startOfPattern);
            return startPatternFound;
        }

        return false;
    }

    private boolean findStartPattern(char[] buffer, int offset, String startOfPattern) {
        for (int i = 0; i < startOfPattern.length(); i++) {
            if (startOfPattern.charAt(i) != buffer[offset + i]) {
                return false;
            }
        }
        return true;
    }

    private boolean propertyKeyIsSet() {
        final int count = bufferPosition - (startOfPattern.length() - 1);
        if (count <= 0) {
            errorMessage = format("Invalid property key. There is no property key before the '%s' context pattern",
                    startOfPattern);
            return false;
        }
        propertyKey =  new String(buffer, 0, count);
        return true;
    }

    private boolean isEndOfPattern(char character) {
        boolean endOfLengthReached = bufferPosition == bufferLength - 1;
        return endOfLengthReached && (character == endOfPattern);
    }

    public void endProperty(String propertyValue) {
        if (startPatternFound && propertyKey == null) {
            errorMessage = format("Unable to recognise the '%s...%s' context pattern",
                    startOfPattern, Character.toString(endOfPattern));
            return;
        }

        if (startPatternFound && !endPatternFound) {
            errorMessage = format("Property key doesn't end with the context end pattern '%s'", Character.toString(endOfPattern));
            return;
        }

        if (!startPatternFound && !endPatternFound) {
            propertyKey = new String(buffer, 0, bufferPosition);
            visitor.startProperty(propertyKey);
            visitor.endProperty(propertyKey, propertyValue);
        }

    }

    public boolean hasError() {
        return errorMessage != null || criteriaPattern.hasError();
    }

    public RuntimeException exception(String keyText) {
        if (errorMessage != null) {
            return new IllegalArgumentException(errorMessage + " (text:'" + keyText + "')");
        } else if (criteriaPattern.hasError()) {
            return criteriaPattern.exception(keyText);
        }
        throw new IllegalStateException("Attempt to retrieve exception when no error exists");
    }

    public String propertyKey() {
        return propertyKey;
    }
}
