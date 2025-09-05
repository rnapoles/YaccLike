package com.demo.parser;

/**
 * Base exception class for all expression-related errors.
 * Provides common functionality for error reporting with position information.
 */
public class ExpressionException extends Exception {
    private final Position position;
    private final String context;

    /**
     * Creates a new ExpressionException with a message.
     * @param message the error message
     */
    public ExpressionException(String message) {
        super(message);
        this.position = null;
        this.context = null;
    }

    /**
     * Creates a new ExpressionException with a message and cause.
     * @param message the error message
     * @param cause the underlying cause
     */
    public ExpressionException(String message, Throwable cause) {
        super(message, cause);
        this.position = null;
        this.context = null;
    }

    /**
     * Creates a new ExpressionException with a message and position.
     * @param message the error message
     * @param position the position where the error occurred
     */
    public ExpressionException(String message, Position position) {
        super(message);
        this.position = position;
        this.context = null;
    }

    /**
     * Creates a new ExpressionException with a message, position, and context.
     * @param message the error message
     * @param position the position where the error occurred
     * @param context additional context information
     */
    public ExpressionException(String message, Position position, String context) {
        super(message);
        this.position = position;
        this.context = context;
    }

    /**
     * Gets the position where the error occurred.
     * @return the error position, or null if not available
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets additional context information about the error.
     * @return the context information, or null if not available
     */
    public String getContext() {
        return context;
    }

    /**
     * Gets a detailed error message including position and context information.
     * @return the detailed error message
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(getMessage());
        
        if (position != null) {
            sb.append(" at line ").append(position.getLine())
              .append(", column ").append(position.getColumn());
        }
        
        if (context != null && !context.isEmpty()) {
            sb.append(" (").append(context).append(")");
        }
        
        return sb.toString();
    }
}