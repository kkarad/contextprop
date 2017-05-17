package org.kkarad.contextprop;

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public final class ContextProperties {

    private final ConcurrentMap<String, ContextProperty> ctxProperties;

    private final ContextPropertyResolver propertyResolver;

    public static Resolver basedOn(DomainPredicates predicates) {
        return new Resolver(predicates);
    }

    public static <E extends Enum> Builder basedOnDomain(Class<E> domainClass) {
        return new Builder(Domain.create(domainClass));
    }

    private ContextProperties(ConcurrentMap<String, ContextProperty> ctxProperties,
                              ContextPropertyResolver propertyResolver) {
        this.ctxProperties = ctxProperties;
        this.propertyResolver = propertyResolver;
    }

    public String resolveString(String property, DomainPredicates predicates) {
        return propertyResolver.resolve(ctxProperties.get(property), predicates);
    }

    public static final class Resolver extends AbstractBuilder<Resolver> {

        private final DomainPredicates predicates;

        private Resolver(DomainPredicates predicates) {
            this.predicates = predicates;
        }

        public Properties resolve(Properties ctxProperties) {
            Properties resolved = new Properties();
            Collection<ContextProperty> contextProperties = createParser().parse(ctxProperties);
            PropertyValidator validator = createValidator(predicates.domain());
            contextProperties.forEach(validator::validate);
            createResolver().resolve(contextProperties, predicates, resolved);
            return resolved;
        }

        public TypedProperties resolveTyped(Properties ctxProperties) {
            Properties properties = resolve(ctxProperties);
            return new TypedProperties(properties);
        }
    }

    public static final class Builder extends AbstractBuilder<Builder> {

        private final Domain domain;

        Builder(Domain domain) {
            this.domain = domain;
        }

        public ContextProperties create(Properties unresolved) {
            final PropertyValidator validator = createValidator(domain);
            ConcurrentMap<String, ContextProperty> propertyMap = createParser()
                    .parse(unresolved)
                    .stream()
                    .map(property -> {
                        validator.validate(property);
                        return property;
                    }).collect(Collectors.toConcurrentMap(ContextProperty::key, (prop) -> prop));
            return new ContextProperties(propertyMap, createResolver());
        }
    }

}
