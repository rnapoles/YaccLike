package com.parsergenerator.codegen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TemplateException class.
 */
public class TemplateExceptionTest {
    
    @Test
    void testConstructorWithMessage() {
        String message = "Template error occurred";
        TemplateException exception = new TemplateException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Template error occurred";
        Exception cause = new Exception("Root cause");
        TemplateException exception = new TemplateException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertSame(cause, exception.getCause());
    }
    
    @Test
    void testConstructorWithCause() {
        Exception cause = new Exception("Root cause");
        TemplateException exception = new TemplateException(cause);
        
        assertSame(cause, exception.getCause());
        // Message should be the cause's toString()
        assertEquals(cause.toString(), exception.getMessage());
    }
    
    @Test
    void testConstructorWithNullMessage() {
        TemplateException exception = new TemplateException((String) null);
        assertNull(exception.getMessage());
    }
    
    @Test
    void testConstructorWithNullCause() {
        TemplateException exception = new TemplateException((Throwable) null);
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithNullMessageAndCause() {
        TemplateException exception = new TemplateException(null, null);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    void testIsCheckedException() {
        // TemplateException should be a checked exception (extends Exception)
        TemplateException exception = new TemplateException("test");
        assertTrue(exception instanceof Exception);
        // TemplateException extends Exception, not RuntimeException
        Exception ex = exception;
        assertFalse(ex instanceof RuntimeException);
    }
    
    @Test
    void testThrowAndCatch() {
        String message = "Test exception";
        
        assertThrows(TemplateException.class, () -> {
            throw new TemplateException(message);
        });
        
        try {
            throw new TemplateException(message);
        } catch (TemplateException e) {
            assertEquals(message, e.getMessage());
        }
    }
}