package com.parsergenerator.parser;

import com.parsergenerator.lexer.Position;

/**
 * Exception thrown when a semantic error is encountered during parsing.
 * This includes issues like undefined symbols, duplicate definitions,
 * and other grammar validation problems.
 */
public class SemanticErrorException extends ParseException {
    private final String symbolName;
    private final String errorType;
    
    /**
     * Creates a new SemanticErrorException with the specified message and position.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     */
    public SemanticErrorException(String message, Position position) {
        this(message, position, null, null);
    }
    
    /**
     * Creates a new SemanticErrorException with symbol and error type information.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     * @param symbolName the name of the symbol involved in the error
     * @param errorType the type of semantic error
     */
    public SemanticErrorException(String message, Position position, 
                                 String symbolName, String errorType) {
        super(formatSemanticMessage(message, symbolName, errorType), position);
        this.symbolName = symbolName;
        this.errorType = errorType;
    }
    
    /**
     * Returns the name of the symbol involved in the error.
     * 
     * @return the symbol name, or null if not specified
     */
    public String getSymbolName() {
        return symbolName;
    }
    
    /**
     * Returns the type of semantic error.
     * 
     * @return the error type, or null if not specified
     */
    public String getErrorType() {
        return errorType;
    }
    
    /**
     * Formats a semantic error message with symbol information.
     * 
     * @param baseMessage the base error message
     * @param symbolName the symbol name
     * @param errorType the error type
     * @return the formatted message
     */
    private static String formatSemanticMessage(String baseMessage, String symbolName, String errorType) {
        StringBuilder sb = new StringBuilder(baseMessage);
        
        if (symbolName != null) {
            sb.append(" (symbol: ").append(symbolName).append(")");
        }
        
        if (errorType != null) {
            sb.append(" [").append(errorType).append("]");
        }
        
        return sb.toString();
    }
}