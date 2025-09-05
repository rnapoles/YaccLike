package com.parsergenerator.codegen;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.lalr.ParsingTable;
import com.parsergenerator.lalr.Action;
import com.parsergenerator.symbol.Terminal;
import com.parsergenerator.symbol.NonTerminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for error recovery mechanisms in generated parsers.
 * Tests various error scenarios and recovery strategies.
 */
class ErrorRecoveryTest {
    
    private JavaCodeGenerator generator;
    private Grammar simpleGrammar;
    private ParsingTable simpleParsingTable;
    
    @BeforeEach
    void setUp() {
        generator = new JavaCodeGenerator("com.test.errorrecovery", "SimpleGrammar");
        setupSimpleGrammar();
        setupSimpleParsingTable();
    }
    
    private void setupSimpleGrammar() {
        simpleGrammar = new Grammar();
        
        // Create a simple grammar: S -> a S b | c
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        Terminal c = new Terminal("c");
        
        NonTerminal s = new NonTerminal("<S>");
        
        simpleGrammar.addTerminal(a);
        simpleGrammar.addTerminal(b);
        simpleGrammar.addTerminal(c);
        simpleGrammar.addNonTerminal(s);
        
        // Productions: S -> a S b, S -> c
        Production p1 = new Production(s, Arrays.asList(a, s, b));
        Production p2 = new Production(s, Arrays.asList(c));
        
        simpleGrammar.addProduction(p1);
        simpleGrammar.addProduction(p2);
        simpleGrammar.setStartSymbol(s);
    }
    
    private void setupSimpleParsingTable() {
        simpleParsingTable = new ParsingTable();
        
        // Create minimal parsing table for testing
        Terminal a = simpleGrammar.getTerminals().stream()
                .filter(t -> t.getName().equals("a"))
                .findFirst().orElse(null);
        Terminal c = simpleGrammar.getTerminals().stream()
                .filter(t -> t.getName().equals("c"))
                .findFirst().orElse(null);
        
        if (a != null && c != null) {
            simpleParsingTable.setAction(0, a, Action.shift(1));
            simpleParsingTable.setAction(0, c, Action.shift(2));
            
            // Add some reduce actions
            Production cProduction = simpleGrammar.getProductions().stream()
                    .filter(p -> p.getRightSide().size() == 1 && p.getRightSide().get(0).equals(c))
                    .findFirst().orElse(null);
            if (cProduction != null) {
                Terminal b = simpleGrammar.getTerminals().stream()
                        .filter(t -> t.getName().equals("b"))
                        .findFirst().orElse(null);
                if (b != null) {
                    simpleParsingTable.setAction(2, b, Action.reduce(cProduction));
                }
            }
        }
    }
    
    @Test
    @DisplayName("Generated parser should include error recovery infrastructure")
    void testErrorRecoveryInfrastructure() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        assertNotNull(parserCode);
        
        // Check for error recovery fields
        assertTrue(parserCode.contains("private List<ParseError> errors"));
        assertTrue(parserCode.contains("private boolean errorRecoveryMode"));
        assertTrue(parserCode.contains("SYNC_TOKENS"));
        
