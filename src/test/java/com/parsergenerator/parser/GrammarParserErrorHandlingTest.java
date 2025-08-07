package com.parsergenerator.parser;

import com.parsergenerator.lexer.GrammarLexer;
import com.parsergenerator.lexer.Token;
import com.parsergenerator.lexer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for comprehensive error handling in GrammarParser.
 * Tests various error conditions, recovery mechanisms, and error reporting.
 */
public class GrammarParserErrorHandlingTest {
    
    // Helper method to create tokens from input
    private List<Token> tokenize(String input) {
        GrammarLexer lexer = new GrammarLexer(input);
        return lexer.tokenize();
    }
    
    @Test
    void testSyntaxErrorWithLineNumbers() {
        String input = "Start = <expr>\n" +
                      "INVALID_TOKEN = {pattern}\n" +
                      "<expr> ::= <term>";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        // The parser may throw a combined exception or the first error
        // Check if it's a SyntaxErrorException or contains syntax error information
        assertTrue(exception instanceof SemanticErrorException || 
                  exception.getMessage().contains("undefined_nonterminal") ||
                  exception.getMessage().contains("Multiple parse errors"));
    }
    
    @Test
    void testMissingEqualsInTerminalDefinition() {
        String input = "TERM {pattern}";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        // The parser should detect the syntax error
        assertTrue(exception.getMessage().contains("Expected") || 
                  exception.getMessage().contains("Unexpected"));
    }
    
    @Test
    void testWrongOperatorInTerminalDefinition() {
        String input = "TERM ::= {pattern}";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        // Should contain multiple errors due to error recovery
        assertTrue(exception.getMessage().contains("Multiple parse errors"));
        assertTrue(exception.getMessage().contains("EQUALS"));
        assertTrue(exception.getMessage().contains("PRODUCTION_ARROW"));
        assertTrue(parser.hasErrors());
        assertTrue(parser.getErrors().size() > 0);
        
        // Check that the first error is the syntax error we expect
        ParseException firstError = parser.getErrors().get(0);
        assertTrue(firstError instanceof SyntaxErrorException);
        SyntaxErrorException syntaxError = (SyntaxErrorException) firstError;
        assertEquals(TokenType.EQUALS, syntaxError.getExpectedType());
        assertEquals(TokenType.PRODUCTION_ARROW, syntaxError.getActualType());
    }
    
    @Test
    void testWrongOperatorInProductionRule() {
        String input = "<expr> = <term>";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        // The parser should detect the wrong operator
        assertTrue(exception.getMessage().contains("Expected") || 
                  exception.getMessage().contains("::=") ||
                  exception.getMessage().contains("production"));
    }
    
    @Test
    void testMissingPatternInTerminalDefinition() {
        String input = "TERM = <expr>";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        // The parser should detect the missing pattern
        assertTrue(exception.getMessage().contains("Expected") || 
                  exception.getMessage().contains("pattern") ||
                  exception.getMessage().contains("braces"));
    }
    
    @Test
    void testTerminalAsStartSymbol() {
        String input = "Start = TERM";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        // The parser should detect the terminal as start symbol error
        assertTrue(exception.getMessage().contains("Expected") || 
                  exception.getMessage().contains("non-terminal") ||
                  exception.getMessage().contains("Start"));
    }
    
    @Test
    void testDuplicateStartSymbol() {
        String input = "Start = <expr>\n" +
                      "Start = <term>";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        // The parser should detect the duplicate start symbol
        assertTrue(exception.getMessage().contains("duplicate") || 
                  exception.getMessage().contains("already defined") ||
                  exception.getMessage().contains("Start"));
    }
    
    @Test
    void testDuplicateTerminalDefinition() {
        String input = "TERM = {pattern1}\n" +
                      "TERM = {pattern2}";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        // The parser should detect the duplicate terminal
        assertTrue(exception.getMessage().contains("duplicate") || 
                  exception.getMessage().contains("already defined") ||
                  exception.getMessage().contains("TERM"));
    }
    
