package com.parsergenerator.parser;

import com.parsergenerator.lexer.Position;
import com.parsergenerator.lexer.TokenType;

/**
 * Exception thrown when a syntax error is encountered during parsing.
 * This includes missing tokens, unexpected tokens, and malformed constructs.
 */
public class SyntaxErrorException extends ParseException {
    private final TokenType expectedType;
    private final TokenType actualType;
    private final String context;
    
    /**
     * Creates a new SyntaxErrorException with the specified message and position.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     */
    public SyntaxErrorException(String message, Position position) {
        this(message, position, null, null, null);
    }
    
    /**
     * Creates a new SyntaxErrorException with expected and actual token types.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     * @param expectedType the expected token type
     * @param actualType the actual token type encountered
     */
    public SyntaxErrorException(String message, Position position, 
                               TokenType expectedType, TokenType actualType) {
        this(message, position, expectedType, actualType, null);
    }
    
    /**
     * Creates a new SyntaxErrorException with full context information.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     * @param expectedType the expected token type
     * @param actualType the actual token type encountered
     * @param context additional context information
     */
    public SyntaxErrorException(String message, Position position, 
                               TokenType expectedType, TokenType actualType, String context) {
        super(formatDetailedMessage(message, expectedType, actualType, context), position);
        this.expectedType = expectedType;
        this.actualType = actualType;
        this.context = context;
    }
    
    /**
     * Returns the expected token type.
     * 
     * @return the expected token type, or null if not specified
     */
    public TokenType getExpectedType() {
        return expectedType;
    }
    
    /**
     * Returns the actual token type that was encountered.
     * 
     * @return the actual token type, or null if not specified
     */
    public TokenType getActualType() {
        return actualType;
    }
    
    /**
     * Returns additional context information about the error.
     * 
     * @return the context string, or null if not provided
     */
    public String getContext() {
        return context;
    }
    
    /**
     * Formats a detailed error message with token type information.
     * 
     * @param baseMessage the base error message
     * @param expectedType the expected token type
     * @param actualType the actual token type
     * @param context additional context
     * @return the formatted message
     */
    private static String formatDetailedMessage(String baseMessage, TokenType expectedType, 
                                              TokenType actualType, String context) {
        StringBuilder sb = new StringBuilder(baseMessage);
        
        if (expectedType != null && actualType != null) {
            sb.append(" (expected ").append(expectedType)
              .append(", but found ").append(actualType).append(")");
        } else if (expectedType != null) {
            sb.append(" (expected ").append(expectedType).append(")");
        } else if (actualType != null) {
            sb.append(" (found ").append(actualType).append(")");
        }
        
        if (context != null && !context.trim().isEmpty()) {
            sb.append(" in ").append(context);
        }
        
        return sb.toString();
    }
}