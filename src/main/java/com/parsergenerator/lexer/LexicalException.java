package com.parsergenerator.lexer;

/**
 * Exception thrown when lexical analysis fails.
 * This exception provides detailed information about the location and nature
 * of lexical errors encountered during tokenization.
 */
public class LexicalException extends Exception {
    private final int line;
    private final int column;
    private final String unexpectedInput;
    private final String suggestion;

    /**
     * Creates a new LexicalException with the specified message and position.
     *
     * @param message the error message
     * @param line the line number where the error occurred
     * @param column the column number where the error occurred
     * @param unexpectedInput the input that caused the error
     */
    public LexicalException(String message, int line, int column, String unexpectedInput) {
        this(message, line, column, unexpectedInput, null);
    }

    /**
     * Creates a new LexicalException with the specified message, position, and suggestion.
     *
     * @param message the error message
     * @param line the line number where the error occurred
     * @param column the column number where the error occurred
     * @param unexpectedInput the input that caused the error
     * @param suggestion a suggestion for fixing the error (optional)
     */
    public LexicalException(String message, int line, int column, String unexpectedInput, String suggestion) {
        super(message);
        this.line = line;
        this.column = column;
        this.unexpectedInput = unexpectedInput;
        this.suggestion = suggestion;
    }

    /**
     * Creates a new LexicalException from a Position object.
     *
     * @param message the error message
     * @param position the position where the error occurred
     * @param unexpectedInput the input that caused the error
     */
    public LexicalException(String message, Position position, String unexpectedInput) {
        this(message, position.getLine(), position.getColumn(), unexpectedInput, null);
    }

    /**
     * Creates a new LexicalException from a Position object with suggestion.
     *
     * @param message the error message
     * @param position the position where the error occurred
     * @param unexpectedInput the input that caused the error
     * @param suggestion a suggestion for fixing the error (optional)
     */
    public LexicalException(String message, Position position, String unexpectedInput, String suggestion) {
        this(message, position.getLine(), position.getColumn(), unexpectedInput, suggestion);
    }

    /**
     * Gets the line number where the error occurred.
     *
     * @return the line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the column number where the error occurred.
     *
     * @return the column number
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the input that caused the error.
     *
     * @return the unexpected input
     */
    public String getUnexpectedInput() {
        return unexpectedInput;
    }

    /**
     * Gets the suggestion for fixing the error.
     *
     * @return the suggestion, or null if none provided
     */
    public String getSuggestion() {
        return suggestion;
    }

    /**
     * Checks if this exception has a suggestion.
     *
     * @return true if a suggestion is available
     */
    public boolean hasSuggestion() {
        return suggestion != null && !suggestion.trim().isEmpty();
    }

    /**
     * Gets the position where the error occurred.
     *
     * @return a Position object representing the error location
     */
    public Position getPosition() {
        return new Position(line, column);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LexicalException at ").append(line).append(":").append(column);
        sb.append(": ").append(getMessage());
        
        if (unexpectedInput != null && !unexpectedInput.isEmpty()) {
            sb.append(" (unexpected input: '").append(unexpectedInput).append("')");
        }
        
        if (hasSuggestion()) {
            sb.append(" - Suggestion: ").append(suggestion);
        }
        
        return sb.toString();
    }
}