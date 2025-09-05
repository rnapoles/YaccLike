package com.demo.parser;

/**
 * Exception thrown during expression evaluation when runtime errors occur.
 * This includes mathematical errors like division by zero, invalid function arguments, etc.
 */
public class EvaluationException extends ExpressionException {
    private final String operation;

    /**
     * Creates a new EvaluationException with a message.
     * @param message the error message
     */
    public EvaluationException(String message) {
        super(message);
        this.operation = null;
    }

    /**
     * Creates a new EvaluationException with a message and cause.
     * @param message the error message
     * @param cause the underlying cause
     */
    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
        this.operation = null;
    }

    /**
     * Creates a new EvaluationException with a message and operation context.
     * @param message the error message
     * @param operation the operation that failed
     */
    public EvaluationException(String message, String operation) {
        super(message);
        this.operation = operation;
    }

    /**
     * Creates a new EvaluationException with a message, position, and operation context.
     * @param message the error message
     * @param position the position where the error occurred
     * @param operation the operation that failed
     */
    public EvaluationException(String message, Position position, String operation) {
        super(message, position);
        this.operation = operation;
    }

    /**
     * Gets the operation that caused the evaluation error.
     * @return the operation name, or null if not available
     */
    public String getOperation() {
        return operation;
    }

    @Override
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(super.getDetailedMessage());
        
        if (operation != null && !operation.isEmpty()) {
            sb.append(" during operation: ").append(operation);
        }
        
        return sb.toString();
    }
}