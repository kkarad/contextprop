package org.kkarad.contextprop;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Stream;

class ContextPropertyResolver {

    private PropertyValidator validator;

    private PropertyResolver resolver;

    ContextPropertyResolver(PropertyValidator validator, PropertyResolver resolver) {
        this.validator = validator;
        this.resolver = resolver;
    }

    void resolve(Collection<ContextProperty> contextualisedProperties, Properties resolved) {
        contextualisedProperties.forEach(property -> {
            validator.validate(property);
            String value = resolver.resolve(property);
            if (value != null) {
                resolved.setProperty(property.key(), value);
            }
        });
    }
}
