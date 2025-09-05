package com.parsergenerator.codegen;

/**
 * Exception thrown when template processing fails.
 * This includes cases like missing variables, invalid template syntax,
 * or other template-related errors.
 */
public class TemplateException extends Exception {
    
    /**
     * Constructs a new TemplateException with the specified detail message.
     *
     * @param message the detail message
     */
    public TemplateException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new TemplateException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new TemplateException with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public TemplateException(Throwable cause) {
        super(cause);
    }
}