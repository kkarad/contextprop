package org.kkarad.contextprop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiTest {

    @SuppressWarnings("unused")
    enum Keys {env, loc, group, app, host, user}

    @Test
    @DisplayName("Basic usage")
    void basic_usage() {

        Properties contextProperties = new Properties();
        contextProperties.setProperty("my.prop.key.CTXT[env(uat),loc(ldn,nyk),group(internal),app(whatsapp),host(localhost),user(kkarad)]", "myValue");

        Context context = Context.contextBasedOn(Keys.class)
                .entry("env", "uat")
                .entry("loc", "ldn")
                .entry("group", "internal")
                .entry("app", "whatsapp")
                .entry("host", "localhost")
                .entry("user", "kkarad")
                .create();

        Properties conf = ContextPropParser.parser(context)
                .requiresDefault(false)
                .parse(contextProperties);

        assertEquals("myValue", conf.getProperty("my.prop.key"));
    }
}
