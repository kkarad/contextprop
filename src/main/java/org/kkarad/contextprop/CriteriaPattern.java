package org.kkarad.contextprop;

import java.util.Arrays;

import static java.lang.String.format;

//                 0         1         2         3
//                 0123456789012345678901234567890123
//my.prop.key.CTXT[env(uat),loc(ldn),group(internal)]
final class CriteriaPattern {

    private final char startOfPattern;

    private final char endOfPattern;

    private final String valueDelimiter;

    private final ParserVisitor visitor;
    private final char criteriaDelimiter;

    private int bufferLength = 1;

    private int bufferPosition = 0;

    private char[] buffer = new char[bufferLength];

    private String propertyKey;

    private int criteriaStartIndex = -1;

    private String criteriaKey = null;

    private int valueStartIndex = -1;

    private boolean criteriaComplete = false;

    private String errorMessage = null;

    public CriteriaPattern(char startOfPattern, char endOfPattern, ParserVisitor visitor, String valueDelimiter, char criteriaDelimiter) {
        this.startOfPattern = startOfPattern;
        this.endOfPattern = endOfPattern;
        this.visitor = visitor;
        this.valueDelimiter = valueDelimiter;
        this.criteriaDelimiter = criteriaDelimiter;
    }

    public void startContext(String propertyKey, int textLength) {
        resetContextState(propertyKey, textLength);
        resetCriteriaState(0);
        errorMessage = null;
    }

    private void resetContextState(String propertyKey, int textLength) {
        bufferLength = textLength;
        bufferPosition = 0;
        setBuffer(bufferLength);
        this.propertyKey = propertyKey;
    }

    private void setBuffer(int length) {
        if (buffer.length < length) {
            buffer = new char[length];
        }
        Arrays.fill(buffer, '\u0000');
    }

    private void resetCriteriaState(int position) {
        criteriaStartIndex = position;
        criteriaKey = null;
        valueStartIndex = -1;
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
        if (criteriaComplete && character == criteriaDelimiter) {
            criteriaComplete = false;
            resetCriteriaState(bufferPosition + 1);
        } else if (character == startOfPattern) {
            onStartOfPattern();
        } else if (character == endOfPattern) {
            onEndOfPattern();
        }
    }

    private void onStartOfPattern() {
        int count = bufferPosition - criteriaStartIndex;
        if (count <= 0) {
            errorMessage = format("There is no criteria key before the '%s' criteria start pattern", startOfPattern);
            return;
        }

        criteriaKey = new String(buffer, criteriaStartIndex, count);

        valueStartIndex = bufferPosition + 1;
        if (valueStartIndex == bufferLength) {
            errorMessage = format("End of text reached without recognising values for criteria key: '%s'", criteriaKey);
        }
    }

    private void onEndOfPattern() {
        if (criteriaKey == null) {
            errorMessage = format("Reached end of criteria end pattern '%s' without recognising the criteria key", endOfPattern);
            return;
        }

        int count = bufferPosition - valueStartIndex;
        if (count <= 0) {
            errorMessage = format("No values found for criteria key: '%s'", criteriaKey);
            return;
        }

        String valueText = new String(buffer, valueStartIndex, count);
        String[] values = valueText.split(valueDelimiter);
        if (validate(criteriaKey, values)) {
            visitor.propertyCriteria(propertyKey, criteriaKey, values);
            criteriaComplete = true;
        }
    }

    private boolean validate(String criteriaKey, String[] values) {
        for (String value : values) {
            if (value.isEmpty()) {
                errorMessage = format("Invalid value ('%s') assigned to criteria key: '%s'", value, criteriaKey);
                return false;
            }
        }

        return true;
    }

    public void endContext() {
        if (!criteriaComplete) {
            String text = new String(buffer, criteriaStartIndex, bufferPosition - criteriaStartIndex);
            errorMessage = format("Reached end of context of property key (%s) without recognising criteria from text: '%s'",
                    propertyKey, text);
        }
    }

    public boolean hasError() {
        return errorMessage != null;
    }

    public RuntimeException exception(String keyText) {
        if (errorMessage != null) {
            return new IllegalArgumentException(errorMessage + " (text: '" + keyText + "')");
        }
        throw new IllegalStateException("Attempt to retrieve exception when no error exists");
    }
}
