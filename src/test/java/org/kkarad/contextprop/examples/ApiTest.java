package org.kkarad.contextprop.examples;

import org.junit.jupiter.api.Test;
import org.kkarad.contextprop.ContextProperties;
import org.kkarad.contextprop.DomainPredicates;
import org.kkarad.contextprop.TypedProperties;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ApiTest {

    @SuppressWarnings("unused")
    public enum MyDomain {
        env, loc, group, app, host, user
    }

    @Test
    void resolveContextPropertiesToJdkProperties() {
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

        Properties properties = ContextProperties.basedOn(predicates)
                .requiresDefault()
                .allowSystemPropertyOverride()
                .debugParser(System.out::println)
                .debugResolver(System.out::println)
                .logResolution((property, systemOverride, value, isLast) ->
                        System.out.format("(%s) %s -> %s", systemOverride ? "sys " : "prop", property, value))
                .resolve(ctxProperties);

        assertThat(properties.getProperty("my.prop.key")).isEqualTo("myValue");
    }

    @Test
    void resolveContextPropertiesToTypedProperties() {
        Properties ctxProperties = new Properties();
        ctxProperties.setProperty("my.prop.key.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "true");
        ctxProperties.setProperty("my.prop.key", "false");

        DomainPredicates predicates = DomainPredicates.basedOnDomain(MyDomain.class)
                .predicate("env", "uat")
                .predicate("loc", "ldn")
                .predicate("group", "internal")
                .predicate("app", "whatsapp")
                .predicate("host", "localhost")
                .predicate("user", "kkarad")
                .create();

        TypedProperties properties = ContextProperties.basedOn(predicates)
                .logResolution((property, systemOverride, value, isLast) ->
                        System.out.format("(%s) %s -> %s%n", systemOverride ? "sys " : "prop", property, value))
                .resolveTyped(ctxProperties);

        assertThat(properties.getBoolean("my.prop.key")).isTrue();
    }

    @Test
    void createContextPropertiesAndResolveOnDemand() {
        Properties ctxProperties = new Properties();
        ctxProperties.setProperty("my.prop.key.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "myValue");
        ctxProperties.setProperty("my.prop.key", "defaultValue");

        ContextProperties properties = ContextProperties.basedOnDomain(MyDomain.class)
                .logResolution((property, systemOverride, value, isLast) ->
                        System.out.format("(%s) %s -> %s", systemOverride ? "sys " : "prop", property, value))
                .create(ctxProperties);

        DomainPredicates predicates = DomainPredicates.basedOnDomain(MyDomain.class)
                .predicate("env", "uat")
                .predicate("loc", "ldn")
                .predicate("group", "internal")
                .predicate("app", "whatsapp")
                .predicate("host", "localhost")
                .predicate("user", "kkarad")
                .create();

        assertThat(properties.resolveString("my.prop.key", predicates)).isEqualTo("myValue");
    }
}
