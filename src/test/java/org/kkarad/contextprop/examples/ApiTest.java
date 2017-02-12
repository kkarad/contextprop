package org.kkarad.contextprop.examples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kkarad.contextprop.Context;
import org.kkarad.contextprop.ContextPropParser;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiTest {

    @SuppressWarnings("unused")
    enum Keys {env, loc, group, app, host, user}

    @Test
    @DisplayName("Basic usage")
    void basic_usage() {

        Properties ctxProp = new Properties();
        ctxProp.setProperty("my.prop.key.CTXT[env(uat),loc(ldn,nyk),group(internal),app(whatsapp),host(localhost),user(kkarad)]", "myValue");
        ctxProp.setProperty("my.prop.key", "defaultValue");

        Context context = Context.basedOn(Keys.class)
                .entry("env", "uat")
                .entry("loc", "ldn")
                .entry("group", "internal")
                .entry("app", "whatsapp")
                .entry("host", "localhost")
                .entry("user", "kkarad")
                .create();

        Properties conf = ContextPropParser.parser(context)
                .requiresDefault(false)
                .parse(ctxProp);

        assertEquals("myValue", conf.getProperty("my.prop.key"));
    }
}
