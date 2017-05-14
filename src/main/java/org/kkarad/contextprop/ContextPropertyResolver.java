package org.kkarad.contextprop;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

class ContextPropertyResolver {

    private final PropertyValidator validator;

    private final PropertyResolver resolver;

    private final ContextProperties.LogConsumer resolutionLogger;

    ContextPropertyResolver(PropertyValidator validator, PropertyResolver resolver, ContextProperties.LogConsumer resolutionLogger) {
        this.validator = validator;
        this.resolver = resolver;
        this.resolutionLogger = resolutionLogger;
    }

    void resolve(Collection<ContextProperty> contextualisedProperties, Properties resolved) {
        Iterator<ContextProperty> iterator = contextualisedProperties.iterator();
        while (iterator.hasNext()) {
            ContextProperty property = iterator.next();
            validator.validate(property);
            String value = resolver.resolve(property);
            resolutionLogger.log(property.key(), value, !iterator.hasNext());
            if (value != null) {
                resolved.setProperty(property.key(), value);
            }
        }
    }
}
