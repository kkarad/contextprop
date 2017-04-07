package org.kkarad.contextprop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kkarad.contextprop.Context.Builder.context;
import static org.kkarad.contextprop.Error.Type.*;
import static org.kkarad.contextprop.JdkCollections.asSet;
import static org.kkarad.contextprop.TestBuilders.aContextProperty;

class PropertyValidatorTest {

    private DomainPredicates domainPredicates;

    @SuppressWarnings("unused")
    enum MyDomain {
        env, loc, user
    }

    private PropertyValidator validator;

    @BeforeEach
    void setUp() {

        domainPredicates = DomainPredicates.basedOnDomain(MyDomain.class)
                .predicate("env", "test")
                .predicate("loc", "uk")
                .predicate("user", "kkarad")
                .create();

        validator = new PropertyValidator(domainPredicates.domain(), false);
    }

    @Test
    @DisplayName("Context property with unknown condition key fails validation")
    void contextPropertyWithUnknownConditionKeyFailsValidation() {
        ContextProperty contextProperty = aContextProperty()
                .add(context()
                        .condition("app", "whatsapp")
                        .getWithValue("myValue"))
                .get();

        Optional<Error> validation = validator.validate(contextProperty);

        assertThat(validation).isPresent().hasValueSatisfying(error -> {
            assertThat(error.type()).isEqualTo(INVALID_DOMAIN);
        });
    }

    @Test
    @DisplayName("When default property is required a context property without default value fails validation")
    void whenDefaultPropertyIsRequiredAContextPropertyWithoutDefaultValueFailsValidation() {
        validator = new PropertyValidator(domainPredicates.domain(), true);

        ContextProperty contextProperty = aContextProperty()
                .add(context()
                        .condition("env", "test")
                        .getWithValue("myValue"))
                .get();

        Optional<Error> validation = validator.validate(contextProperty);

        assertThat(validation).isPresent().hasValueSatisfying(error ->
                assertThat(error.type()).isEqualTo(MISSING_DEFAULT));

    }

    @Test
    @DisplayName("When default property is not required a context property without default value does not fail validation")
    void name() {
        ContextProperty contextProperty = aContextProperty()
                .add(context()
                        .condition("env", "test")
                        .getWithValue("myValue"))
                .defaultValue("defaultValue")
                .get();
        Optional<Error> validation = validator.validate(contextProperty);

        assertThat(validation).isEmpty();
    }

    @Test
    @DisplayName("ContextProperties with entries which do not declare conditions declared by other entries fail validation")
    void contextPropertiesWithEntriesWhichDoNotDeclareConditionsDeclaredByOtherEntriesFailValidation() {
        ContextProperty contextProperty = aContextProperty()
                .add(context()
                        .condition("env", "test")
                        .getWithValue("value1"))
                .add(context()
                        .condition("loc", "home")
                        .getWithValue("value2"))
                .get();

        Optional<Error> validation = validator.validate(contextProperty);

        assertThat(validation).isPresent().hasValueSatisfying(error ->
                assertThat(error.type()).isEqualTo(CONDITION_ORDER_VIOLATION));

        contextProperty = aContextProperty()
                .add(context()
                        .condition("env", "test")
                        .condition("user", "george")
                        .getWithValue("value1"))
                .add(context()
                        .condition("env", "test")
                        .condition("loc", "work")
                        .getWithValue("value2"))
                .get();

        validation = validator.validate(contextProperty);

        assertThat(validation).isPresent().hasValueSatisfying(error ->
                assertThat(error.type()).isEqualTo(CONDITION_ORDER_VIOLATION));
    }

    @Test
    @DisplayName("Context properties with entries of which scope overlap fail validation")
    void contextPropertiesWithEntriesOfWhichScopeOverlapFailValidation() {
        ContextProperty contextProperty = aContextProperty()
                .add(context()
                        .condition("env", "test")
                        .condition("loc", "home")
                        .getWithValue("value1"))
                .add(context()
                        .condition("env", "real")
                        .condition("loc", asSet("home", "work", "gym")) //home is already defined
                        .getWithValue("value2"))
                .get();

        Optional<Error> validation = validator.validate(contextProperty);
        assertThat(validation).isPresent().hasValueSatisfying(error ->
                assertThat(error.type()).isEqualTo(CONTEXT_SCOPE_CONFLICT));
    }


}