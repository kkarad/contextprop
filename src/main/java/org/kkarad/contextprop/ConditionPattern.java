package org.kkarad.contextprop;

import java.util.Arrays;
import java.util.regex.Pattern;

import static java.lang.String.format;

//                 0         1         2         3
//                 0123456789012345678901234567890123
//my.prop.key.CTXT(env[uat],loc[ldn],group[internal])
final class ConditionPattern {

    private final char startOfPattern;

    private final char endOfPattern;

    private final String valueDelimiter;

    private final ParseVisitor visitor;

    private final char conditionDelimiter;

    private int bufferLength = 1;

    private int bufferPosition = 0;

    private char[] buffer = new char[bufferLength];

    private String propertyKey;

    private int conditionStartIndex = -1;

    private String conditionKey = null;

    private int valueStartIndex = -1;

    private boolean conditionComplete = false;

    private String errorMessage = null;

    ConditionPattern(char startOfPattern, char endOfPattern, ParseVisitor visitor, String valueDelimiter, char conditionDelimiter) {
        this.startOfPattern = startOfPattern;
        this.endOfPattern = endOfPattern;
        this.visitor = visitor;
        this.valueDelimiter = Pattern.quote(valueDelimiter);
        this.conditionDelimiter = conditionDelimiter;
    }

    void startContext(String propertyKey, int textLength) {
        resetContextState(propertyKey, textLength);
        resetConditionState(0);
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

    private void resetConditionState(int position) {
        conditionStartIndex = position;
        conditionKey = null;
        conditionComplete = false;
        valueStartIndex = -1;
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
        if (conditionComplete) {
            onStartOfNextCondition(character);
        } else if (character == startOfPattern) {
            onStartOfPattern();
        } else if (character == endOfPattern) {
            onEndOfPattern();
        }
    }

    private void onStartOfNextCondition(char character) {
        if (character == conditionDelimiter) {
            resetConditionState(bufferPosition + 1);
        } else {
            String previousCondition = new String(buffer, 0, bufferPosition);
            errorMessage = format("Condition should be delimited with '%s'. Expecting delimiter after '%s'",
                    conditionDelimiter, previousCondition);
        }
    }

    private void onStartOfPattern() {
        int count = bufferPosition - conditionStartIndex;
        if (count <= 0) {
            errorMessage = format("There is no condition key before the '%s' condition start pattern", startOfPattern);
            return;
        }

        conditionKey = new String(buffer, conditionStartIndex, count);

        valueStartIndex = bufferPosition + 1;
        if (valueStartIndex == bufferLength) {
            errorMessage = format("End of text reached without recognising values for condition key: '%s'", conditionKey);
        }
    }

    private void onEndOfPattern() {
        if (conditionKey == null) {
            errorMessage = format("Reached end of condition end pattern '%s' without recognising the condition key", endOfPattern);
            return;
        }

        int count = bufferPosition - valueStartIndex;
        if (count <= 0) {
            errorMessage = format("No values found for condition key: '%s'", conditionKey);
            return;
        }

        String valueText = new String(buffer, valueStartIndex, count);
        String[] values = valueText.split(valueDelimiter);
        if (validate(conditionKey, values)) {
            visitor.propertyCondition(propertyKey, conditionKey, values);
            conditionComplete = true;
        }
    }

    private boolean validate(String conditionKey, String[] values) {
        for (String value : values) {
            if (value.isEmpty()) {
                errorMessage = format("Invalid value ('%s') assigned to condition key: '%s'", value, conditionKey);
                return false;
            }
        }

        return true;
    }

    void endContext() {
        if (!conditionComplete) {
            String text = new String(buffer, conditionStartIndex, bufferPosition - conditionStartIndex);
            errorMessage = format("Reached end of context of property key (%s) without recognising condition from '%s'",
                    propertyKey, text);
        }
    }

    boolean hasError() {
        return errorMessage != null;
    }

    RuntimeException exception(String keyText) {
        if (errorMessage != null) {
            return new ContextPropParseException(format("%s (" +
                            "text: '%s', " +
                            "start condition pattern: '%s', " +
                            "end condition pattern: '%s')",
                    errorMessage, keyText, startOfPattern, endOfPattern));
        }

        throw new IllegalStateException("Attempt to retrieve exception when no error exists");
    }
}
