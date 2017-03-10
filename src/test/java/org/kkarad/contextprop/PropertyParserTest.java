package org.kkarad.contextprop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Collection;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PropertyParserTest {

    private ParseVisitor visitor;
    private ContextPattern contextPattern;
    private PropertyParser parser;

    @BeforeEach
    void setUp() {
        visitor = spy(new ContextVisitor());
        contextPattern = new ContextPattern(
                ".CTXT(",
                ')',
                new CriteriaPattern(
                        '[',
                        ']',
                        visitor,
                        ",",
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
    @DisplayName("Property without the context identifier results to a ContextProperty with no criteria and default value")
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

        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> parser.parse(unresolved));
        throwable.printStackTrace();

    }

    //criteria with empty []
}