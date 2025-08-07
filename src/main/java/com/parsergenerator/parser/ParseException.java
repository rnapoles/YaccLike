package com.parsergenerator.parser;

import com.parsergenerator.lexer.Position;

/**
 * Exception thrown when parsing of grammar specification fails.
 * Contains information about the error location and context.
 */
public class ParseException extends Exception {
    private final Position position;
    
    /**
     * Creates a new ParseException with the specified message.
     * 
     * @param message the error message
     */
    public ParseException(String message) {
        this(message, null);
    }
    
    /**
     * Creates a new ParseException with the specified message and position.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     */
    public ParseException(String message, Position position) {
        super(formatMessage(message, position));
        this.position = position;
    }
    
    /**
     * Creates a new ParseException with the specified message, position, and cause.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     * @param cause the underlying cause
     */
    public ParseException(String message, Position position, Throwable cause) {
        super(formatMessage(message, position), cause);
        this.position = position;
    }
    
    /**
     * Returns the position where the error occurred.
     * 
     * @return the error position, or null if not available
     */
    public Position getPosition() {
        return position;
    }
    
    /**
     * Formats the error message with position information.
     * 
     * @param message the base error message
     * @param position the error position
     * @return the formatted message
     */
    private static String formatMessage(String message, Position position) {
        if (position != null) {
            return String.format("Parse error at %s: %s", position, message);
        } else {
            return "Parse error: " + message;
        }
    }
}