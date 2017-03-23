package org.kkarad.contextprop;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Collections.disjoint;
import static org.kkarad.contextprop.Error.Type.*;

final class PropertyValidator {
    private final Domain domain;
    private final boolean requiresDefault;

    PropertyValidator(Domain domain, boolean requiresDefault) {
        this.domain = domain;
        this.requiresDefault = requiresDefault;
    }

    Optional<Error> validate(ContextProperty property) {
        return findFirst(
                () -> validateDomainKeys(property),
                () -> validateDefaultRequirement(property),
                () -> validateConditionOrder(property),
                () -> validateContextScope(property));
    }

    @SafeVarargs
    private final Optional<Error> findFirst(Supplier<Optional<Error>>... validators) {
        return Arrays.stream(validators)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<Error> validateDomainKeys(ContextProperty property) {
        Optional<String> invalidDomainKey = property.contexts()
                .stream()
                .flatMap(context -> context.conditions().stream())
                .map(Condition::domainKey)
                .filter(key -> !domain.contains(key))
                .findFirst();

        return invalidDomainKey
                .map(domainKey -> invalidDomainError(property.key(), domainKey));
    }

    private Error invalidDomainError(String propertyKey, String domainKey) {
        String msg = format("Unrecognised domain key: '%s' in property: '%s'", domainKey, propertyKey);
        return new Error(msg, INVALID_DOMAIN);
    }

    private Optional<Error> validateDefaultRequirement(ContextProperty property) {
        if (!requiresDefault) return Optional.empty();

        return property.defaultValue() == null
                ? Optional.of(missingDefaultPropertyError(property.key()))
                : Optional.empty();
    }

    private Error missingDefaultPropertyError(String propertyKey) {
        return new Error(format("default context is missing from property: '%s'", propertyKey), MISSING_DEFAULT);
    }

    private Optional<Error> validateConditionOrder(ContextProperty property) {
        for (String domainKey : domain.orderedKeys()) {
            if (domainKeyExists(property, domainKey)) {
                for (Context context : property.contexts()) {
                    if (!context.containsCondition(domainKey) &&
                            lowerOrderDomainKeyExists(context, domainKey)) {
                        return Optional.of(missingHighOrderDomainKeyError(
                                domainKey, property.key(),
                                context.toStringOrderBy(domain.orderedKeys())));
                    }
                }
            }
        }

        return Optional.empty();
    }

    private boolean domainKeyExists(ContextProperty property, String domainKey) {
        for (Context context : property.contexts()) {
            if (context.containsCondition(domainKey)) {
                return true;
            }
        }

        return false;
    }

    private boolean lowerOrderDomainKeyExists(Context context, String domainKey) {
        int indexOfHighOrderKey = domain.orderedKeys().indexOf(domainKey);
        List<String> lowOrderKeys = domain.orderedKeys()
                .subList(indexOfHighOrderKey, domain.orderedKeys().size() - 1);

        for (Condition condition : context.conditions()) {
            if (lowOrderKeys.contains(condition.domainKey())) {
                return true;
            }
        }
        return false;
    }

    private Error missingHighOrderDomainKeyError(String domainKey,
                                                 String propertyKey,
                                                 String context) {
        String msg = format("High order domain key: '%s' is missing from property: '%s' with context: '%s'",
                domainKey, propertyKey, context);
        return new Error(msg, CONDITION_ORDER_VIOLATION);
    }

    private Optional<Error> validateContextScope(ContextProperty property) {
        for (Context thisCtx : property.contexts()) {
            for (Context thatCtx : property.contexts()) {
                if (thisCtx != thatCtx && thisCtx.equalDomain(thatCtx) && contextConflictExists(thisCtx, thatCtx)) {
                    return Optional.of(contextScopeConflictError(thisCtx, thatCtx));
                }
            }
        }
        return Optional.empty();
    }

    private boolean contextConflictExists(Context thisCtx, Context thatCtx) {
        for (Condition thisCondition : thisCtx.conditions()) {
            Set<String> thisValues = thisCondition.values();
            Set<String> thatValues = thatCtx.condition(thisCondition.domainKey()).values();
            boolean commonValueExists = !disjoint(thisValues, thatValues);
            if (commonValueExists) {
                return true;
            }
        }
        return false;
    }

    private Error contextScopeConflictError(Context thisCtx, Context thatCtx) {
        String msg = format("Context scope conflict exists (contexts with the same domain keys cannot" +
                        " have common condition values) between contexts: %s, %s",
                thisCtx.toStringOrderBy(domain.orderedKeys()),
                thatCtx.toStringOrderBy(domain.orderedKeys()));
        return new Error(msg, CONTEXT_SCOPE_CONFLICT);
    }
}
