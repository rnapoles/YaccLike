package com.parsergenerator.lexer;

/**
 * Enumeration of all token types that can appear in a grammar specification.
 * This includes terminals, non-terminals, operators, and special symbols.
 */
public enum TokenType {
    // Identifiers and literals
    TERMINAL,           // Terminal symbol (e.g., ID, NUMBER)
    NON_TERMINAL,       // Non-terminal symbol (e.g., <expr>, <stmt>)
    PATTERN,            // Pattern definition (e.g., {[a-zA-Z]+})
    
    // Operators and delimiters
    EQUALS,             // =
    PIPE,               // |
    PRODUCTION_ARROW,   // ::=
    LEFT_ANGLE,         // <
    RIGHT_ANGLE,        // >
    LEFT_BRACE,         // {
    RIGHT_BRACE,        // }
    LEFT_PAREN,         // (
    RIGHT_PAREN,        // )
    PLUS,               // +
    
    // Keywords
    START,              // Start keyword
    
    // Special tokens
    COMMENT,            // -- comment text
    WHITESPACE,         // Spaces, tabs, newlines
    EOF,                // End of file
    NEWLINE,            // Line separator
    
    // Error token
    UNKNOWN             // Unrecognized character sequence
}