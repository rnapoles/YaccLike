package com.parsergenerator.parser;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.lexer.GrammarLexer;
import com.parsergenerator.lexer.Token;
import com.parsergenerator.lexer.TokenType;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the GrammarParser class.
 * Tests parsing of various grammar constructs including terminal definitions,
 * production rules, start symbols, and error handling.
 */
class GrammarParserTest {
    
    private GrammarParser parser;
    
    @BeforeEach
    void setUp() {
        // Reset production ID counter for consistent test results
        Production.resetIdCounter();
    }
    
    @Test
    void testParseEmptyGrammar() throws ParseException {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        // Empty grammar should throw SemanticErrorException for missing start symbol
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> parser.parse());
        assertEquals("missing_start_symbol", exception.getErrorType());
        assertTrue(exception.getMessage().contains("No start symbol defined"));
    }
    
    @Test
    void testParseStartSymbol() throws ParseException {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.START, "Start"),
            new Token(TokenType.EQUALS, "="),
            new Token(TokenType.NON_TERMINAL, "expr"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        // Start symbol without productions should throw SemanticErrorException
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> parser.parse());
        assertEquals("undefined_start_symbol", exception.getErrorType());
        assertEquals("expr", exception.getSymbolName());
        assertTrue(exception.getMessage().contains("Start symbol has no production rules"));
    }
    
    @Test
    void testParseTerminalDefinition() throws ParseException {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.TERMINAL, "ID"),
            new Token(TokenType.EQUALS, "="),
            new Token(TokenType.PATTERN, "[a-zA-Z][a-zA-Z0-9]*"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        // Terminal definition without start symbol should throw SemanticErrorException
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> parser.parse());
        assertEquals("missing_start_symbol", exception.getErrorType());
        assertTrue(exception.getMessage().contains("No start symbol defined"));
    }
    
    @Test
    void testParseTerminalDefinitionWithPlus() throws ParseException {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.TERMINAL, "NUMBER"),
            new Token(TokenType.EQUALS, "="),
            new Token(TokenType.PATTERN, "[0-9]+"),
            new Token(TokenType.PLUS, "+"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        // Terminal definition without start symbol should throw SemanticErrorException
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> parser.parse());
        assertEquals("missing_start_symbol", exception.getErrorType());
        assertTrue(exception.getMessage().contains("No start symbol defined"));
    }
    
    @Test
    void testParseSimpleProductionRule() throws ParseException {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.NON_TERMINAL, "expr"),
            new Token(TokenType.PRODUCTION_ARROW, "::="),
            new Token(TokenType.TERMINAL, "ID"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        // Production rule without start symbol should throw SemanticErrorException
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> parser.parse());
        assertEquals("missing_start_symbol", exception.getErrorType());
        assertTrue(exception.getMessage().contains("No start symbol defined"));
    }
    
    @Test
    void testParseProductionRuleWithMultipleSymbols() throws ParseException {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.NON_TERMINAL, "expr"),
            new Token(TokenType.PRODUCTION_ARROW, "::="),
            new Token(TokenType.NON_TERMINAL, "expr"),
            new Token(TokenType.TERMINAL, "PLUS"),
            new Token(TokenType.NON_TERMINAL, "term"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        // Production rule without start symbol should throw SemanticErrorException
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> parser.parse());
        assertEquals("missing_start_symbol", exception.getErrorType());
        assertTrue(exception.getMessage().contains("No start symbol defined"));
    }
    
    @Test
    void testParseProductionRuleWithAlternatives() throws ParseException {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.NON_TERMINAL, "expr"),
            new Token(TokenType.PRODUCTION_ARROW, "::="),
            new Token(TokenType.TERMINAL, "ID"),
            new Token(TokenType.PIPE, "|"),
            new Token(TokenType.TERMINAL, "NUMBER"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        // Production rule without start symbol should throw SemanticErrorException
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> parser.parse());
        assertEquals("missing_start_symbol", exception.getErrorType());
        assertTrue(exception.getMessage().contains("No start symbol defined"));
    }
    
    @Test
    void testParseCompleteGrammar() throws ParseException {
        String grammarText = 
            "ID = {[a-zA-Z][a-zA-Z0-9]*}\n" +
            "NUMBER = {[0-9]+}\n" +
            "PLUS = {\\+}\n" +
            "Start = <expr>\n" +
            "<expr> ::= <expr> PLUS <term> | <term>\n" +
            "<term> ::= ID | NUMBER\n";
        
        GrammarLexer lexer = new GrammarLexer(grammarText);
        List<Token> tokens = lexer.tokenize();
        
        parser = new GrammarParser(tokens);
        Grammar grammar = parser.parse();
        
        // Check start symbol
        assertNotNull(grammar.getStartSymbol());
        assertEquals("expr", grammar.getStartSymbol().getName());
        
        // Check terminals
        assertEquals(3, grammar.getTerminalCount());
        assertNotNull(grammar.getTerminal("ID"));
        assertNotNull(grammar.getTerminal("NUMBER"));
        assertNotNull(grammar.getTerminal("PLUS"));
        
        // Check non-terminals
        assertEquals(2, grammar.getNonTerminalCount());
        assertNotNull(grammar.getNonTerminal("expr"));
        assertNotNull(grammar.getNonTerminal("term"));
        
        // Check productions
        assertEquals(4, grammar.getProductionCount());
        
        NonTerminal expr = grammar.getNonTerminal("expr");
        assertEquals(2, expr.getProductionCount());
        
        NonTerminal term = grammar.getNonTerminal("term");
        assertEquals(2, term.getProductionCount());
    }
    
    @Test
    void testParseWithComments() throws ParseException {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.COMMENT, " This is a comment"),
            new Token(TokenType.NEWLINE, "\n"),
            new Token(TokenType.TERMINAL, "ID"),
            new Token(TokenType.EQUALS, "="),
            new Token(TokenType.PATTERN, "[a-zA-Z]+"),
            new Token(TokenType.COMMENT, " Another comment"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        // Terminal definition without start symbol should throw SemanticErrorException
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> parser.parse());
        assertEquals("missing_start_symbol", exception.getErrorType());
        assertTrue(exception.getMessage().contains("No start symbol defined"));
    }
    
    @Test
    void testParseWithNewlines() throws ParseException {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.TERMINAL, "ID"),
            new Token(TokenType.EQUALS, "="),
            new Token(TokenType.PATTERN, "[a-zA-Z]+"),
            new Token(TokenType.NEWLINE, "\n"),
            new Token(TokenType.NEWLINE, "\n"),
            new Token(TokenType.START, "Start"),
            new Token(TokenType.EQUALS, "="),
            new Token(TokenType.NON_TERMINAL, "expr"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        // Start symbol without productions should throw SemanticErrorException
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> parser.parse());
        assertEquals("undefined_start_symbol", exception.getErrorType());
        assertEquals("expr", exception.getSymbolName());
        assertTrue(exception.getMessage().contains("Start symbol has no production rules"));
    }
    
    @Test
    void testParseEmptyRightHandSide() throws ParseException {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.NON_TERMINAL, "empty"),
            new Token(TokenType.PRODUCTION_ARROW, "::="),
            new Token(TokenType.NEWLINE, "\n"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        // Production rule without start symbol should throw SemanticErrorException
        SemanticErrorException exception = assertThrows(SemanticErrorException.class, () -> parser.parse());
        assertEquals("missing_start_symbol", exception.getErrorType());
        assertTrue(exception.getMessage().contains("No start symbol defined"));
    }
    
    @Test
    void testParseErrorMissingEquals() {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.START, "Start"),
            new Token(TokenType.NON_TERMINAL, "expr"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse());
        assertTrue(exception.getMessage().contains("Expected '=' after 'Start'"));
    }
    
    @Test
    void testParseErrorMissingProductionArrow() {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.NON_TERMINAL, "expr"),
            new Token(TokenType.TERMINAL, "ID"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse());
        assertTrue(exception.getMessage().contains("Expected '::=' after non-terminal"));
    }
    
    @Test
    void testParseErrorMissingPattern() {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.TERMINAL, "ID"),
            new Token(TokenType.EQUALS, "="),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse());
        assertTrue(exception.getMessage().contains("Expected pattern after '='"));
    }
    
    @Test
    void testParseErrorUnexpectedToken() {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.EQUALS, "="),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse());
        assertTrue(exception.getMessage().contains("Unexpected token"));
    }
    
    @Test
    void testParseErrorUnexpectedTerminal() {
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.TERMINAL, "ID"),
            new Token(TokenType.TERMINAL, "NUMBER"),
            new Token(TokenType.EOF, "")
        );
        
        parser = new GrammarParser(tokens);
        
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse());
        assertTrue(exception.getMessage().contains("Unexpected terminal symbol"));
    }
    
    @Test
    void testConstructorWithNullTokens() {
        assertThrows(IllegalArgumentException.class, () -> new GrammarParser(null));
    }
    
    @Test
    void testParseArithmeticGrammar() throws ParseException {
        String grammarText = 
            "-- Arithmetic expression grammar\n" +
            "ID = {[a-zA-Z][a-zA-Z0-9]*}\n" +
            "NUMBER = {[0-9]+}\n" +
            "PLUS = {\\+}\n" +
            "MINUS = {-}\n" +
            "MULTIPLY = {\\*}\n" +
            "DIVIDE = {/}\n" +
            "LPAREN = {\\(}\n" +
            "RPAREN = {\\)}\n" +
            "\n" +
            "Start = <expression>\n" +
            "\n" +
            "<expression> ::= <expression> PLUS <term> | <expression> MINUS <term> | <term>\n" +
            "<term> ::= <term> MULTIPLY <factor> | <term> DIVIDE <factor> | <factor>\n" +
            "<factor> ::= LPAREN <expression> RPAREN | ID | NUMBER\n";
        
        GrammarLexer lexer = new GrammarLexer(grammarText);
        List<Token> tokens = lexer.tokenize();
        
        parser = new GrammarParser(tokens);
        Grammar grammar = parser.parse();
        
        // Verify the grammar structure
        assertNotNull(grammar.getStartSymbol());
        assertEquals("expression", grammar.getStartSymbol().getName());
        
        assertEquals(8, grammar.getTerminalCount());
        assertEquals(3, grammar.getNonTerminalCount());
        assertEquals(9, grammar.getProductionCount());
        
        // Verify specific productions exist
        NonTerminal expression = grammar.getNonTerminal("expression");
        assertEquals(3, expression.getProductionCount());
        
        NonTerminal term = grammar.getNonTerminal("term");
        assertEquals(3, term.getProductionCount());
        
        NonTerminal factor = grammar.getNonTerminal("factor");
        assertEquals(3, factor.getProductionCount());
    }
}