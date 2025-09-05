package com.demo.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;

/**
 * Unit tests for the VariableContext class.
 */
public class VariableContextTest {

    private VariableContext context;

    @BeforeEach
    public void setUp() {
        context = new VariableContext();
    }

    @Test
    public void testEmptyContext() {
        assertFalse(context.hasVariable("x"));
        assertFalse(context.hasVariable("pi"));
        assertEquals(0, context.getVariableCount());
        assertEquals(0, context.getConstantCount());
    }

    @Test
    public void testCreateDefault() {
        VariableContext defaultContext = VariableContext.createDefault();
        
        assertTrue(defaultContext.hasVariable("pi"));
        assertTrue(defaultContext.hasVariable("e"));
        assertEquals(0, defaultContext.getVariableCount());
        assertEquals(2, defaultContext.getConstantCount());
        
        try {
            assertEquals(Math.PI, defaultContext.getVariable("pi"), 1e-15);
            assertEquals(Math.E, defaultContext.getVariable("e"), 1e-15);
        } catch (VariableNotFoundException e) {
            fail("Default constants should be accessible");
        }
    }

    @Test
    public void testSetAndGetVariable() throws VariableNotFoundException {
        context.setVariable("x", 42.0);
        
        assertTrue(context.hasVariable("x"));
        assertEquals(42.0, context.getVariable("x"), 1e-15);
        assertEquals(1, context.getVariableCount());
    }

    @Test
    public void testSetVariableOverwrite() throws VariableNotFoundException {
        context.setVariable("x", 10.0);
        context.setVariable("x", 20.0);
        
        assertEquals(20.0, context.getVariable("x"), 1e-15);
        assertEquals(1, context.getVariableCount());
    }

