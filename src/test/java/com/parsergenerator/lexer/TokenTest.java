package com.parsergenerator.lexer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenTest {
    
    @Test
    void testConstructorWithPosition() {
        Position pos = new Position(5, 10);
        Token token = new Token(TokenType.TERMINAL, "ID", pos);
        
        assertEquals(TokenType.TERMINAL, token.getType());
        assertEquals("ID", token.getValue());
        assertEquals(pos, token.getPosition());
    }
    
    @Test
    void testConstructorWithoutPosition() {
        Token token = new Token(TokenType.NON_TERMINAL, "<expr>");
        
        assertEquals(TokenType.NON_TERMINAL, token.getType());
        assertEquals("<expr>", token.getValue());
        assertNotNull(token.getPosition());
        assertEquals(0, token.getPosition().getLine());
        assertEquals(0, token.getPosition().getColumn());
    }
    
    @Test
    void testIsType() {
        Token token = new Token(TokenType.EQUALS, "=");
        
        assertTrue(token.isType(TokenType.EQUALS));
        assertFalse(token.isType(TokenType.PIPE));
        assertFalse(token.isType(TokenType.TERMINAL));
    }
    
    @Test
    void testIsAnyType() {
        Token token = new Token(TokenType.PIPE, "|");
        
        assertTrue(token.isAnyType(TokenType.PIPE));
        assertTrue(token.isAnyType(TokenType.EQUALS, TokenType.PIPE));
        assertTrue(token.isAnyType(TokenType.TERMINAL, TokenType.PIPE, TokenType.EOF));
        assertFalse(token.isAnyType(TokenType.EQUALS, TokenType.TERMINAL));
        assertFalse(token.isAnyType());
    }
    
    @Test
    void testIsEOF() {
        Token eofToken = new Token(TokenType.EOF, "");
        Token normalToken = new Token(TokenType.TERMINAL, "ID");
        
        assertTrue(eofToken.isEOF());
        assertFalse(normalToken.isEOF());
    }
    
    @Test
    void testToString() {
        Position pos = new Position(3, 7);
        Token token = new Token(TokenType.PRODUCTION_ARROW, "::=", pos);
        
        String expected = "Token{type=PRODUCTION_ARROW, value='::=', position=3:7}";
        assertEquals(expected, token.toString());
    }
    
    @Test
    void testEquals() {
        Position pos1 = new Position(5, 10);
        Position pos2 = new Position(5, 10);
        Position pos3 = new Position(6, 10);
        
        Token token1 = new Token(TokenType.TERMINAL, "ID", pos1);
        Token token2 = new Token(TokenType.TERMINAL, "ID", pos2);
        Token token3 = new Token(TokenType.TERMINAL, "ID", pos3);
        Token token4 = new Token(TokenType.NON_TERMINAL, "ID", pos1);
        Token token5 = new Token(TokenType.TERMINAL, "NUMBER", pos1);
        
        assertEquals(token1, token2);
        assertNotEquals(token1, token3);
        assertNotEquals(token1, token4);
        assertNotEquals(token1, token5);
        assertNotEquals(token1, null);
        assertNotEquals(token1, "not a token");
    }
    
    @Test
    void testEqualsWithNullValues() {
        Token token1 = new Token(TokenType.TERMINAL, null, null);
        Token token2 = new Token(TokenType.TERMINAL, null, null);
        Token token3 = new Token(TokenType.TERMINAL, "ID", null);
        
        assertEquals(token1, token2);
        assertNotEquals(token1, token3);
    }
    
    @Test
    void testHashCode() {
        Position pos = new Position(5, 10);
        Token token1 = new Token(TokenType.TERMINAL, "ID", pos);
        Token token2 = new Token(TokenType.TERMINAL, "ID", pos);
        Token token3 = new Token(TokenType.NON_TERMINAL, "ID", pos);
        
        assertEquals(token1.hashCode(), token2.hashCode());
        assertNotEquals(token1.hashCode(), token3.hashCode());
    }
    
    @Test
    void testAllTokenTypes() {
        // Test that we can create tokens for all token types
        for (TokenType type : TokenType.values()) {
            Token token = new Token(type, "test");
            assertEquals(type, token.getType());
            assertEquals("test", token.getValue());
        }
    }
}