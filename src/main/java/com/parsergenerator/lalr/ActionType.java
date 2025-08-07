package com.parsergenerator.lalr;

/**
 * Enumeration of action types in LALR parsing tables.
 */
public enum ActionType {
    /**
     * Shift action - push the current input symbol onto the stack and move to a new state.
     */
    SHIFT,
    
    /**
     * Reduce action - reduce by a production rule.
     */
    REDUCE,
    
    /**
     * Accept action - parsing completed successfully.
     */
    ACCEPT,
    
    /**
     * Error action - parsing error occurred.
     */
    ERROR
}