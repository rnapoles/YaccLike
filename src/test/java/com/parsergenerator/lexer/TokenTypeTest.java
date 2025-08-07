package com.parsergenerator.lexer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenTypeTest {
    
    @Test
    void testAllTokenTypesExist() {
        // Verify that all expected token types are defined
        TokenType[] expectedTypes = {
            // Identifiers and literals
            TokenType.TERMINAL,
            TokenType.NON_TERMINAL,
            TokenType.PATTERN,
            
            // Operators and delimiters
            TokenType.EQUALS,
            TokenType.PIPE,
            TokenType.PRODUCTION_ARROW,
            TokenType.LEFT_ANGLE,
            TokenType.RIGHT_ANGLE,
            TokenType.LEFT_BRACE,
            TokenType.RIGHT_BRACE,
            TokenType.LEFT_PAREN,
            TokenType.RIGHT_PAREN,
            TokenType.PLUS,
            
            // Keywords
            TokenType.START,
            
            // Special tokens
            TokenType.COMMENT,
            TokenType.WHITESPACE,
            TokenType.EOF,
            TokenType.NEWLINE,
            
            // Error token
            TokenType.UNKNOWN
        };
        
        // Check that all expected types exist
        for (TokenType expectedType : expectedTypes) {
            assertNotNull(expectedType, "Token type " + expectedType + " should exist");
        }
        
        // Check that we have the expected number of token types
        assertEquals(expectedTypes.length, TokenType.values().length, 
                    "Number of token types should match expected count");
    }
    
    @Test
    void testTokenTypeNames() {
        // Test specific token type names to ensure they're correctly defined
        assertEquals("TERMINAL", TokenType.TERMINAL.name());
        assertEquals("NON_TERMINAL", TokenType.NON_TERMINAL.name());
        assertEquals("PRODUCTION_ARROW", TokenType.PRODUCTION_ARROW.name());
        assertEquals("EOF", TokenType.EOF.name());
        assertEquals("UNKNOWN", TokenType.UNKNOWN.name());
    }
    
    @Test
    void testTokenTypeOrdinals() {
        // Ensure token types have consistent ordinal values
        assertTrue(TokenType.TERMINAL.ordinal() >= 0);
        assertTrue(TokenType.EOF.ordinal() >= 0);
        assertTrue(TokenType.UNKNOWN.ordinal() >= 0);
        
        // Ensure all ordinals are unique
        TokenType[] types = TokenType.values();
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                assertNotEquals(types[i].ordinal(), types[j].ordinal(),
                              "Token types should have unique ordinals");
            }
        }
    }
}