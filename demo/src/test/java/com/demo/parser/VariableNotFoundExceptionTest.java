package com.demo.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the VariableNotFoundException class.
 */
public class VariableNotFoundExceptionTest {

    @Test
    public void testBasicConstructor() {
        String variableName = "x";
        VariableNotFoundException ex = new VariableNotFoundException(variableName);
        
        assertEquals("Variable not found: x", ex.getMessage());
        assertEquals(variableName, ex.getVariableName());
        assertNull(ex.getPosition());
    }

    @Test
    public void testConstructorWithPosition() {
        String variableName = "myVar";
        Position position = new Position(3, 8);
        VariableNotFoundException ex = new VariableNotFoundException(variableName, position);
        
        assertEquals("Variable not found: myVar", ex.getMessage());
        assertEquals(variableName, ex.getVariableName());
        assertEquals(position, ex.getPosition());
        assertEquals("Variable not found: myVar at line 3, column 8", ex.getDetailedMessage());
    }

    @Test
    public void testVariableNameHandling() {
        // Test with different variable names
        VariableNotFoundException ex1 = new VariableNotFoundException("a");
        assertEquals("a", ex1.getVariableName());
        assertEquals("Variable not found: a", ex1.getMessage());

        VariableNotFoundException ex2 = new VariableNotFoundException("longVariableName");
        assertEquals("longVariableName", ex2.getVariableName());
        assertEquals("Variable not found: longVariableName", ex2.getMessage());

        VariableNotFoundException ex3 = new VariableNotFoundException("var_with_underscore");
        assertEquals("var_with_underscore", ex3.getVariableName());
        assertEquals("Variable not found: var_with_underscore", ex3.getMessage());
    }

    @Test
    public void testInheritanceFromExpressionException() {
        VariableNotFoundException ex = new VariableNotFoundException("test");
        assertTrue(ex instanceof ExpressionException);
    }

    @Test
    public void testDetailedMessage() {
        String variableName = "undefinedVar";
        Position position = new Position(1, 15);
        VariableNotFoundException ex = new VariableNotFoundException(variableName, position);
        
        String detailedMessage = ex.getDetailedMessage();
        assertTrue(detailedMessage.contains("Variable not found: undefinedVar"));
        assertTrue(detailedMessage.contains("line 1"));
        assertTrue(detailedMessage.contains("column 15"));
    }
}