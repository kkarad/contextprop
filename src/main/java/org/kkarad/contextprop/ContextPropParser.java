package org.kkarad.contextprop;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class ContextPropParser {

    private static final String CONTEXT_START_PATTERN = ".CTXT[";

    private static final char CONTEXT_END_PATTERN = ']';

    private static final char CRITERIA_VALUE_START_PATTERN = '(';

    private static final char CRITERIA_VALUE_END_PATTERN = ')';

    private static final char CRITERIA_DELIMITER = ',';

    private static final String CRITERIA_VALUE_DELIMITER = ",";

    private final ContextVisitor visitor = new ContextVisitor();

    private final ParseVisitor logVisitor = new LogAndParseVisitor(
            true,
            visitor);

    private final ContextPattern contextPattern = new ContextPattern(
            CONTEXT_START_PATTERN,
            CONTEXT_END_PATTERN,
            new CriteriaPattern(
                    CRITERIA_VALUE_START_PATTERN,
                    CRITERIA_VALUE_END_PATTERN,
                    logVisitor,
                    CRITERIA_VALUE_DELIMITER,
                    CRITERIA_DELIMITER),
            logVisitor);

    private final Context context;

    private boolean requiresDefault = false;

    public static ContextPropParser parser(Context context) {
        return new ContextPropParser(context);
    }

    private ContextPropParser(Context context) {
        this.context = context;
    }

    public ContextPropParser requiresDefault(boolean requiresDefault) {
        this.requiresDefault = requiresDefault;
        return this;
    }

    public Properties parse(Properties contextProperties) {
        Map<String, String> propertyMap = toPropertyMap(contextProperties);
        logVisitor.startParse();
        for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
            String keyText = entry.getKey();
            parseProperty(keyText, contextPattern, entry.getValue());
        }
        logVisitor.endParse();

        Properties resolved = new Properties();
        PropertyResolver propertyResolver = new PropertyResolver(context, requiresDefault);
        visitor.properties().forEach(property ->
                resolved.setProperty(
                        property.key(),
                        propertyResolver.resolve(property)));

        return resolved;
    }

    private String parseProperty(String keyText, ContextPattern contextPattern, String value) {
        contextPattern.startProperty(keyText.length());
        for (int i = 0; i < keyText.length(); i++) {
            char character = keyText.charAt(i);
            System.out.println("character = " + character);
            contextPattern.traverse(character);
            if (contextPattern.hasError()) {
                throw contextPattern.exception(keyText);
            }
        }
        contextPattern.endProperty(value);

        if (contextPattern.hasError()) {
            throw contextPattern.exception(keyText);
        }
        return contextPattern.propertyKey();
    }

    private Map<String, String> toPropertyMap(Properties contextProperties) {
        Map<String, String> normalised = new HashMap<>(contextProperties.size());
        for (String key : contextProperties.stringPropertyNames()) {
            String value = contextProperties.getProperty(key);
            normalised.put(key, value);
        }
        return normalised;
    }
}
