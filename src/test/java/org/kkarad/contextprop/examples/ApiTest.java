package org.kkarad.contextprop.examples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kkarad.contextprop.ContextProperties;
import org.kkarad.contextprop.DomainPredicates;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ApiTest {

    @SuppressWarnings("unused")
    enum MyDomain {
        env, loc, group, app, host, user
    }

    @Test
    @DisplayName("Basic usage")
    void basic_usage() {

        Properties ctxProperties = new Properties();
        ctxProperties.setProperty("my.prop.key.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "myValue");
        ctxProperties.setProperty("my.prop.key", "defaultValue");

        DomainPredicates predicates = DomainPredicates.basedOnDomain(MyDomain.class)
                .predicate("env", "uat")
                .predicate("loc", "ldn")
                .predicate("group", "internal")
                .predicate("app", "whatsapp")
                .predicate("host", "localhost")
                .predicate("user", "kkarad")
                .create();

        Properties properties = ContextProperties.create(predicates)
                .requiresDefault(false)
                .resolve(ctxProperties);

        assertEquals("myValue", properties.getProperty("my.prop.key"));
    }

    @Test
    void syntax_fails() {
        Properties ctxProperties = new Properties();
        ctxProperties.setProperty("", "myValue");

        DomainPredicates predicates = DomainPredicates.basedOnDomain(MyDomain.class)
                .predicate("env", "uat")
                .predicate("loc", "ldn")
                .predicate("group", "internal")
                .predicate("app", "whatsapp")
                .predicate("host", "localhost")
                .predicate("user", "kkarad")
                .create();

        Properties properties = ContextProperties.create(predicates)
                .requiresDefault(false)
                .resolve(ctxProperties);

        fail("");

    }
}
