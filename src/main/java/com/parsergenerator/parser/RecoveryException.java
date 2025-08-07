package com.parsergenerator.parser;

import com.parsergenerator.lexer.Position;

/**
 * Exception used internally for error recovery during parsing.
 * This exception is caught and handled by the parser to continue
 * parsing after encountering recoverable errors.
 */
public class RecoveryException extends ParseException {
    private final int recoveryPosition;
    private final String recoveryStrategy;
    
    /**
     * Creates a new RecoveryException with the specified message and position.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     * @param recoveryPosition the position to recover to
     */
    public RecoveryException(String message, Position position, int recoveryPosition) {
        this(message, position, recoveryPosition, "panic mode");
    }
    
    /**
     * Creates a new RecoveryException with recovery strategy information.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     * @param recoveryPosition the position to recover to
     * @param recoveryStrategy the recovery strategy used
     */
    public RecoveryException(String message, Position position, 
                           int recoveryPosition, String recoveryStrategy) {
        super(message + " (attempting recovery using " + recoveryStrategy + ")", position);
        this.recoveryPosition = recoveryPosition;
        this.recoveryStrategy = recoveryStrategy;
    }
    
    /**
     * Returns the position to recover to.
     * 
     * @return the recovery position
     */
    public int getRecoveryPosition() {
        return recoveryPosition;
    }
    
    /**
     * Returns the recovery strategy used.
     * 
     * @return the recovery strategy
     */
    public String getRecoveryStrategy() {
        return recoveryStrategy;
    }
}