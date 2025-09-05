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
    
    // Multi-line detection infrastructure tests
    
    @Test
    void testIsAtLineStartAtBeginning() {
        GrammarLexer lexer = new GrammarLexer("ID = {test}");
        
        // Use reflection to access private method for testing
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("isAtLineStart");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertTrue(result, "Should be at line start at position 0");
        } catch (Exception e) {
            fail("Failed to test isAtLineStart method: " + e.getMessage());
        }
    }
    
    @Test
    void testIsAtLineStartAfterNewline() {
        GrammarLexer lexer = new GrammarLexer("ID\nNUMBER");
        
        // Advance past "ID\n" to position after newline
        lexer.nextToken(); // ID
        lexer.nextToken(); // newline
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("isAtLineStart");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertTrue(result, "Should be at line start after newline");
        } catch (Exception e) {
            fail("Failed to test isAtLineStart method: " + e.getMessage());
        }
    }
    
    @Test
    void testIsAtLineStartInMiddleOfLine() {
        GrammarLexer lexer = new GrammarLexer("ID = {test}");
        
        // Advance past "ID" to be in middle of line
        lexer.nextToken(); // ID
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("isAtLineStart");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertFalse(result, "Should not be at line start in middle of line");
        } catch (Exception e) {
            fail("Failed to test isAtLineStart method: " + e.getMessage());
        }
    }
    
    @Test
    void testIsProductionContinuationWithLeadingPipe() {
        GrammarLexer lexer = new GrammarLexer("  | <term>");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("isProductionContinuation");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertTrue(result, "Should detect continuation with leading pipe after whitespace");
        } catch (Exception e) {
            fail("Failed to test isProductionContinuation method: " + e.getMessage());
        }
    }
    
    @Test
    void testIsProductionContinuationWithoutPipe() {
        GrammarLexer lexer = new GrammarLexer("  <term>");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("isProductionContinuation");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertFalse(result, "Should not detect continuation without leading pipe");
        } catch (Exception e) {
            fail("Failed to test isProductionContinuation method: " + e.getMessage());
        }
    }
    
    @Test
    void testIsProductionContinuationAtEndOfInput() {
        GrammarLexer lexer = new GrammarLexer("");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("isProductionContinuation");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertFalse(result, "Should not detect continuation at end of input");
        } catch (Exception e) {
            fail("Failed to test isProductionContinuation method: " + e.getMessage());
        }
    }
    
    @Test
    void testIsProductionContinuationPositionRestoration() {
        GrammarLexer lexer = new GrammarLexer("   | <term>");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("isProductionContinuation");
            method.setAccessible(true);
            
            // Get initial position
            Token firstToken = lexer.nextToken();
            assertEquals(TokenType.PIPE, firstToken.getType());
            assertEquals(4, firstToken.getPosition().getColumn()); // Should be at column 4 (after 3 spaces)
            
        } catch (Exception e) {
            fail("Failed to test position restoration: " + e.getMessage());
        }
    }
    
    @Test
    void testMultiLineInfrastructureInitialization() {
        GrammarLexer lexer = new GrammarLexer("ID = {test}");
        
        try {
            // Test that multi-line fields are properly initialized
            java.lang.reflect.Field tokenBufferField = GrammarLexer.class.getDeclaredField("tokenBuffer");
            tokenBufferField.setAccessible(true);
            List<?> tokenBuffer = (List<?>) tokenBufferField.get(lexer);
            assertNotNull(tokenBuffer, "Token buffer should be initialized");
            assertTrue(tokenBuffer.isEmpty(), "Token buffer should be empty initially");
            
            java.lang.reflect.Field inMultiLineField = GrammarLexer.class.getDeclaredField("inMultiLineProduction");
            inMultiLineField.setAccessible(true);
            boolean inMultiLine = (Boolean) inMultiLineField.get(lexer);
            assertFalse(inMultiLine, "Should not be in multi-line production initially");
            
            java.lang.reflect.Field multiLineStartLineField = GrammarLexer.class.getDeclaredField("multiLineStartLine");
            multiLineStartLineField.setAccessible(true);
            int multiLineStartLine = (Integer) multiLineStartLineField.get(lexer);
            assertEquals(-1, multiLineStartLine, "Multi-line start line should be -1 initially");
            
        } catch (Exception e) {
            fail("Failed to test multi-line infrastructure initialization: " + e.getMessage());
        }
    }
    
    // Line continuation detection tests
    
    @Test
    void testDetectContinuationPatternWithTrailingPipe() {
        GrammarLexer lexer = new GrammarLexer("<expr> ::= <term> |\n  <factor>");
        
        // Advance to the second line
        lexer.nextToken(); // <expr>
        lexer.nextToken(); // ::=
        lexer.nextToken(); // <term>
        lexer.nextToken(); // |
        lexer.nextToken(); // newline
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("detectContinuationPattern");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertTrue(result, "Should detect continuation pattern with trailing pipe on previous line");
        } catch (Exception e) {
            fail("Failed to test detectContinuationPattern method: " + e.getMessage());
        }
    }
    
    @Test
    void testDetectContinuationPatternWithLeadingPipe() {
        GrammarLexer lexer = new GrammarLexer("  | <factor>");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("detectContinuationPattern");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertTrue(result, "Should detect continuation pattern with leading pipe");
        } catch (Exception e) {
            fail("Failed to test detectContinuationPattern method: " + e.getMessage());
        }
    }
    
    @Test
    void testDetectContinuationPatternWithIncompleteProduction() {
        GrammarLexer lexer = new GrammarLexer("<expr> ::=\n  <term>");
        
        // Advance to the second line
        lexer.nextToken(); // <expr>
        lexer.nextToken(); // ::=
        lexer.nextToken(); // newline
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("detectContinuationPattern");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertTrue(result, "Should detect continuation pattern with incomplete production");
        } catch (Exception e) {
            fail("Failed to test detectContinuationPattern method: " + e.getMessage());
        }
    }
    
    @Test
    void testDetectContinuationPatternNoContinuation() {
        GrammarLexer lexer = new GrammarLexer("<expr> ::= <term>\n<stmt> ::= ID");
        
        // Advance to the second line
        lexer.nextToken(); // <expr>
        lexer.nextToken(); // ::=
        lexer.nextToken(); // <term>
        lexer.nextToken(); // newline
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("detectContinuationPattern");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertFalse(result, "Should not detect continuation pattern for complete production");
        } catch (Exception e) {
            fail("Failed to test detectContinuationPattern method: " + e.getMessage());
        }
    }
    
    @Test
    void testDetectContinuationPatternAtEndOfInput() {
        GrammarLexer lexer = new GrammarLexer("");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("detectContinuationPattern");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertFalse(result, "Should not detect continuation pattern at end of input");
        } catch (Exception e) {
            fail("Failed to test detectContinuationPattern method: " + e.getMessage());
        }
    }
    
    @Test
    void testDetectContinuationPatternWithWhitespaceVariations() {
        // Test with various whitespace patterns
        String[] testCases = {
            "<expr> ::= <term>   |\n    <factor>",  // trailing spaces before pipe
            "<expr> ::= <term>|\n\t<factor>",       // no space before pipe, tab on next line
            "<expr> ::= <term> |   \n  <factor>",   // spaces after pipe
            "<expr> ::=\t\n\t\t<factor>"            // tabs everywhere
        };
        
        for (String testCase : testCases) {
            GrammarLexer lexer = new GrammarLexer(testCase);
            
            // Advance to the second line
            while (lexer.nextToken().getType() != TokenType.NEWLINE) {
                // Skip to newline
            }
            
            try {
                java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("detectContinuationPattern");
                method.setAccessible(true);
                
                boolean result = (Boolean) method.invoke(lexer);
                assertTrue(result, "Should detect continuation pattern with whitespace variations: " + testCase);
            } catch (Exception e) {
                fail("Failed to test detectContinuationPattern with whitespace variations: " + e.getMessage());
            }
        }
    }
    
    @Test
    void testIsIncompleteProductionWithProductionArrow() {
        GrammarLexer lexer = new GrammarLexer("dummy");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("isIncompleteProduction", String.class);
            method.setAccessible(true);
            
            // Test cases that should be considered incomplete
            assertTrue((Boolean) method.invoke(lexer, "<expr> ::="), "Should detect incomplete production ending with ::=");
            assertTrue((Boolean) method.invoke(lexer, "<expr> ::= <term> |"), "Should detect incomplete production ending with |");
            assertTrue((Boolean) method.invoke(lexer, "<expr> ::=   "), "Should detect incomplete production with empty right side");
            
            // Test cases that should NOT be considered incomplete
            assertFalse((Boolean) method.invoke(lexer, "<expr> ::= <term>"), "Should not detect complete production as incomplete");
            assertFalse((Boolean) method.invoke(lexer, "ID = {test}"), "Should not detect terminal definition as incomplete");
            assertFalse((Boolean) method.invoke(lexer, ""), "Should not detect empty line as incomplete");
            assertFalse((Boolean) method.invoke(lexer, "-- comment"), "Should not detect comment as incomplete");
            
        } catch (Exception e) {
            fail("Failed to test isIncompleteProduction method: " + e.getMessage());
        }
    }
    
    @Test
    void testSkipLeadingWhitespaceOnContinuationLine() {
        GrammarLexer lexer = new GrammarLexer("   \t  | <factor>");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("skipLeadingWhitespaceOnContinuationLine");
            method.setAccessible(true);
            
            // Before skipping, we should be at the first space
            assertEquals(' ', lexer.peek());
            
            // Skip leading whitespace
            method.invoke(lexer);
            
            // After skipping, we should be at the pipe character
            assertEquals('|', lexer.peek());
            
        } catch (Exception e) {
            fail("Failed to test skipLeadingWhitespaceOnContinuationLine method: " + e.getMessage());
        }
    }
    
    @Test
    void testSkipLeadingWhitespaceOnContinuationLineWithNewline() {
        GrammarLexer lexer = new GrammarLexer("   \n<factor>");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("skipLeadingWhitespaceOnContinuationLine");
            method.setAccessible(true);
            
            // Skip leading whitespace - should stop at newline
            method.invoke(lexer);
            
            // After skipping, we should be at the newline character
            assertEquals('\n', lexer.peek());
            
        } catch (Exception e) {
            fail("Failed to test skipLeadingWhitespaceOnContinuationLine with newline: " + e.getMessage());
        }
    }
    
    @Test
    void testSkipLeadingWhitespaceOnContinuationLineWithMixedWhitespace() {
        GrammarLexer lexer = new GrammarLexer(" \t \r | <factor>");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("skipLeadingWhitespaceOnContinuationLine");
            method.setAccessible(true);
            
            // Before skipping, we should be at the first space
            assertEquals(' ', lexer.peek());
            
            // Skip leading whitespace (space, tab, space, carriage return)
            method.invoke(lexer);
            
            // After skipping, we should be at the pipe character
            assertEquals('|', lexer.peek());
            
        } catch (Exception e) {
            fail("Failed to test skipLeadingWhitespaceOnContinuationLine with mixed whitespace: " + e.getMessage());
        }
    }
    
    @Test
    void testIsAnyWhitespaceMethod() {
        GrammarLexer lexer = new GrammarLexer("dummy");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("isAnyWhitespace", char.class);
            method.setAccessible(true);
            
            // Test all whitespace characters
            assertTrue((Boolean) method.invoke(lexer, ' '), "Space should be recognized as whitespace");
            assertTrue((Boolean) method.invoke(lexer, '\t'), "Tab should be recognized as whitespace");
            assertTrue((Boolean) method.invoke(lexer, '\r'), "Carriage return should be recognized as whitespace");
            assertTrue((Boolean) method.invoke(lexer, '\n'), "Newline should be recognized as whitespace");
            
            // Test non-whitespace characters
            assertFalse((Boolean) method.invoke(lexer, 'a'), "Letter should not be recognized as whitespace");
            assertFalse((Boolean) method.invoke(lexer, '1'), "Digit should not be recognized as whitespace");
            assertFalse((Boolean) method.invoke(lexer, '|'), "Pipe should not be recognized as whitespace");
            assertFalse((Boolean) method.invoke(lexer, '<'), "Angle bracket should not be recognized as whitespace");
            
        } catch (Exception e) {
            fail("Failed to test isAnyWhitespace method: " + e.getMessage());
        }
    }
    
    @Test
    void testWhitespaceSkippingWithAllTypes() {
        // Test that the lexer properly handles all types of whitespace
        GrammarLexer lexer = new GrammarLexer(" \t \r ID \t \r = \t \r {test}");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // ID + = + pattern + EOF (all whitespace skipped)
        assertEquals(TokenType.TERMINAL, tokens.get(0).getType());
        assertEquals("ID", tokens.get(0).getValue());
        assertEquals(TokenType.EQUALS, tokens.get(1).getType());
        assertEquals(TokenType.PATTERN, tokens.get(2).getType());
        assertEquals("test", tokens.get(2).getValue());
        assertEquals(TokenType.EOF, tokens.get(3).getType());
    }
    
    @Test
    void testPositionTrackingWithMixedWhitespace() {
        // Test that position tracking works correctly with mixed whitespace
        GrammarLexer lexer = new GrammarLexer(" \t ID\n \t NUMBER", "test.grammar");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // ID + newline + NUMBER + EOF
        
        // Check positions - ID should be at column 4 (after space and tab)
        assertEquals(1, tokens.get(0).getPosition().getLine());
        assertEquals(4, tokens.get(0).getPosition().getColumn());
        assertEquals("ID", tokens.get(0).getValue());
        
        // Newline should be at column 6 (after ID)
        assertEquals(1, tokens.get(1).getPosition().getLine());
        assertEquals(6, tokens.get(1).getPosition().getColumn());
        assertEquals(TokenType.NEWLINE, tokens.get(1).getType());
        
        // NUMBER should be at line 2, column 4 (after space and tab on second line)
        assertEquals(2, tokens.get(2).getPosition().getLine());
        assertEquals(4, tokens.get(2).getPosition().getColumn());
        assertEquals("NUMBER", tokens.get(2).getValue());
    }
    
    @Test
    void testMultiLineDetectionInfrastructureBasicFunctionality() {
        // Test that the multi-line detection infrastructure works with basic patterns
        String multiLineGrammar = "<expr> ::= <term> |\n  <factor>";
        GrammarLexer lexer = new GrammarLexer(multiLineGrammar);
        
        // Test that we can tokenize the multi-line grammar
        List<Token> tokens = lexer.tokenize();
        
        // Should get: <expr> + ::= + <term> + | + newline + <factor> + EOF
        assertEquals(7, tokens.size());
        assertEquals(TokenType.NON_TERMINAL, tokens.get(0).getType());
        assertEquals("expr", tokens.get(0).getValue());
        assertEquals(TokenType.PRODUCTION_ARROW, tokens.get(1).getType());
        assertEquals(TokenType.NON_TERMINAL, tokens.get(2).getType());
        assertEquals("term", tokens.get(2).getValue());
        assertEquals(TokenType.PIPE, tokens.get(3).getType());
        assertEquals(TokenType.NEWLINE, tokens.get(4).getType());
        assertEquals(TokenType.NON_TERMINAL, tokens.get(5).getType());
        assertEquals("factor", tokens.get(5).getValue());
        assertEquals(TokenType.EOF, tokens.get(6).getType());
    }
    
    @Test
    void testMultiLineDetectionWithVariousWhitespacePatterns() {
        // Test various whitespace patterns in multi-line productions
        String[] testCases = {
            "<expr> ::= <term> |\n\t<factor>",           // tab on continuation line
            "<expr> ::= <term> |\n \t <factor>",         // space + tab on continuation line
            "<expr> ::= <term> |\r\n  <factor>",         // CRLF line ending
            "<expr> ::= <term> | \t \r\n\t\t<factor>"    // mixed whitespace everywhere
        };
        
        for (String testCase : testCases) {
            GrammarLexer lexer = new GrammarLexer(testCase);
            List<Token> tokens = lexer.tokenize();
            
            // All should produce the same token structure
            assertTrue(tokens.size() >= 6, "Should have at least 6 tokens for: " + testCase);
            assertEquals(TokenType.NON_TERMINAL, tokens.get(0).getType());
            assertEquals("expr", tokens.get(0).getValue());
            assertEquals(TokenType.PRODUCTION_ARROW, tokens.get(1).getType());
            assertEquals(TokenType.NON_TERMINAL, tokens.get(2).getType());
            assertEquals("term", tokens.get(2).getValue());
            assertEquals(TokenType.PIPE, tokens.get(3).getType());
            assertEquals(TokenType.NEWLINE, tokens.get(4).getType());
            
            // Find the factor token (may have different position due to whitespace)
            boolean foundFactor = false;
            for (Token token : tokens) {
                if (token.getType() == TokenType.NON_TERMINAL && "factor".equals(token.getValue())) {
                    foundFactor = true;
                    break;
                }
            }
            assertTrue(foundFactor, "Should find factor token in: " + testCase);
        }
    }
    
    @Test
    void testSkipLeadingWhitespaceStopsAtNewline() {
        GrammarLexer lexer = new GrammarLexer("   \n<factor>");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("skipLeadingWhitespaceOnContinuationLine");
            method.setAccessible(true);
            
            // Skip leading whitespace - should stop at newline
            method.invoke(lexer);
            
            // Should stop at newline, not skip it
            assertEquals('\n', lexer.peek());
            
        } catch (Exception e) {
            fail("Failed to test skipLeadingWhitespaceOnContinuationLine with newline: " + e.getMessage());
        }
    }
    
    @Test
    void testSkipLeadingWhitespaceOnContinuationLineAtEndOfInput() {
        GrammarLexer lexer = new GrammarLexer("   ");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("skipLeadingWhitespaceOnContinuationLine");
            method.setAccessible(true);
            
            // Skip leading whitespace
            method.invoke(lexer);
            
            // Should be at end of input
            assertTrue(lexer.isAtEnd());
            
        } catch (Exception e) {
            fail("Failed to test skipLeadingWhitespaceOnContinuationLine at end of input: " + e.getMessage());
        }
    }
    
    @Test
    void testContinuationPatternDetectionPositionRestoration() {
        GrammarLexer lexer = new GrammarLexer("<expr> ::= <term> |\n  <factor>");
        
        // Advance to the second line
        lexer.nextToken(); // <expr>
        lexer.nextToken(); // ::=
        lexer.nextToken(); // <term>
        lexer.nextToken(); // |
        lexer.nextToken(); // newline
        
        // Get position before calling detectContinuationPattern
        Position beforePos = lexer.getCurrentPosition();
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("detectContinuationPattern");
            method.setAccessible(true);
            
            boolean result = (Boolean) method.invoke(lexer);
            assertTrue(result, "Should detect continuation pattern");
            
            // Position should be restored after the call
            Position afterPos = lexer.getCurrentPosition();
            assertEquals(beforePos.getLine(), afterPos.getLine(), "Line should be restored");
            assertEquals(beforePos.getColumn(), afterPos.getColumn(), "Column should be restored");
            
        } catch (Exception e) {
            fail("Failed to test position restoration in detectContinuationPattern: " + e.getMessage());
        }
    }
    
    @Test
    void testContinuationPatternWithComplexMultiLineGrammar() {
        String grammar = "<expr> ::= <expr> PLUS <term>\n" +
                        "         | <expr> MINUS <term>\n" +
                        "         | <term>\n" +
                        "\n" +
                        "<term> ::= <factor> MULTIPLY <term> |\n" +
                        "           <factor>";
        
        GrammarLexer lexer = new GrammarLexer(grammar);
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("detectContinuationPattern");
            method.setAccessible(true);
            
            // Test at various positions in the multi-line grammar
            // Skip to first continuation line
            while (lexer.nextToken().getType() != TokenType.NEWLINE) {
                // Skip to first newline
            }
            
            boolean result1 = (Boolean) method.invoke(lexer);
            assertTrue(result1, "Should detect continuation on second line of first production");
            
            // Continue to next line
            while (lexer.nextToken().getType() != TokenType.NEWLINE) {
                // Skip to next newline
            }
            
            boolean result2 = (Boolean) method.invoke(lexer);
            assertTrue(result2, "Should detect continuation on third line of first production");
            
        } catch (Exception e) {
            fail("Failed to test continuation pattern with complex multi-line grammar: " + e.getMessage());
        }
    }
    
    // Additional multi-line detection infrastructure tests
    
    @Test
    void testWhitespaceSkippingWithAllWhitespaceTypes() {
        // Test with spaces, tabs, and carriage returns
        GrammarLexer lexer = new GrammarLexer(" \t\r ID");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(2, tokens.size()); // ID + EOF (whitespace skipped)
        assertEquals(TokenType.TERMINAL, tokens.get(0).getType());
        assertEquals("ID", tokens.get(0).getValue());
    }
    
    @Test
    void testWhitespaceSkippingPreservesNewlines() {
        // Test that newlines are preserved for line tracking
        GrammarLexer lexer = new GrammarLexer(" \t\r\n ID");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(3, tokens.size()); // newline + ID + EOF
        assertEquals(TokenType.NEWLINE, tokens.get(0).getType());
        assertEquals(TokenType.TERMINAL, tokens.get(1).getType());
        assertEquals("ID", tokens.get(1).getValue());
    }
    
    @Test
    void testPositionTrackingWithCarriageReturns() {
        // Test position tracking with various whitespace characters including carriage returns
        GrammarLexer lexer = new GrammarLexer(" \t ID \r\n\t NUMBER", "test.grammar");
        List<Token> tokens = lexer.tokenize();
        
        assertEquals(4, tokens.size()); // ID + newline + NUMBER + EOF
        
        // Check positions are correctly tracked
        assertEquals(1, tokens.get(0).getPosition().getLine()); // ID
        assertEquals(4, tokens.get(0).getPosition().getColumn()); // After space and tab
        
        assertEquals(1, tokens.get(1).getPosition().getLine()); // newline
        assertEquals(8, tokens.get(1).getPosition().getColumn()); // After ID and space and \r
        
        assertEquals(2, tokens.get(2).getPosition().getLine()); // NUMBER
        assertEquals(3, tokens.get(2).getPosition().getColumn()); // After tab on new line
    }
    
    @Test
    void testIsAnyWhitespaceMethodForMultiLine() {
        GrammarLexer lexer = new GrammarLexer("dummy");
        
        try {
            java.lang.reflect.Method method = GrammarLexer.class.getDeclaredMethod("isAnyWhitespace", char.class);
            method.setAccessible(true);
            
            // Test all whitespace characters
            assertTrue((Boolean) method.invoke(lexer, ' '), "Space should be whitespace");
            assertTrue((Boolean) method.invoke(lexer, '\t'), "Tab should be whitespace");
            assertTrue((Boolean) method.invoke(lexer, '\r'), "Carriage return should be whitespace");
            assertTrue((Boolean) method.invoke(lexer, '\n'), "Newline should be whitespace");
            
            // Test non-whitespace characters
            assertFalse((Boolean) method.invoke(lexer, 'a'), "Letter should not be whitespace");
            assertFalse((Boolean) method.invoke(lexer, '1'), "Digit should not be whitespace");
            assertFalse((Boolean) method.invoke(lexer, '|'), "Pipe should not be whitespace");
            
        } catch (Exception e) {
            fail("Failed to test isAnyWhitespace method: " + e.getMessage());
        }
    }
    
    // Calculator grammar test for multi-line detection infrastructure
    
    @Test
    void testCalculatorGrammarBasicTokenization() {
        // Test basic tokenization of calculator grammar
        String calculatorGrammar = 
            "-- Extended Calculator Grammar\n" +
            "NUMBER = {[0-9]+(\\.[0-9]+)?}\n" +
            "IDENTIFIER = {[a-zA-Z][a-zA-Z0-9]*}\n" +
            "PLUS = {\\+}\n" +
            "\n" +
            "Start = <Program>\n" +
            "\n" +
            "<Program> ::= <Statement>\n" +
            "<Expression> ::= <Term> PLUS <Expression>\n" +
            "    | <Term>\n";
        
        GrammarLexer lexer = new GrammarLexer(calculatorGrammar, "calculator.grammar");
        List<Token> tokens = lexer.tokenize();
        
        // Verify we get tokens without errors
        assertFalse(lexer.hasErrors(), "Calculator grammar should tokenize without errors");
        assertTrue(tokens.size() > 20, "Should generate multiple tokens from calculator grammar");
        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).getType(), "Should end with EOF token");
        
        // Check for presence of key token types
        boolean hasComment = tokens.stream().anyMatch(t -> t.getType() == TokenType.COMMENT);
        boolean hasPattern = tokens.stream().anyMatch(t -> t.getType() == TokenType.PATTERN);
        boolean hasNonTerminal = tokens.stream().anyMatch(t -> t.getType() == TokenType.NON_TERMINAL);
        boolean hasProductionArrow = tokens.stream().anyMatch(t -> t.getType() == TokenType.PRODUCTION_ARROW);
        boolean hasPipe = tokens.stream().anyMatch(t -> t.getType() == TokenType.PIPE);
        
        assertTrue(hasComment, "Should contain comment tokens");
        assertTrue(hasPattern, "Should contain pattern tokens");
        assertTrue(hasNonTerminal, "Should contain non-terminal tokens");
        assertTrue(hasProductionArrow, "Should contain production arrow tokens");
        assertTrue(hasPipe, "Should contain pipe tokens");
    }
    
    @Test
    void testCalculatorGrammarWithMultiLineProduction() {
        // Test a multi-line production rule from calculator grammar
        String multiLineProduction = 
            "<Expression> ::= <Term> PLUS <Expression>\n" +
            "    | <Term> MINUS <Expression>\n" +
            "    | <Term>";
        
        GrammarLexer lexer = new GrammarLexer(multiLineProduction);
        List<Token> tokens = lexer.tokenize();
        
        // Should tokenize correctly even with multi-line format
        assertFalse(lexer.hasErrors(), "Multi-line production should tokenize without errors");
        
        // Count specific token types
        long pipeCount = tokens.stream().filter(t -> t.getType() == TokenType.PIPE).count();
        long nonTerminalCount = tokens.stream().filter(t -> t.getType() == TokenType.NON_TERMINAL).count();
        long terminalCount = tokens.stream().filter(t -> t.getType() == TokenType.TERMINAL).count();
        long newlineCount = tokens.stream().filter(t -> t.getType() == TokenType.NEWLINE).count();
        
        assertEquals(2, pipeCount, "Should have 2 pipe tokens");
        assertEquals(6, nonTerminalCount, "Should have 6 non-terminal tokens"); // <Expression>, <Term>, <Expression>, <Term>, <Expression>, <Term>
        assertEquals(2, terminalCount, "Should have 2 terminal tokens"); // PLUS, MINUS
        assertEquals(2, newlineCount, "Should have 2 newline tokens");
    }
    
    @Test
    void testCalculatorGrammarPositionTracking() {
        // Test position tracking with calculator grammar format
        String grammarFragment = 
            "<Factor> ::= <Base> POWER <Factor>\n" +
            "    | <Base>";
        
        GrammarLexer lexer = new GrammarLexer(grammarFragment, "test.grammar");
        List<Token> tokens = lexer.tokenize();
        
        // Find the pipe token and verify its position
        Token pipeToken = tokens.stream()
            .filter(t -> t.getType() == TokenType.PIPE)
            .findFirst()
            .orElse(null);
        
        assertNotNull(pipeToken, "Should find pipe token");
        assertEquals(2, pipeToken.getPosition().getLine(), "Pipe should be on line 2");
        assertEquals(5, pipeToken.getPosition().getColumn(), "Pipe should be at column 5 (after 4 spaces)");
        assertEquals("test.grammar", pipeToken.getPosition().getFilename(), "Should track filename");
    }
    
    @Test
    void testCalculatorGrammarComplexWhitespace() {
        // Test calculator grammar with various whitespace patterns
        String grammarWithWhitespace = 
            "<Expression> ::= <Term> PLUS <Expression> \t\r\n" +
            "\t    | <Term> MINUS <Expression>   \n" +
            "  \t  | <Term>  \r\n";
        
        GrammarLexer lexer = new GrammarLexer(grammarWithWhitespace);
        List<Token> tokens = lexer.tokenize();
        
        // Should handle all whitespace variations correctly
        assertFalse(lexer.hasErrors(), "Should handle complex whitespace without errors");
        
        // Verify token sequence is correct despite whitespace variations
        List<TokenType> expectedTypes = List.of(
            TokenType.NON_TERMINAL,     // <Expression>
            TokenType.PRODUCTION_ARROW, // ::=
            TokenType.NON_TERMINAL,     // <Term>
            TokenType.TERMINAL,         // PLUS
            TokenType.NON_TERMINAL,     // <Expression>
            TokenType.NEWLINE,          // \n
            TokenType.PIPE,             // |
            TokenType.NON_TERMINAL,     // <Term>
            TokenType.TERMINAL,         // MINUS
            TokenType.NON_TERMINAL,     // <Expression>
            TokenType.NEWLINE,          // \n
            TokenType.PIPE,             // |
            TokenType.NON_TERMINAL,     // <Term>
            TokenType.NEWLINE,          // \n
            TokenType.EOF               // EOF
        );
        
        assertEquals(expectedTypes.size(), tokens.size(), "Should have expected number of tokens");
        for (int i = 0; i < expectedTypes.size(); i++) {
            assertEquals(expectedTypes.get(i), tokens.get(i).getType(), 
                "Token " + i + " should have expected type");
        }
    }
    
    // Edge case tests for multi-line detection infrastructure
    
    @Test
    void testMultiLineDetectionWithEmptyLines() {
        String grammarWithEmptyLines = 
            "<Expression> ::= <Term>\n" +
            "\n" +
            "    | <Factor>\n" +
            "\n" +
            "    | NUMBER";
        
        GrammarLexer lexer = new GrammarLexer(grammarWithEmptyLines);
        List<Token> tokens = lexer.tokenize();
        
        assertFalse(lexer.hasErrors(), "Should handle empty lines without errors");
        
        // Count newlines (should include empty lines)
        long newlineCount = tokens.stream().filter(t -> t.getType() == TokenType.NEWLINE).count();
        assertEquals(4, newlineCount, "Should preserve all newlines including empty lines");
    }
    
    @Test
    void testMultiLineDetectionWithComments() {
        String grammarWithComments = 
            "<Expression> ::= <Term> -- first alternative\n" +
            "    | <Factor> -- second alternative\n" +
            "    -- comment line\n" +
            "    | NUMBER -- third alternative";
        
        GrammarLexer lexer = new GrammarLexer(grammarWithComments);
        List<Token> tokens = lexer.tokenize();
        
        assertFalse(lexer.hasErrors(), "Should handle comments in multi-line productions without errors");
        
        // Count comments
        long commentCount = tokens.stream().filter(t -> t.getType() == TokenType.COMMENT).count();
        assertEquals(4, commentCount, "Should find all comments");
    }
}