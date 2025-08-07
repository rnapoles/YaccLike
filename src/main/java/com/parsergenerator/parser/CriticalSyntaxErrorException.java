package com.parsergenerator.parser;

import com.parsergenerator.lexer.Position;
import com.parsergenerator.lexer.TokenType;

/**
 * A critical syntax error that should not be recovered from.
 * This type of error indicates a fundamental misunderstanding of the grammar syntax
 * and should be reported immediately without attempting error recovery.
 */
public class CriticalSyntaxErrorException extends SyntaxErrorException {
    
    /**
     * Creates a new CriticalSyntaxErrorException with the specified message and position.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     */
    public CriticalSyntaxErrorException(String message, Position position) {
        super(message, position);
    }
    
    /**
     * Creates a new CriticalSyntaxErrorException with expected and actual token types.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     * @param expectedType the expected token type
     * @param actualType the actual token type encountered
     */
    public CriticalSyntaxErrorException(String message, Position position, 
                                       TokenType expectedType, TokenType actualType) {
        super(message, position, expectedType, actualType);
    }
    
    /**
     * Creates a new CriticalSyntaxErrorException with full context information.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     * @param expectedType the expected token type
     * @param actualType the actual token type encountered
     * @param context additional context information
     */
    public CriticalSyntaxErrorException(String message, Position position, 
                                       TokenType expectedType, TokenType actualType, String context) {
        super(message, position, expectedType, actualType, context);
    }
}