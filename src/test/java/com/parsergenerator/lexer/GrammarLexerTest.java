package com.parsergenerator.lexer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class GrammarLexerTest {
    
    @Test
    void testEmptyInput() {
        GrammarLexer lexer = new GrammarLexer("");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).getType());
    }
    
    @Test
    void testNullInput() {
        GrammarLexer lexer = new GrammarLexer(null);
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).getType());
    }
    
    @Test
    void testSingleOperators() {
        GrammarLexer lexer = new GrammarLexer("= | ( ) +");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(6, tokens.size()); // 5 operators + EOF
        assertEquals(TokenType.EQUALS, tokens.get(0).getType());
        assertEquals(TokenType.PIPE, tokens.get(1).getType());
        assertEquals(TokenType.LEFT_PAREN, tokens.get(2).getType());
        assertEquals(TokenType.RIGHT_PAREN, tokens.get(3).getType());
        assertEquals(TokenType.PLUS, tokens.get(4).getType());
        assertEquals(TokenType.EOF, tokens.get(5).getType());
    }
    
    @Test
    void testProductionArrow() {
        GrammarLexer lexer = new GrammarLexer("::=");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(2, tokens.size()); // ::= + EOF
        assertEquals(TokenType.PRODUCTION_ARROW, tokens.get(0).getType());
        assertEquals("::=", tokens.get(0).getValue());
    }
    
    @Test
    void testTerminalIdentifiers() {
        GrammarLexer lexer = new GrammarLexer("ID NUMBER PLUS_OP identifier_123");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(5, tokens.size()); // 4 terminals + EOF
        assertEquals(TokenType.TERMINAL, tokens.get(0).getType());
        assertEquals("ID", tokens.get(0).getValue());
        assertEquals(TokenType.TERMINAL, tokens.get(1).getType());
        assertEquals("NUMBER", tokens.get(1).getValue());
        assertEquals(TokenType.TERMINAL, tokens.get(2).getType());
        assertEquals("PLUS_OP", tokens.get(2).getValue());
        assertEquals(TokenType.TERMINAL, tokens.get(3).getType());
        assertEquals("identifier_123", tokens.get(3).getValue());
    }
    
    @Test
    void testNonTerminals() {
        GrammarLexer lexer = new GrammarLexer("<expr> <stmt> <program>");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // 3 non-terminals + EOF
        assertEquals(TokenType.NON_TERMINAL, tokens.get(0).getType());
        assertEquals("expr", tokens.get(0).getValue());
        assertEquals(TokenType.NON_TERMINAL, tokens.get(1).getType());
        assertEquals("stmt", tokens.get(1).getValue());
        assertEquals(TokenType.NON_TERMINAL, tokens.get(2).getType());
        assertEquals("program", tokens.get(2).getValue());
    }
    
    @Test
    void testPatterns() {
        GrammarLexer lexer = new GrammarLexer("{[a-zA-Z]+} {[0-9]+} {\\+}");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // 3 patterns + EOF
        assertEquals(TokenType.PATTERN, tokens.get(0).getType());
        assertEquals("[a-zA-Z]+", tokens.get(0).getValue());
        assertEquals(TokenType.PATTERN, tokens.get(1).getType());
        assertEquals("[0-9]+", tokens.get(1).getValue());
        assertEquals(TokenType.PATTERN, tokens.get(2).getType());
        assertEquals("\\+", tokens.get(2).getValue());
    }
    
    @Test
    void testStartKeyword() {
        GrammarLexer lexer = new GrammarLexer("Start = <program>");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // Start + = + <program> + EOF
        assertEquals(TokenType.START, tokens.get(0).getType());
        assertEquals("Start", tokens.get(0).getValue());
        assertEquals(TokenType.EQUALS, tokens.get(1).getType());
        assertEquals(TokenType.NON_TERMINAL, tokens.get(2).getType());
        assertEquals("program", tokens.get(2).getValue());
    }
    
    @Test
    void testComments() {
        GrammarLexer lexer = new GrammarLexer("-- This is a comment\nID = {[a-zA-Z]+}");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(6, tokens.size()); // comment + newline + ID + = + pattern + EOF
        assertEquals(TokenType.COMMENT, tokens.get(0).getType());
        assertEquals(" This is a comment", tokens.get(0).getValue());
        assertEquals(TokenType.NEWLINE, tokens.get(1).getType());
        assertEquals(TokenType.TERMINAL, tokens.get(2).getType());
        assertEquals("ID", tokens.get(2).getValue());
    }
    
    @Test
    void testNewlines() {
        GrammarLexer lexer = new GrammarLexer("ID\nNUMBER\n");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(5, tokens.size()); // ID + newline + NUMBER + newline + EOF
        assertEquals(TokenType.TERMINAL, tokens.get(0).getType());
        assertEquals(TokenType.NEWLINE, tokens.get(1).getType());
        assertEquals(TokenType.TERMINAL, tokens.get(2).getType());
        assertEquals(TokenType.NEWLINE, tokens.get(3).getType());
        assertEquals(TokenType.EOF, tokens.get(4).getType());
    }
    
    @Test
    void testWhitespaceSkipping() {
        GrammarLexer lexer = new GrammarLexer("  ID   =   {test}  ");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // ID + = + pattern + EOF (whitespace skipped)
        assertEquals(TokenType.TERMINAL, tokens.get(0).getType());
        assertEquals(TokenType.EQUALS, tokens.get(1).getType());
        assertEquals(TokenType.PATTERN, tokens.get(2).getType());
        assertEquals("test", tokens.get(2).getValue());
    }
    
    @Test
    void testCompleteGrammarExample() {
        String grammar = "-- Arithmetic grammar\n" +
                        "Start = <expr>\n" +
                        "\n" +
                        "ID = {[a-zA-Z]+}\n" +
                        "NUMBER = {[0-9]+}\n" +
                        "PLUS = {\\+}\n" +
                        "\n" +
                        "<expr> ::= <expr> PLUS <term> | <term>\n" +
                        "<term> ::= ID | NUMBER\n";
        
        GrammarLexer lexer = new GrammarLexer(grammar);
        List<Token> tokens = lexer.tokenize();
        
        // Verify we get the expected token types
        assertTrue(tokens.size() > 10);
        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).getType());
        
        // Check for presence of key tokens
        boolean hasComment = tokens.stream().anyMatch(t -> t.getType() == TokenType.COMMENT);
        boolean hasStart = tokens.stream().anyMatch(t -> t.getType() == TokenType.START);
        boolean hasProductionArrow = tokens.stream().anyMatch(t -> t.getType() == TokenType.PRODUCTION_ARROW);
        boolean hasNonTerminal = tokens.stream().anyMatch(t -> t.getType() == TokenType.NON_TERMINAL);
        boolean hasPattern = tokens.stream().anyMatch(t -> t.getType() == TokenType.PATTERN);
        
        assertTrue(hasComment);
        assertTrue(hasStart);
        assertTrue(hasProductionArrow);
        assertTrue(hasNonTerminal);
        assertTrue(hasPattern);
    }
    
    @Test
    void testPositionTracking() {
        GrammarLexer lexer = new GrammarLexer("ID\n  NUMBER", "test.grammar");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // ID + newline + NUMBER + EOF
        
        // Check positions
        assertEquals(1, tokens.get(0).getPosition().getLine()); // ID
        assertEquals(1, tokens.get(0).getPosition().getColumn());
        assertEquals("test.grammar", tokens.get(0).getPosition().getFilename());
        
        assertEquals(1, tokens.get(1).getPosition().getLine()); // newline
        assertEquals(3, tokens.get(1).getPosition().getColumn());
        
        assertEquals(2, tokens.get(2).getPosition().getLine()); // NUMBER
        assertEquals(3, tokens.get(2).getPosition().getColumn());
    }
    
    @Test
    void testUnknownCharacters() {
        GrammarLexer lexer = new GrammarLexer("ID @ NUMBER");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // ID + @ + NUMBER + EOF
        assertEquals(TokenType.TERMINAL, tokens.get(0).getType());
        assertEquals(TokenType.UNKNOWN, tokens.get(1).getType());
        assertEquals("@", tokens.get(1).getValue());
        assertEquals(TokenType.TERMINAL, tokens.get(2).getType());
    }
    
    @Test
    void testIncompletePatterns() {
        GrammarLexer lexer = new GrammarLexer("{incomplete");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(2, tokens.size()); // pattern + EOF
        assertEquals(TokenType.PATTERN, tokens.get(0).getType());
        assertEquals("incomplete", tokens.get(0).getValue());
    }
    
    @Test
    void testIncompleteNonTerminals() {
        GrammarLexer lexer = new GrammarLexer("<incomplete");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(2, tokens.size()); // non-terminal + EOF
        assertEquals(TokenType.NON_TERMINAL, tokens.get(0).getType());
        assertEquals("incomplete", tokens.get(0).getValue());
    }
    
    @Test
    void testNextTokenMethod() {
        GrammarLexer lexer = new GrammarLexer("ID = {test}");
        
        Token token1 = lexer.nextToken();
        assertEquals(TokenType.TERMINAL, token1.getType());
        assertEquals("ID", token1.getValue());
        
        Token token2 = lexer.nextToken();
        assertEquals(TokenType.EQUALS, token2.getType());
        
        Token token3 = lexer.nextToken();
        assertEquals(TokenType.PATTERN, token3.getType());
        assertEquals("test", token3.getValue());
        
        Token token4 = lexer.nextToken();
        assertEquals(TokenType.EOF, token4.getType());
        
        // Should continue returning EOF
        Token token5 = lexer.nextToken();
        assertEquals(TokenType.EOF, token5.getType());
    }
    
    @Test
    void testComplexPatterns() {
        GrammarLexer lexer = new GrammarLexer("{[a-zA-Z_][a-zA-Z0-9_]*} {\\d+\\.\\d+} {\"[^\"]*\"}");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // 3 patterns + EOF
        assertEquals(TokenType.PATTERN, tokens.get(0).getType());
        assertEquals("[a-zA-Z_][a-zA-Z0-9_]*", tokens.get(0).getValue());
        assertEquals(TokenType.PATTERN, tokens.get(1).getType());
        assertEquals("\\d+\\.\\d+", tokens.get(1).getValue());
        assertEquals(TokenType.PATTERN, tokens.get(2).getType());
        assertEquals("\"[^\"]*\"", tokens.get(2).getValue());
    }
    
    @Test
    void testNestedBracesInPatterns() {
        GrammarLexer lexer = new GrammarLexer("{\\{[^}]*\\}}");
        List<Token> tokens = lexer.tokenize();
        
        // The lexer stops at the first } it encounters, so this creates multiple tokens
        // This is expected behavior for a simple lexer - it doesn't handle nested braces
        assertEquals(7, tokens.size()); // pattern + 5 unknown chars + EOF
        assertEquals(TokenType.PATTERN, tokens.get(0).getType());
        assertEquals("\\{[^", tokens.get(0).getValue()); // Stops at first }
        assertEquals(TokenType.UNKNOWN, tokens.get(1).getType());
        assertEquals("]", tokens.get(1).getValue());
    }
    
    @Test
    void testMultipleConsecutiveOperators() {
        GrammarLexer lexer = new GrammarLexer("::=||++");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(6, tokens.size()); // ::= + | + | + + + + + EOF
        assertEquals(TokenType.PRODUCTION_ARROW, tokens.get(0).getType());
        assertEquals(TokenType.PIPE, tokens.get(1).getType());
        assertEquals(TokenType.PIPE, tokens.get(2).getType());
        assertEquals(TokenType.PLUS, tokens.get(3).getType());
        assertEquals(TokenType.PLUS, tokens.get(4).getType());
        assertEquals(TokenType.EOF, tokens.get(5).getType());
    }
    
    @Test
    void testEmptyPatterns() {
        GrammarLexer lexer = new GrammarLexer("EMPTY = {}");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // EMPTY + = + {} + EOF
        assertEquals(TokenType.TERMINAL, tokens.get(0).getType());
        assertEquals("EMPTY", tokens.get(0).getValue());
        assertEquals(TokenType.EQUALS, tokens.get(1).getType());
        assertEquals(TokenType.PATTERN, tokens.get(2).getType());
        assertEquals("", tokens.get(2).getValue());
    }
    
    @Test
    void testEmptyNonTerminals() {
        GrammarLexer lexer = new GrammarLexer("<>");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(2, tokens.size()); // <> + EOF
        assertEquals(TokenType.NON_TERMINAL, tokens.get(0).getType());
        assertEquals("", tokens.get(0).getValue());
    }
    
    @Test
    void testCarriageReturnHandling() {
        GrammarLexer lexer = new GrammarLexer("ID\r\nNUMBER\r\n");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(5, tokens.size()); // ID + newline + NUMBER + newline + EOF
        assertEquals(TokenType.TERMINAL, tokens.get(0).getType());
        assertEquals("ID", tokens.get(0).getValue());
        assertEquals(TokenType.NEWLINE, tokens.get(1).getType());
        assertEquals(TokenType.TERMINAL, tokens.get(2).getType());
        assertEquals("NUMBER", tokens.get(2).getValue());
        assertEquals(TokenType.NEWLINE, tokens.get(3).getType());
    }
    
    @Test
    void testTabsAndSpaces() {
        GrammarLexer lexer = new GrammarLexer("ID\t=\t{test}\t");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // ID + = + pattern + EOF (tabs treated as whitespace)
        assertEquals(TokenType.TERMINAL, tokens.get(0).getType());
        assertEquals(TokenType.EQUALS, tokens.get(1).getType());
        assertEquals(TokenType.PATTERN, tokens.get(2).getType());
    }
    
    @Test
    void testLongComments() {
        String longComment = "-- This is a very long comment that spans multiple words and contains various characters like @#$%^&*()";
        GrammarLexer lexer = new GrammarLexer(longComment);
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(2, tokens.size()); // comment + EOF
        assertEquals(TokenType.COMMENT, tokens.get(0).getType());
        assertTrue(tokens.get(0).getValue().contains("very long comment"));
        assertTrue(tokens.get(0).getValue().contains("@#$%^&*()"));
    }
    
    @Test
    void testSpecialCharactersInIdentifiers() {
        GrammarLexer lexer = new GrammarLexer("ID_123 _underscore identifier2");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // 3 identifiers + EOF
        assertEquals(TokenType.TERMINAL, tokens.get(0).getType());
        assertEquals("ID_123", tokens.get(0).getValue());
        assertEquals(TokenType.TERMINAL, tokens.get(1).getType());
        assertEquals("_underscore", tokens.get(1).getValue());
        assertEquals(TokenType.TERMINAL, tokens.get(2).getType());
        assertEquals("identifier2", tokens.get(2).getValue());
    }
}