        // Check for error recovery methods
        assertTrue(parserCode.contains("private boolean handleError"));
        assertTrue(parserCode.contains("private boolean panicModeRecovery"));
        assertTrue(parserCode.contains("private boolean phraseLevelRecovery"));
        assertTrue(parserCode.contains("private boolean tryInsertToken"));
    }
    
    @Test
    @DisplayName("Generated parser should support parsing with error recovery enabled")
    void testParseWithErrorRecovery() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check for overloaded parse method
        assertTrue(parserCode.contains("public ASTNode parse(List<Token> tokens)"));
        assertTrue(parserCode.contains("public ASTNode parse(List<Token> tokens, boolean enableErrorRecovery)"));
        
        // Check that error recovery is properly handled
        assertTrue(parserCode.contains("if (enableErrorRecovery)"));
        assertTrue(parserCode.contains("if (!handleError(currentToken))"));
    }
    
    @Test
    @DisplayName("Generated parser should provide error reporting methods")
    void testErrorReportingMethods() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check for error reporting methods
        assertTrue(parserCode.contains("public List<ParseError> getErrors()"));
        assertTrue(parserCode.contains("public boolean hasErrors()"));
        assertTrue(parserCode.contains("private String createErrorMessage"));
        assertTrue(parserCode.contains("private List<String> getExpectedTokens"));
        assertTrue(parserCode.contains("private String formatErrors()"));
    }
    
    @Test
    @DisplayName("Generated ParseError class should contain position information")
    void testParseErrorClass() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check for ParseError inner class
        assertTrue(parserCode.contains("public static class ParseError"));
        assertTrue(parserCode.contains("private final String message"));
        assertTrue(parserCode.contains("private final Position position"));
        assertTrue(parserCode.contains("public String getMessage()"));
        assertTrue(parserCode.contains("public Position getPosition()"));
    }
    
    @Test
    @DisplayName("Generated parser should handle panic mode recovery")
    void testPanicModeRecovery() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check panic mode recovery implementation
        assertTrue(parserCode.contains("private boolean panicModeRecovery()"));
        assertTrue(parserCode.contains("while (currentTokenIndex < tokens.size())"));
        assertTrue(parserCode.contains("if (SYNC_TOKENS.contains(token.getType()))"));
        assertTrue(parserCode.contains("currentTokenIndex++"));
    }
    
    @Test
    @DisplayName("Generated parser should handle phrase-level recovery")
    void testPhraseLevelRecovery() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check phrase-level recovery implementation
        assertTrue(parserCode.contains("private boolean phraseLevelRecovery"));
        assertTrue(parserCode.contains("if (tryInsertToken())"));
        assertTrue(parserCode.contains("currentTokenIndex++"));
    }
    
    @Test
    @DisplayName("Generated parser should attempt token insertion for recovery")
    void testTokenInsertion() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check token insertion implementation
        assertTrue(parserCode.contains("private boolean tryInsertToken()"));
        assertTrue(parserCode.contains("for (TokenType tokenType : TokenType.values())"));
        assertTrue(parserCode.contains("Token dummyToken = new Token"));
        assertTrue(parserCode.contains("Inserted missing"));
    }
    
    @Test
    @DisplayName("Generated parser should create detailed error messages")
    void testDetailedErrorMessages() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check error message creation
        assertTrue(parserCode.contains("Syntax error at line"));
        assertTrue(parserCode.contains("unexpected token"));
        assertTrue(parserCode.contains("Expected one of:"));
        assertTrue(parserCode.contains("token.getPosition().getLine()"));
        assertTrue(parserCode.contains("token.getPosition().getColumn()"));
    }
    
    @Test
    @DisplayName("Generated parser should identify expected tokens")
    void testExpectedTokensIdentification() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check expected tokens identification
        assertTrue(parserCode.contains("private List<String> getExpectedTokens(int state)"));
        assertTrue(parserCode.contains("if (actions[i] != 0)"));
        assertTrue(parserCode.contains("TokenType tokenType = TokenType.values()[i]"));
        assertTrue(parserCode.contains("expected.add(tokenType.name())"));
    }
    
    @Test
    @DisplayName("Generated parser should handle synchronization tokens")
    void testSynchronizationTokens() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check synchronization tokens setup
        assertTrue(parserCode.contains("private static final Set<TokenType> SYNC_TOKENS"));
        assertTrue(parserCode.contains("EnumSet.of"));
        assertTrue(parserCode.contains("TokenType.EOF"));
    }
    
    @Test
    @DisplayName("Generated parser should maintain error recovery state")
    void testErrorRecoveryState() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check error recovery state management
        assertTrue(parserCode.contains("errorRecoveryMode = true"));
        assertTrue(parserCode.contains("errorRecoveryMode = false"));
        assertTrue(parserCode.contains("this.errorRecoveryMode = false"));
    }
    
    @Test
    @DisplayName("Generated parser should collect multiple errors")
    void testMultipleErrorCollection() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check error collection
        assertTrue(parserCode.contains("errors.add(new ParseError"));
        assertTrue(parserCode.contains("this.errors.clear()"));
        assertTrue(parserCode.contains("new ArrayList<>(errors)"));
    }
    
    @Test
    @DisplayName("Generated parser should handle EOF gracefully")
    void testEOFHandling() throws TemplateException {
        GeneratedCode result = generator.generate(simpleGrammar, simpleParsingTable);
        String parserCode = result.getFileContent("SimpleGrammarParser.java");
        
        // Check EOF handling
        assertTrue(parserCode.contains("if (currentToken.getType() == TokenType.EOF"));
        assertTrue(parserCode.contains("currentTokenIndex >= tokens.size()"));
        assertTrue(parserCode.contains("Unexpected end of input"));
    }
}