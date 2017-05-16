package org.kkarad.contextprop.examples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kkarad.contextprop.ContextProperties;
import org.kkarad.contextprop.DomainPredicates;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ApiTest {

    @SuppressWarnings("unused")
    public enum MyDomain {
        env, loc, group, app, host, user
    }

    @Test
    @DisplayName("Basic usage")
    void basic_usage() {
        Properties ctxProperties = new Properties();
        ctxProperties.setProperty("my.prop.key.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "myValue");
        ctxProperties.setProperty("my.prop.key", "defaultValue");
        ctxProperties.setProperty("my.prop.to.be.overridden", "value");

        System.setProperty("my.prop.to.be.overridden", "overriddenValue");

        DomainPredicates predicates = DomainPredicates.basedOnDomain(MyDomain.class)
                .predicate("env", "uat")
                .predicate("loc", "ldn")
                .predicate("group", "internal")
                .predicate("app", "whatsapp")
                .predicate("host", "localhost")
                .predicate("user", "kkarad")
                .create();

        Properties properties = ContextProperties.create(predicates)
                .requiresDefault()
                .allowSystemPropertyOverride()
                .debugParser(System.out::println)
                .debugResolver(System.out::println)
                .logResolution((property, overridden, value, isLast) ->
                        System.out.format("(%s) %s -> %s", overridden ? "sys " : "prop", property, value))
                .resolve(ctxProperties);

        assertThat(properties.getProperty("my.prop.key")).isEqualTo("myValue");
    }
}
