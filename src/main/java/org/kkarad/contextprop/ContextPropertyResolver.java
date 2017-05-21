package org.kkarad.contextprop;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.function.Consumer;

import static java.lang.String.format;

class ContextPropertyResolver {

    private final PropertyResolver resolver;

    private final boolean systemPropertyOverride;

    private final Consumer<String> debugMsgResolver;

    private final ResolutionConsumer resolutionLogger;

    ContextPropertyResolver(PropertyResolver resolver,
                            boolean systemPropertyOverride,
                            Consumer<String> debugMsgResolver,
                            ResolutionConsumer resolutionLogger) {
        this.resolver = resolver;
        this.systemPropertyOverride = systemPropertyOverride;
        this.debugMsgResolver = debugMsgResolver;
        this.resolutionLogger = resolutionLogger;
    }

    void resolve(Collection<ContextProperty> contextualisedProperties, DomainPredicates predicates, Properties resolved) {
        Iterator<ContextProperty> iterator = contextualisedProperties.iterator();
        while (iterator.hasNext()) {
            ContextProperty property = iterator.next();
            String value = resolve(property, predicates, !iterator.hasNext());
            if (value != null) {
                resolved.setProperty(property.key(), value);
            }
        }
    }

    String resolve(ContextProperty property, DomainPredicates predicates) {
        return resolve(property, predicates, true);
    }

    private String resolve(ContextProperty property, DomainPredicates predicates, boolean isLast) {
        debugMsgResolver.accept(format("ContextPropertyResolver.resolve -> starting '%s'", property.key()));
        String value = systemPropertyOverride ? System.getProperty(property.key()) : null;
        final boolean overridden;
        if (value != null && !value.isEmpty()) {
            overridden = true;
            debugMsgResolver.accept(format("ContextPropertyResolver.resolve -> system property for '%s' exists. Value resolved to '%s'",
                    property.key(), value));
        } else {
            overridden = false;
            value = resolver.resolve(property, predicates);
        }

        debugMsgResolver.accept(format("ContextPropertyResolver.resolve -> finished '%s'", property.key()));
        resolutionLogger.onResolution(property.key(), overridden, value, isLast);
        return value;
    }
}
