package com.demo.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the EvaluationException class.
 */
public class EvaluationExceptionTest {

    @Test
    public void testBasicConstructor() {
        String message = "Division by zero";
        EvaluationException ex = new EvaluationException(message);
        
        assertEquals(message, ex.getMessage());
        assertNull(ex.getOperation());
        assertEquals(message, ex.getDetailedMessage());
    }

    @Test
    public void testConstructorWithCause() {
        String message = "Mathematical error";
        ArithmeticException cause = new ArithmeticException("/ by zero");
        EvaluationException ex = new EvaluationException(message, cause);
        
        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
        assertNull(ex.getOperation());
    }

    @Test
    public void testConstructorWithOperation() {
        String message = "Invalid operation";
        String operation = "divide";
        EvaluationException ex = new EvaluationException(message, operation);
        
        assertEquals(message, ex.getMessage());
        assertEquals(operation, ex.getOperation());
        assertEquals("Invalid operation during operation: divide", ex.getDetailedMessage());
    }

    @Test
    public void testConstructorWithPositionAndOperation() {
        String message = "Runtime error";
        Position position = new Position(2, 5);
        String operation = "power";
        EvaluationException ex = new EvaluationException(message, position, operation);
        
        assertEquals(message, ex.getMessage());
        assertEquals(position, ex.getPosition());
        assertEquals(operation, ex.getOperation());
        assertEquals("Runtime error at line 2, column 5 during operation: power", ex.getDetailedMessage());
    }

    @Test
    public void testDetailedMessageWithoutOperation() {
        String message = "Error message";
        Position position = new Position(1, 1);
        EvaluationException ex = new EvaluationException(message, position, null);
        
        assertEquals("Error message at line 1, column 1", ex.getDetailedMessage());
    }

    @Test
    public void testDetailedMessageWithEmptyOperation() {
        String message = "Error message";
        EvaluationException ex = new EvaluationException(message, "");
        
        assertEquals("Error message", ex.getDetailedMessage());
    }

    @Test
    public void testInheritanceFromExpressionException() {
        EvaluationException ex = new EvaluationException("Test");
        assertTrue(ex instanceof ExpressionException);
    }
}