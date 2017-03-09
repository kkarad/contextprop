package org.kkarad.contextprop;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

final class PropertyParser {

    private ParseVisitor visitor;

    private ContextPattern contextPattern;

    PropertyParser(ParseVisitor visitor, ContextPattern contextPattern) {
        this.visitor = visitor;
        this.contextPattern = contextPattern;
    }

    Collection<ContextProperty> parse(Properties unresolved) {
        Map<String, String> propertyMap = toPropertyMap(unresolved);
        visitor.startParse();
        for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
            String keyText = entry.getKey();
            parseProperty(keyText, contextPattern, entry.getValue());
        }
        visitor.endParse();

        return visitor.properties();
    }

    private String parseProperty(String keyText, ContextPattern contextPattern, String value) {
        contextPattern.startProperty(keyText.length());
        for (int i = 0; i < keyText.length(); i++) {
            char character = keyText.charAt(i);
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
