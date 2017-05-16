package org.kkarad.contextprop;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.function.Consumer;

import static java.lang.String.format;

class ContextPropertyResolver {

    private final PropertyValidator validator;

    private final PropertyResolver resolver;

    private final boolean systemPropertyOverride;

    private final Consumer<String> debugMsgResolver;

    private final ContextProperties.LogConsumer resolutionLogger;

    ContextPropertyResolver(PropertyValidator validator,
                            PropertyResolver resolver,
                            boolean systemPropertyOverride,
                            Consumer<String> debugMsgResolver,
                            ContextProperties.LogConsumer resolutionLogger) {
        this.validator = validator;
        this.resolver = resolver;
        this.systemPropertyOverride = systemPropertyOverride;
        this.debugMsgResolver = debugMsgResolver;
        this.resolutionLogger = resolutionLogger;
    }

    void resolve(Collection<ContextProperty> contextualisedProperties, Properties resolved) {
        Iterator<ContextProperty> iterator = contextualisedProperties.iterator();
        while (iterator.hasNext()) {
            ContextProperty property = iterator.next();
            validator.validate(property);
            String value = resolveProperty(iterator, property);
            if (value != null) {
                resolved.setProperty(property.key(), value);
            }
        }
    }

    private String resolveProperty(Iterator<ContextProperty> iterator, ContextProperty property) {
        debugMsgResolver.accept(format("ContextPropertyResolver.resolveProperty -> starting '%s'", property.key()));
        String value = systemPropertyOverride ? System.getProperty(property.key()) : null;
        final boolean overridden;
        if (value != null && !value.isEmpty()) {
            overridden = true;
            debugMsgResolver.accept(format("ContextPropertyResolver.resolveProperty -> system property for '%s' exists. Value resolved to '%s'",
                    property.key(), value));
        } else {
            overridden = false;
            value = resolver.resolve(property);
        }

        debugMsgResolver.accept(format("ContextPropertyResolver.resolveProperty -> finished '%s'", property.key()));
        resolutionLogger.log(property.key(), overridden, value, !iterator.hasNext());
        return value;
    }
}
