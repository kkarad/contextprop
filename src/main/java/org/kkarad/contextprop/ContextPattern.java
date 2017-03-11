package org.kkarad.contextprop;

import java.util.Arrays;

import static java.lang.String.format;

final class ContextPattern {

    private final String contextIdentifier;

    private final char startOfPattern;

    private final char endOfPattern;

    private final CriteriaPattern criteriaPattern;

    private final ParseVisitor visitor;

    private int bufferLength = 1;

    private int bufferPosition = 0;

    private char[] buffer = new char[bufferLength];

    private String errorMessage = null;

    private boolean contextIdentifierFound = false;

    private String propertyKey = null;

    private boolean endPatternFound = false;

    private boolean startPatternFound = false;

    ContextPattern(String contextIdentifier,
                   char startOfPattern,
                   char endOfPattern,
                   CriteriaPattern criteriaPattern,
                   ParseVisitor visitor) {
        this.contextIdentifier = contextIdentifier;
        this.startOfPattern = startOfPattern;
        this.endOfPattern = endOfPattern;
        this.criteriaPattern = criteriaPattern;
        this.visitor = visitor;
    }

    void startProperty(int textLength) {
        bufferLength = textLength;
        bufferPosition = 0;
        setBuffer(bufferLength);
        errorMessage = null;
        contextIdentifierFound = false;
        propertyKey = null;
        endPatternFound = false;
    }

    private void setBuffer(int length) {
        if (buffer.length < length) {
            buffer = new char[length];
        }
        Arrays.fill(buffer, '\u0000');
    }

    void traverse(char character) {
        try {
            buffer[bufferPosition] = character;
            updateState(character);
        } finally {
            bufferPosition++;
        }
    }

    private void updateState(char character) {
        if (!contextIdentifierFound) {
            if (isContextIdentifierFound() && propertyKeyIsSet()) {
                contextIdentifierFound = true;
                visitor.startProperty(propertyKey);
                criteriaPattern.startContext(propertyKey, bufferLength - bufferPosition + 1);
            }
            return;
        }

        if (!startPatternFound) {
            if (isStartOfPattern(character)) {
                startPatternFound = true;
            } else {
                errorMessage = format("Context should start with '%s'. Found '%s'", startOfPattern, character);
            }
            return;
        }

        if (!endPatternFound) {
            if (isEndOfPattern(character)) {
                endPatternFound = true;
                criteriaPattern.endContext();
            } else if (isLastCharacter()) {
                errorMessage = format("Context should end with '%s'. Found '%s'", endOfPattern, character);
            } else {
                criteriaPattern.traverse(character);
            }
        } else {
            errorMessage = format("End pattern '%s' already reached", endOfPattern);
        }
    }

    private boolean isLastCharacter() {
        return bufferPosition == bufferLength - 1;
    }

    private boolean isContextIdentifierFound() {
        if (bufferPosition >= contextIdentifier.length() - 1) {
            int offset = bufferPosition - (contextIdentifier.length() - 1);
            return findContextIdentifier(buffer, offset, contextIdentifier);
        }

        return false;
    }

    private boolean findContextIdentifier(char[] buffer, int offset, String contextIdentifier) {
        for (int i = 0; i < contextIdentifier.length(); i++) {
            if (contextIdentifier.charAt(i) != buffer[offset + i]) {
                return false;
            }
        }
        return true;
    }

    private boolean propertyKeyIsSet() {
        final int count = bufferPosition - (contextIdentifier.length() - 1);
        if (count <= 0) {
            errorMessage = format("Invalid property key. There is no property key before the context identifier ('%s')",
                    contextIdentifier);
            return false;
        }
        propertyKey = new String(buffer, 0, count);
        return true;
    }

    private boolean isStartOfPattern(char character) {
        return character == startOfPattern;
    }

    private boolean isEndOfPattern(char character) {
        return character == endOfPattern;
    }

    void endProperty(String propertyValue) {
        //is a default property?
        if (!contextIdentifierFound && !startPatternFound && !endPatternFound) {
            propertyKey = new String(buffer, 0, bufferPosition);
            if (spellingMistakeExists(propertyKey)) {
                errorMessage = "Found spelling mistake";
            } else {
                visitor.startProperty(propertyKey);
                visitor.endProperty(propertyKey, propertyValue);
            }
            return;
        }

        if (contextIdentifierFound && !(startPatternFound && endPatternFound)) {
            errorMessage = format("Context is not enclosed in '%s...%s'", startOfPattern, endOfPattern);
            return;
        }

        visitor.endProperty(propertyKey, propertyValue);
    }

    private boolean spellingMistakeExists(String propertyKey) {
        return propertyKey.contains(contextIdentifier.substring(1)) ||
                propertyKey.contains(contextIdentifier.substring(2)) ||
                propertyKey.contains(contextIdentifier.substring(0, contextIdentifier.length() - 1)) ||
                (propertyKey.contains(Character.toString(startOfPattern)) &&
                        propertyKey.contains(Character.toString(endOfPattern)));
    }

    boolean hasError() {
        return errorMessage != null || criteriaPattern.hasError();
    }

    RuntimeException exception(String keyText) {
        if (errorMessage != null) {
            return new ContextPropParseException(errorMessage + " (text:'" + keyText + "')");
        } else if (criteriaPattern.hasError()) {
            return criteriaPattern.exception(keyText);
        }
        throw new IllegalStateException("Attempt to retrieve exception when no error exists");
    }

    String propertyKey() {
        return propertyKey;
    }
}
