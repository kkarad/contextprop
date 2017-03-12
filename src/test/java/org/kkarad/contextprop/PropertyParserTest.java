package org.kkarad.contextprop;

import org.junit.jupiter.api.*;
import org.mockito.InOrder;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.*;

class PropertyParserTest {

    private ParseVisitor visitor;

    private PropertyParser parser;

    @BeforeEach
    void setUp() {
        visitor = spy(new LogAndDelegateVisitor(true, new ContextVisitor()));

        ContextPattern contextPattern = new ContextPattern(
                ".CTXT",
                '(',
                ')',
                new ConditionPattern(
                        '[',
                        ']',
                        visitor,
                        "|",
                        ','),
                visitor);

        parser = new PropertyParser(
                visitor,
                contextPattern);
    }

    @Test
    @DisplayName("An empty size of Properties results to an empty collection of ContextProperty")
    void test1() {
        Collection<ContextProperty> ctxProperties = parser.parse(new Properties());
        assertThat(ctxProperties).isEmpty();

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).startParse();
        inOrder.verify(visitor).endParse();
    }

    @Test
    @DisplayName("Property without the context identifier results to a ContextProperty with no condition and default value")
    void test2() {
        Properties unresolved = new Properties();
        String expectedDefaultValue = "myValue";
        String expectedKey = "my.property";
        unresolved.setProperty(expectedKey, expectedDefaultValue);
        Collection<ContextProperty> ctxProperties = parser.parse(unresolved);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).startParse();
        inOrder.verify(visitor).startProperty(eq(expectedKey));
        inOrder.verify(visitor).endProperty(eq(expectedKey), eq(expectedDefaultValue));
        inOrder.verify(visitor).endParse();

        assertThat(ctxProperties).hasOnlyOneElementSatisfying(ctxProp -> {
            assertThat(ctxProp.defaultValue()).isEqualTo(expectedDefaultValue);
            assertThat(ctxProp.contexts()).isEmpty();
        });

    }

    @Test
    @DisplayName("Property with context identifier but empty context results in exception thrown during parsing")
    void test3() {
        Properties unresolved = new Properties();
        unresolved.setProperty("my.property.CTXT()", "myValue");

        Throwable throwable = assertThrows(ContextPropParseException.class, () -> parser.parse(unresolved));
        System.out.println(throwable.getMessage());
    }

    @Test
    @DisplayName("Property context with empty conditions results in exception thrown during parsing")
    void test4() {
        Properties unresolved = new Properties();
        unresolved.setProperty("my.property.CTXT(env[])", "myValue");

        Throwable throwable = assertThrows(ContextPropParseException.class, () -> parser.parse(unresolved));
        System.out.println(throwable.getMessage());
    }

    @Test
    @DisplayName("Property context with wrong conditions start and end pattern results in exception thrown during parsing")
    void test5() {
        Properties unresolved = new Properties();
        unresolved.setProperty("my.property.CTXT(env(dev))", "myValue");

        Throwable throwable = assertThrows(ContextPropParseException.class, () -> parser.parse(unresolved));
        System.out.println(throwable.getMessage());
    }

    @Test
    @DisplayName("Property context with 2nd conditions having wrong start and end pattern results in exception thrown during parsing")
    void test6() {
        Properties unresolved = new Properties();
        unresolved.setProperty("my.property.CTXT(env[dev],location[hkg))", "myValue");

        Throwable throwable = assertThrows(ContextPropParseException.class, () -> parser.parse(unresolved));
        System.out.println(throwable.getMessage());
    }

    @Test
    @DisplayName("Property context without condition delimiter results in exception thrown during parsing")
    void test7() {
        Properties unresolved = new Properties();
        unresolved.setProperty("my.property.CTXT(env[dev]location[hkg])", "myValue");

        Throwable throwable = assertThrows(ContextPropParseException.class, () -> parser.parse(unresolved));
        System.out.println(throwable.getMessage());
    }

    @Test
    @DisplayName("Property context with wrong start pattern results in exception thrown during parsing")
    void test8() {
        Properties unresolved = new Properties();
        unresolved.setProperty("my.property.CTXT{env[])", "myValue");

        Throwable throwable = assertThrows(ContextPropParseException.class, () -> parser.parse(unresolved));
        System.out.println(throwable.getMessage());
    }

    @Test
    @DisplayName("Property context with wrong end pattern results in exception thrown during parsing")
    void test9() {
        Properties unresolved = new Properties();
        unresolved.setProperty("my.property.CTXT(env[test]}", "myValue");

        Throwable throwable = assertThrows(ContextPropParseException.class, () -> parser.parse(unresolved));
        System.out.println(throwable.getMessage());
    }

    @Test
    @DisplayName("When there is no property key before the context identifier throw exception")
    void test10() {
        Properties unresolved = new Properties();
        unresolved.setProperty(".CTXT(env[test])", "myValue");

        Throwable throwable = assertThrows(ContextPropParseException.class, () -> parser.parse(unresolved));
        System.out.println(throwable.getMessage());
    }

    @TestFactory
    @DisplayName("Parser identifies spelling mistakes and missing characters")
    Stream<DynamicTest> test11() {
        return Stream.of(
                "my.property.keyCTXT(env[test])",
                "my.property.key(env[test])",
                "my.property.key.CTX(env[test])",
                "my.property.key.TXT(env[test])")
                .map(key -> dynamicTest(key, () -> {
                    Properties unresolved = new Properties();
                    unresolved.setProperty(key, "myValue");

                    Throwable throwable = assertThrows(ContextPropParseException.class, () -> parser.parse(unresolved));
                    System.out.println(throwable.getMessage());
                }));
    }

    @Test
    @DisplayName("parses correctly property context with multiple conditions and condition with value list")
    void test12() {
        Properties unresolved = new Properties();
        String expectedValue = "myValue";
        unresolved.setProperty("my.property.key.CTXT(env[test],host[localhost],location[gr|uk|it|us])", expectedValue);
        String expectedDefaultValue = "defaultValue";
        unresolved.setProperty("my.property.key", expectedDefaultValue);

        Collection<ContextProperty> resolved = parser.parse(unresolved);
        assertThat(resolved).hasOnlyOneElementSatisfying(ctxProp -> {
            assertThat(ctxProp.key()).isEqualTo("my.property.key");
            assertThat(ctxProp.contexts()).hasOnlyOneElementSatisfying(ctx -> {
                assertThat(ctx.propertyValue()).isEqualTo(expectedValue);
                assertThat(ctx.conditions()).hasSize(3);
                assertThat(ctx.conditions())
                        .filteredOn(condition -> condition.domainKey().equals("env"))
                        .hasOnlyOneElementSatisfying(condition -> {
                            assertThat(condition.values()).containsExactly("test");
                        });
                assertThat(ctx.conditions())
                        .filteredOn(condition -> condition.domainKey().equals("host"))
                        .hasOnlyOneElementSatisfying(condition -> {
                            assertThat(condition.values()).containsExactly("localhost");
                        });
                assertThat(ctx.conditions())
                        .filteredOn(condition -> condition.domainKey().equals("location"))
                        .hasOnlyOneElementSatisfying(condition -> {
                            assertThat(condition.values()).containsExactly("gr", "uk", "it", "us");
                        });
            });
            assertThat(ctxProp.defaultValue()).isEqualTo(expectedDefaultValue);
        });
    }


    @Test
    @DisplayName("Incorrect condition value delimiter results in to one condition value")
    void test13() {
        Properties unresolved = new Properties();
        unresolved.setProperty("my.property.key.CTXT(location[gr,uk,it,us])", "myValue");

        Collection<ContextProperty> resolved = parser.parse(unresolved);
        assertThat(resolved)
                .flatExtracting(ContextProperty::contexts)
                .flatExtracting(Context::conditions)
                .hasOnlyOneElementSatisfying(condition ->
                        assertThat(condition.values()).containsExactly("gr,uk,it,us"));
    }
}