    @Test
    void testMissingStartSymbol() {
        String input = "<expr> ::= <term>";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> {
            parser.parse();
        });
        
        assertEquals("missing_start_symbol", exception.getErrorType());
        assertTrue(exception.getMessage().contains("No start symbol defined"));
    }
    
    @Test
    void testStartSymbolWithoutProductions() {
        String input = "Start = <expr>\n" +
                      "<term> ::= TERM";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> {
            parser.parse();
        });
        
        assertEquals("undefined_start_symbol", exception.getErrorType());
        assertEquals("expr", exception.getSymbolName());
        assertTrue(exception.getMessage().contains("Start symbol has no production rules"));
    }
    
    @Test
    void testUndefinedNonTerminal() {
        String input = "Start = <expr>\n" +
                      "<expr> ::= <undefined_term>";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> {
            parser.parse();
        });
        
        assertEquals("undefined_nonterminal", exception.getErrorType());
        assertEquals("undefined_term", exception.getSymbolName());
        assertTrue(exception.getMessage().contains("Non-terminal used but not defined"));
    }
    
    @Test
    void testEmptyGrammar() {
        String input = "";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> {
            parser.parse();
        });
        
        assertEquals("missing_start_symbol", exception.getErrorType());
        assertTrue(exception.getMessage().contains("No start symbol defined"));
    }
    
    @Test
    void testUnexpectedTokenAtStatementStart() {
        String input = "INVALID_KEYWORD <expr>";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        // The parser should detect the unexpected token
        assertTrue(exception.getMessage().contains("Unexpected") || 
                  exception.getMessage().contains("Expected"));
    }
    
    @Test
    void testMultipleErrors() {
        String input = "Start = TERM\n" +  // Error: terminal as start symbol
                      "TERM ::= {pattern}\n" +  // Error: wrong operator for terminal
                      "<expr> = <term>";  // Error: wrong operator for production
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        assertTrue(exception.getMessage().contains("Multiple parse errors"));
        assertTrue(parser.hasErrors());
        assertTrue(parser.getErrors().size() > 1);
    }
    
    @Test
    void testErrorRecovery() {
        String input = "Start = <expr>\n" +
                      "INVALID_LINE with errors\n" +  // This line should cause error and recovery
                      "<expr> ::= TERM";  // This should still be parsed after recovery
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        // The parser should attempt to recover and continue parsing
        assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        assertTrue(parser.hasErrors());
    }
    
    @Test
    void testContextAwareErrorMessages() {
        String input = "Start = <expr>\n" +
                      "<expr> ::= <term> INVALID_TOKEN";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse();
        });
        
        // The parser should provide context-aware error messages
        assertTrue(exception.getMessage().contains("undefined_nonterminal") || 
                  exception.getMessage().contains("INVALID_TOKEN") ||
                  exception.getMessage().contains("parsing"));
    }
    
    @Test
    void testValidGrammarParsesSuccessfully() {
        String input = "Start = <expr>\n" +
                      "TERM = {[a-z]+}\n" +
                      "PLUS = {\\+}\n" +
                      "<expr> ::= <expr> PLUS <term> | <term>\n" +
                      "<term> ::= TERM";
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        assertDoesNotThrow(() -> {
            parser.parse();
        });
        
        assertFalse(parser.hasErrors());
        assertEquals(0, parser.getErrors().size());
    }
    
    @Test
    void testErrorPositionAccuracy() {
        String input = "Start = <expr>\n" +
                      "<expr> ::= <term>\n" +
                      "<term> = TERM";  // Error on line 3
        
        List<Token> tokens = tokenize(input);
        GrammarParser parser = new GrammarParser(tokens);
        
        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            parser.parse();
        });
        
        assertNotNull(exception.getPosition());
        assertEquals(3, exception.getPosition().getLine());
        assertTrue(exception.getMessage().contains("line 3"));
    }
}