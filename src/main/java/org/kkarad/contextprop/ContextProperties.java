package org.kkarad.contextprop;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static java.util.stream.Collectors.toConcurrentMap;

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

    public Optional<String> resolveOptString(String property, DomainPredicates predicates) {
        return Optional.ofNullable(nullableContextPropertyValue(property, predicates));
    }

    public String resolveString(String property, DomainPredicates predicates) {
        return contextPropertyValue(property, predicates);
    }

    public Optional<Boolean> resolveOptBoolean(String property, DomainPredicates predicates) {
        return Optional.ofNullable(nullableContextPropertyValue(property, predicates, TrueFalse::parse));
    }

    public boolean resolveBoolean(String property, DomainPredicates predicates) {
        return contextPropertyValue(property, predicates, TrueFalse::parse);
    }

    public Optional<Integer> resolveOptInteger(String property, DomainPredicates predicates) {
        return Optional.ofNullable(nullableContextPropertyValue(property, predicates, Integer::valueOf));
    }

    public int resolveInteger(String property, DomainPredicates predicates) {
        return contextPropertyValue(property, predicates, Integer::valueOf);
    }

    public Optional<Long> resolveOptLong(String property, DomainPredicates predicates) {
        return Optional.ofNullable(nullableContextPropertyValue(property, predicates, Long::valueOf));
    }

    public long resolveLong(String property, DomainPredicates predicates) {
        return contextPropertyValue(property, predicates, Long::valueOf);
    }

    public Optional<BigDecimal> resolveOptBigDecimal(String property, DomainPredicates predicates) {
        return Optional.ofNullable(nullableContextPropertyValue(property, predicates, BigDecimal::new));
    }

    public BigDecimal resolveBigDecimal(String property, DomainPredicates predicates) {
        return contextPropertyValue(property, predicates, BigDecimal::new);
    }

    private <T> T contextPropertyValue(String property, DomainPredicates predicates, Function<String, T> parseFunction) {
        return parseFunction.apply(contextPropertyValue(property, predicates));
    }

    private String contextPropertyValue(String property, DomainPredicates predicates) {
        ContextProperty contextProperty = contextProperty(property);
        String value = propertyResolver.resolve(contextProperty, predicates);
        if (TypedProperties.isNullOrEmpty(value)) {
            throw new PropertyNotFoundException(property, predicates);
        }
        return value;
    }

    private ContextProperty contextProperty(String property) {
        ContextProperty contextProperty = nullableContextProperty(property);
        if (contextProperty == null) {
            throw new PropertyNotFoundException(property);
        }
        return contextProperty;
    }

    private <T> T nullableContextPropertyValue(String property, DomainPredicates predicates, Function<String, T> parseFunction) {
        String value = nullableContextPropertyValue(property, predicates);
        return value != null ? parseFunction.apply(value) : null;
    }

    private String nullableContextPropertyValue(String property, DomainPredicates predicates) {
        ContextProperty contextProperty = nullableContextProperty(property);
        return contextProperty != null ? propertyResolver.resolve(contextProperty, predicates) : null;
    }

    private ContextProperty nullableContextProperty(String property) {
        return ctxProperties.get(property);
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

        private Builder(Domain domain) {
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
                    }).collect(toConcurrentMap(ContextProperty::key, (prop) -> prop));
            return new ContextProperties(propertyMap, createResolver());
        }
    }

}
