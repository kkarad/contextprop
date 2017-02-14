package org.kkarad.contextprop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;

public final class DomainPredicates {

    private final Domain domain;

    private final Map<String, String> predicates;

    public static <E extends Enum> Builder basedOnDomain(Class<E> domainClass) {
        return new Builder(Domain.create(domainClass));
    }

    private static <E extends Enum> List<String> toOrderedList(Class<E> keys) {
        return unmodifiableList(stream(keys.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList()));
    }

    private DomainPredicates(Domain domain, Map<String, String> predicates) {
        this.domain = domain;
        this.predicates = predicates;
    }

    public Domain domain() {
        return domain;
    }

    public String value(String key) {
        return predicates.get(key);
    }


    public static class Builder {

        private final Domain domain;

        private final HashMap<String, String> predicates = new HashMap<>();


        private Builder(Domain domain) {
            this.domain = domain;
        }

        public Builder predicate(String key, String value) {
            if (!domain.contains(key)) {
                throw new IllegalArgumentException(format("Predicate: %s can't be found in domain (%s)", key, domain));
            }
            predicates.put(key, value);
            return this;
        }

        public DomainPredicates create() {
            if (predicates.size() != domain.numberOfKeys()) {
                String msg = format("Predicates don't cover all domain keys (predicates: %s, domain: %s)",
                        predicates.keySet(), domain);
                throw new IllegalArgumentException(msg);
            }
            return new DomainPredicates(domain, predicates);
        }
    }
}