    @Test
    public void testSetVariableNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            context.setVariable(null, 42.0);
        });
    }

    @Test
    public void testSetVariableEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            context.setVariable("", 42.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            context.setVariable("   ", 42.0);
        });
    }

    @Test
    public void testGetVariableNotFound() {
        assertThrows(VariableNotFoundException.class, () -> {
            context.getVariable("nonexistent");
        });
    }

    @Test
    public void testGetVariableNullName() {
        assertThrows(VariableNotFoundException.class, () -> {
            context.getVariable(null);
        });
    }

    @Test
    public void testGetVariableEmptyName() {
        assertThrows(VariableNotFoundException.class, () -> {
            context.getVariable("");
        });
        
        assertThrows(VariableNotFoundException.class, () -> {
            context.getVariable("   ");
        });
    }

    @Test
    public void testHasVariableNullAndEmpty() {
        assertFalse(context.hasVariable(null));
        assertFalse(context.hasVariable(""));
        assertFalse(context.hasVariable("   "));
    }

    @Test
    public void testVariableNameTrimming() throws VariableNotFoundException {
        context.setVariable("  x  ", 42.0);
        
        assertTrue(context.hasVariable("x"));
        assertTrue(context.hasVariable("  x  "));
        assertEquals(42.0, context.getVariable("x"), 1e-15);
        assertEquals(42.0, context.getVariable("  x  "), 1e-15);
    }

    @Test
    public void testVariablePrecedenceOverConstants() throws VariableNotFoundException {
        VariableContext defaultContext = VariableContext.createDefault();
        
        // pi constant should exist
        assertEquals(Math.PI, defaultContext.getVariable("pi"), 1e-15);
        
        // Override pi with a variable
        defaultContext.setVariable("pi", 3.0);
        assertEquals(3.0, defaultContext.getVariable("pi"), 1e-15);
        
        // Should still have the constant, but variable takes precedence
        assertTrue(defaultContext.hasVariable("pi"));
        assertEquals(1, defaultContext.getVariableCount());
        assertEquals(2, defaultContext.getConstantCount());
    }

    @Test
    public void testRemoveVariable() throws VariableNotFoundException {
        context.setVariable("x", 42.0);
        context.setVariable("y", 24.0);
        
        assertTrue(context.removeVariable("x"));
        assertFalse(context.hasVariable("x"));
        assertTrue(context.hasVariable("y"));
        assertEquals(1, context.getVariableCount());
        
        // Try to remove non-existent variable
        assertFalse(context.removeVariable("z"));
        assertFalse(context.removeVariable(null));
        assertFalse(context.removeVariable(""));
    }

    @Test
    public void testRemoveVariableDoesNotAffectConstants() throws VariableNotFoundException {
        VariableContext defaultContext = VariableContext.createDefault();
        
        // Try to remove a constant
        assertFalse(defaultContext.removeVariable("pi"));
        assertTrue(defaultContext.hasVariable("pi"));
        assertEquals(Math.PI, defaultContext.getVariable("pi"), 1e-15);
    }

    @Test
    public void testGetVariables() {
        context.setVariable("x", 10.0);
        context.setVariable("y", 20.0);
        
        Map<String, Double> variables = context.getVariables();
        assertEquals(2, variables.size());
        assertEquals(10.0, variables.get("x"), 1e-15);
        assertEquals(20.0, variables.get("y"), 1e-15);
        
        // Modifying returned map should not affect context
        variables.put("z", 30.0);
        assertFalse(context.hasVariable("z"));
    }

    @Test
    public void testGetConstants() {
        VariableContext defaultContext = VariableContext.createDefault();
        
        Map<String, Double> constants = defaultContext.getConstants();
        assertEquals(2, constants.size());
        assertEquals(Math.PI, constants.get("pi"), 1e-15);
        assertEquals(Math.E, constants.get("e"), 1e-15);
        
        // Modifying returned map should not affect context
        constants.put("phi", 1.618);
        assertFalse(defaultContext.hasVariable("phi"));
    }

    @Test
    public void testGetAllNames() {
        VariableContext defaultContext = VariableContext.createDefault();
        defaultContext.setVariable("x", 10.0);
        defaultContext.setVariable("y", 20.0);
        
        Set<String> allNames = defaultContext.getAllNames();
        assertEquals(4, allNames.size());
        assertTrue(allNames.contains("x"));
        assertTrue(allNames.contains("y"));
        assertTrue(allNames.contains("pi"));
        assertTrue(allNames.contains("e"));
    }

    @Test
    public void testClearVariables() throws VariableNotFoundException {
        VariableContext defaultContext = VariableContext.createDefault();
        defaultContext.setVariable("x", 10.0);
        defaultContext.setVariable("y", 20.0);
        
        assertEquals(2, defaultContext.getVariableCount());
        assertEquals(2, defaultContext.getConstantCount());
        
        defaultContext.clearVariables();
        
        assertEquals(0, defaultContext.getVariableCount());
        assertEquals(2, defaultContext.getConstantCount());
        assertFalse(defaultContext.hasVariable("x"));
        assertFalse(defaultContext.hasVariable("y"));
        assertTrue(defaultContext.hasVariable("pi"));
        assertTrue(defaultContext.hasVariable("e"));
    }

    @Test
    public void testCopy() throws VariableNotFoundException {
        VariableContext defaultContext = VariableContext.createDefault();
        defaultContext.setVariable("x", 42.0);
        
        VariableContext copy = defaultContext.copy();
        
        // Copy should have same content
        assertEquals(defaultContext.getVariableCount(), copy.getVariableCount());
        assertEquals(defaultContext.getConstantCount(), copy.getConstantCount());
        assertEquals(42.0, copy.getVariable("x"), 1e-15);
        assertEquals(Math.PI, copy.getVariable("pi"), 1e-15);
        
        // Modifying copy should not affect original
        copy.setVariable("y", 24.0);
        assertFalse(defaultContext.hasVariable("y"));
        assertTrue(copy.hasVariable("y"));
        
        // Modifying original should not affect copy
        defaultContext.setVariable("z", 36.0);
        assertFalse(copy.hasVariable("z"));
        assertTrue(defaultContext.hasVariable("z"));
    }

    @Test
    public void testCaseSensitivity() throws VariableNotFoundException {
        context.setVariable("X", 10.0);
        context.setVariable("x", 20.0);
        
        assertEquals(2, context.getVariableCount());
        assertEquals(10.0, context.getVariable("X"), 1e-15);
        assertEquals(20.0, context.getVariable("x"), 1e-15);
        assertTrue(context.hasVariable("X"));
        assertTrue(context.hasVariable("x"));
        assertFalse(context.hasVariable("y"));
    }

    @Test
    public void testToString() {
        context.setVariable("x", 42.0);
        String str = context.toString();
        
        assertTrue(str.contains("VariableContext"));
        assertTrue(str.contains("variables"));
        assertTrue(str.contains("constants"));
        assertTrue(str.contains("x"));
        assertTrue(str.contains("42.0"));
    }

    @Test
    public void testMultipleVariables() throws VariableNotFoundException {
        context.setVariable("a", 1.0);
        context.setVariable("b", 2.0);
        context.setVariable("c", 3.0);
        context.setVariable("result", 42.0);
        
        assertEquals(4, context.getVariableCount());
        assertEquals(1.0, context.getVariable("a"), 1e-15);
        assertEquals(2.0, context.getVariable("b"), 1e-15);
        assertEquals(3.0, context.getVariable("c"), 1e-15);
        assertEquals(42.0, context.getVariable("result"), 1e-15);
    }

    @Test
    public void testSpecialDoubleValues() throws VariableNotFoundException {
        context.setVariable("infinity", Double.POSITIVE_INFINITY);
        context.setVariable("negInfinity", Double.NEGATIVE_INFINITY);
        context.setVariable("nan", Double.NaN);
        context.setVariable("zero", 0.0);
        context.setVariable("negZero", -0.0);
        
        assertEquals(Double.POSITIVE_INFINITY, context.getVariable("infinity"));
        assertEquals(Double.NEGATIVE_INFINITY, context.getVariable("negInfinity"));
        assertTrue(Double.isNaN(context.getVariable("nan")));
        assertEquals(0.0, context.getVariable("zero"));
        assertEquals(-0.0, context.getVariable("negZero"));
    }
}