package com.demo.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ExpressionException class.
 */
public class ExpressionExceptionTest {

    @Test
    public void testBasicConstructor() {
        String message = "Test error message";
        ExpressionException ex = new ExpressionException(message);
        
        assertEquals(message, ex.getMessage());
        assertNull(ex.getPosition());
        assertNull(ex.getContext());
        assertEquals(message, ex.getDetailedMessage());
    }

    @Test
    public void testConstructorWithCause() {
        String message = "Test error message";
        RuntimeException cause = new RuntimeException("Underlying cause");
        ExpressionException ex = new ExpressionException(message, cause);
        
        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
        assertNull(ex.getPosition());
        assertNull(ex.getContext());
    }

    @Test
    public void testConstructorWithPosition() {
        String message = "Test error message";
        Position position = new Position(5, 10);
        ExpressionException ex = new ExpressionException(message, position);
        
        assertEquals(message, ex.getMessage());
        assertEquals(position, ex.getPosition());
        assertNull(ex.getContext());
        assertEquals("Test error message at line 5, column 10", ex.getDetailedMessage());
    }

    @Test
    public void testConstructorWithPositionAndContext() {
        String message = "Test error message";
        Position position = new Position(3, 7);
        String context = "parsing expression";
        ExpressionException ex = new ExpressionException(message, position, context);
        
        assertEquals(message, ex.getMessage());
        assertEquals(position, ex.getPosition());
        assertEquals(context, ex.getContext());
        assertEquals("Test error message at line 3, column 7 (parsing expression)", ex.getDetailedMessage());
    }

    @Test
    public void testDetailedMessageWithoutPositionButWithContext() {
        String message = "Test error message";
        String context = "test context";
        ExpressionException ex = new ExpressionException(message, null, context);
        
        assertEquals("Test error message (test context)", ex.getDetailedMessage());
    }

    @Test
    public void testDetailedMessageWithEmptyContext() {
        String message = "Test error message";
        Position position = new Position(1, 1);
        ExpressionException ex = new ExpressionException(message, position, "");
        
        assertEquals("Test error message at line 1, column 1", ex.getDetailedMessage());
    }
}