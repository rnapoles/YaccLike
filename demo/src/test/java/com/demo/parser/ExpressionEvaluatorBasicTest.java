package com.demo.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic unit tests for the ExpressionEvaluator class.
 * Tests core functionality without complex AST construction.
 */
class ExpressionEvaluatorBasicTest {

    private ExpressionEvaluator evaluator;
    private VariableContext context;

    @BeforeEach
    void setUp() {
        context = VariableContext.createDefault();
        evaluator = new ExpressionEvaluator(context);
    }

    @Test
    @DisplayName("Should create evaluator with default context")
    void testDefaultConstructor() {
        ExpressionEvaluator defaultEvaluator = new ExpressionEvaluator();
        assertNotNull(defaultEvaluator.getVariableContext());
    }

    @Test
    @DisplayName("Should create evaluator with custom context")
    void testCustomContextConstructor() {
        VariableContext customContext = VariableContext.createDefault();
        customContext.setVariable("test", 42.0);
        
        ExpressionEvaluator customEvaluator = new ExpressionEvaluator(customContext);
        assertEquals(customContext, customEvaluator.getVariableContext());
    }

    @Test
    @DisplayName("Should handle null context gracefully")
    void testNullContextHandling() {
        ExpressionEvaluator nullContextEvaluator = new ExpressionEvaluator(null);
        assertNotNull(nullContextEvaluator.getVariableContext());
    }

    @Test
    @DisplayName("Should have access to variable context")
    void testVariableContextAccess() {
        context.setVariable("x", 5.0);
        assertEquals(context, evaluator.getVariableContext());
        assertTrue(evaluator.getVariableContext().hasVariable("x"));
    }

    @Test
    @DisplayName("Should have access to mathematical constants")
    void testMathematicalConstants() {
        assertTrue(evaluator.getVariableContext().hasVariable("pi"));
        assertTrue(evaluator.getVariableContext().hasVariable("e"));
    }
}