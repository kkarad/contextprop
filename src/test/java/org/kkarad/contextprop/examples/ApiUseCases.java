package org.kkarad.contextprop.examples;

import org.junit.jupiter.api.Test;
import org.kkarad.contextprop.ContextProperties;
import org.kkarad.contextprop.DomainPredicates;
import org.kkarad.contextprop.TypedProperties;

import java.math.BigDecimal;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ApiUseCases {

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
                .resolutionConsumer((property, systemOverride, value, isLast) ->
                        System.out.format("(%s) %s -> %s", systemOverride ? "sys " : "prop", property, value))
                .resolve(ctxProperties);

        assertThat(properties.getProperty("my.prop.key")).isEqualTo("myValue");
    }

    @Test
    void resolveContextPropertiesToTypedProperties() {
        Properties ctxProperties = new Properties();
        ctxProperties.setProperty("my.prop.boolean.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "true");
        ctxProperties.setProperty("my.prop.boolean", "false");
        ctxProperties.setProperty("my.prop.int.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "123");
        ctxProperties.setProperty("my.prop.int", "-123");
        ctxProperties.setProperty("my.prop.long.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "123456789");
        ctxProperties.setProperty("my.prop.long", "-123456789");
        ctxProperties.setProperty("my.prop.bd.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "1.23");
        ctxProperties.setProperty("my.prop.bd", "-1.23");

        DomainPredicates predicates = DomainPredicates.basedOnDomain(MyDomain.class)
                .predicate("env", "uat")
                .predicate("loc", "ldn")
                .predicate("group", "internal")
                .predicate("app", "whatsapp")
                .predicate("host", "localhost")
                .predicate("user", "kkarad")
                .create();

        TypedProperties properties = ContextProperties.basedOn(predicates)
                .resolutionConsumer((property, systemOverride, value, isLast) ->
                        System.out.format("(%s) %s -> %s%n", systemOverride ? "sys " : "prop", property, value))
                .resolveTyped(ctxProperties);

        assertThat(properties.getBoolean("my.prop.boolean")).isTrue();
        assertThat(properties.getString("my.prop.boolean")).isEqualTo("true");
        assertThat(properties.getInteger("my.prop.int")).isEqualTo(123);
        assertThat(properties.getLong("my.prop.long")).isEqualTo(123456789);
        assertThat(properties.getBigDecimal("my.prop.bd")).isEqualTo(new BigDecimal("1.23"));
    }

    @Test
    void createContextPropertiesAndResolveOnDemand() {
        Properties ctxProperties = new Properties();
        ctxProperties.setProperty("my.prop.boolean.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "true");
        ctxProperties.setProperty("my.prop.boolean", "false");
        ctxProperties.setProperty("my.prop.int.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "123");
        ctxProperties.setProperty("my.prop.int", "-123");
        ctxProperties.setProperty("my.prop.long.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "123456789");
        ctxProperties.setProperty("my.prop.long", "-123456789");
        ctxProperties.setProperty("my.prop.bd.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "1.23");
        ctxProperties.setProperty("my.prop.bd", "-1.23");

        ContextProperties properties = ContextProperties.basedOnDomain(MyDomain.class)
                .resolutionConsumer((property, systemOverride, value, isLast) ->
                        System.out.format("(%s) %s -> %s%n", systemOverride ? "sys " : "prop", property, value))
                .create(ctxProperties);

        DomainPredicates predicates = DomainPredicates.basedOnDomain(MyDomain.class)
                .predicate("env", "uat")
                .predicate("loc", "ldn")
                .predicate("group", "internal")
                .predicate("app", "whatsapp")
                .predicate("host", "localhost")
                .predicate("user", "kkarad")
                .create();

        assertThat(properties.resolveBoolean("my.prop.boolean", predicates)).isTrue();
        assertThat(properties.resolveString("my.prop.boolean", predicates)).isEqualTo("true");
        assertThat(properties.resolveInteger("my.prop.int", predicates)).isEqualTo(123);
        assertThat(properties.resolveLong("my.prop.long", predicates)).isEqualTo(123456789);
        assertThat(properties.resolveBigDecimal("my.prop.bd", predicates)).isEqualTo(new BigDecimal("1.23"));
    }
}
