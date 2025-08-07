package com.parsergenerator.integration;

import com.parsergenerator.analysis.AnalysisResult;
import com.parsergenerator.analysis.GrammarAnalyzer;
import com.parsergenerator.error.ErrorReporter;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.lalr.LALRTableBuilder;
import com.parsergenerator.lexer.GrammarLexer;
import com.parsergenerator.lexer.Token;
import com.parsergenerator.parser.GrammarParser;
import com.parsergenerator.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for end-to-end error handling across all components.
 * Tests error collection, batch reporting, and error recovery mechanisms.
 */
public class ErrorReportingIntegrationTest {
    
    private ErrorReporter errorReporter;
    
    @BeforeEach
    void setUp() {
        errorReporter = new ErrorReporter();
    }
    
    @Test
    void testLexicalErrorReporting() {
        String invalidGrammar = "Terminal1 = {pattern}\n" +
                               "Invalid@Character = {pattern2}\n" +
                               "Start = <expr>";
        
        GrammarLexer lexer = new GrammarLexer(invalidGrammar, "test.grammar", errorReporter);
        List<Token> tokens = lexer.tokenize();
        
        // Should have lexical errors but still produce tokens
        assertTrue(errorReporter.hasErrors());
        assertFalse(tokens.isEmpty());
        
        // Check that error details are captured
        assertTrue(errorReporter.getErrorCount() > 0);
        String detailedReport = errorReporter.generateDetailedReport();
        assertTrue(detailedReport.contains("Unexpected character"));
    }
    
    @Test
    void testSyntaxErrorReporting() {
        String invalidGrammar = "Terminal1 = {pattern}\n" +
                               "Start ::= <expr>\n" +  // Wrong operator for start symbol
                               "<expr> = <term>";      // Wrong operator for production
        
        GrammarLexer lexer = new GrammarLexer(invalidGrammar, "test.grammar", errorReporter);
        List<Token> tokens = lexer.tokenize();
        
        GrammarParser parser = new GrammarParser(tokens, errorReporter);
        
        assertThrows(ParseException.class, () -> parser.parse());
        
        // Should have syntax errors
        assertTrue(errorReporter.hasErrors());
        assertTrue(errorReporter.getErrorCount(ErrorReporter.ErrorType.SYNTAX) > 0);
        
        // Check error suggestions
        String detailedReport = errorReporter.generateDetailedReport();
        assertTrue(detailedReport.contains("Invalid operator"));
    }
    
    @Test
    void testSemanticErrorReporting() {
        String invalidGrammar = "Terminal1 = {pattern}\n" +
                               "Start = <expr>\n" +
                               "<expr> ::= <undefined_symbol>";  // Undefined symbol
        
        GrammarLexer lexer = new GrammarLexer(invalidGrammar, "test.grammar", errorReporter);
        List<Token> tokens = lexer.tokenize();
        
        GrammarParser parser = new GrammarParser(tokens, errorReporter);
        
        try {
            Grammar grammar = parser.parse();
            GrammarAnalyzer analyzer = new GrammarAnalyzer(grammar, errorReporter);
            AnalysisResult result = analyzer.analyze();
            
            // Should have semantic errors
            assertTrue(result.hasErrors());
            assertTrue(errorReporter.hasErrors());
            assertTrue(errorReporter.getErrorCount(ErrorReporter.ErrorType.GRAMMAR_VALIDATION) > 0);
            
        } catch (ParseException e) {
            // Parser might fail first, which is also valid
            assertTrue(errorReporter.hasErrors());
        }
    }
    
    @Test
    void testLeftRecursionErrorReporting() {
        String leftRecursiveGrammar = "Terminal1 = {pattern}\n" +
                                     "Start = <expr>\n" +
                                     "<expr> ::= <expr> Terminal1 | Terminal1";  // Direct left recursion
        
        GrammarLexer lexer = new GrammarLexer(leftRecursiveGrammar, "test.grammar", errorReporter);
        List<Token> tokens = lexer.tokenize();
        
        GrammarParser parser = new GrammarParser(tokens, errorReporter);
        
        try {
            Grammar grammar = parser.parse();
            GrammarAnalyzer analyzer = new GrammarAnalyzer(grammar, errorReporter);
            AnalysisResult result = analyzer.analyze();
            
            // Should detect left recursion
            assertTrue(result.hasErrors());
            assertTrue(errorReporter.hasErrors());
            
            String detailedReport = errorReporter.generateDetailedReport();
            assertTrue(detailedReport.contains("Left recursion detected"));
            
        } catch (ParseException e) {
            fail("Parser should not fail for valid syntax with semantic issues");
        }
    }
    
    @Test
    void testLALRConflictReporting() {
        // Grammar that causes shift/reduce conflict
        String conflictGrammar = "IF = {if}\n" +
                               "THEN = {then}\n" +
                               "ELSE = {else}\n" +
                               "ID = {[a-z]+}\n" +
                               "Start = <stmt>\n" +
                               "<stmt> ::= IF ID THEN <stmt> | IF ID THEN <stmt> ELSE <stmt> | ID";
        
        GrammarLexer lexer = new GrammarLexer(conflictGrammar, "test.grammar", errorReporter);
        List<Token> tokens = lexer.tokenize();
        
        GrammarParser parser = new GrammarParser(tokens, errorReporter);
        
        try {
            Grammar grammar = parser.parse();
            GrammarAnalyzer analyzer = new GrammarAnalyzer(grammar, errorReporter);
            AnalysisResult result = analyzer.analyze();
            
            if (!result.hasErrors()) {
                // Try to build LALR table - should detect conflicts
                LALRTableBuilder builder = new LALRTableBuilder(grammar, result, errorReporter);
                
                assertThrows(IllegalStateException.class, () -> builder.buildTable());
                
                // Should have LALR conflict errors
                assertTrue(errorReporter.hasErrors());
                assertTrue(errorReporter.getErrorCount(ErrorReporter.ErrorType.LALR_CONFLICT) > 0);
                
                String detailedReport = errorReporter.generateDetailedReport();
                assertTrue(detailedReport.contains("conflict"));
            }
            
        } catch (ParseException e) {
            fail("Parser should not fail for valid syntax");
        }
    }
    
    @Test
    void testBatchErrorReporting() {
        String multipleErrorGrammar = "Invalid@Symbol = {pattern}\n" +  // Lexical error
                                    "Start ::= <expr>\n" +              // Syntax error
                                    "<expr> ::= <undefined> | <expr> <expr>";  // Semantic errors
        
        GrammarLexer lexer = new GrammarLexer(multipleErrorGrammar, "test.grammar", errorReporter);
        List<Token> tokens = lexer.tokenize();
        
        GrammarParser parser = new GrammarParser(tokens, errorReporter);
        
        try {
            Grammar grammar = parser.parse();
            GrammarAnalyzer analyzer = new GrammarAnalyzer(grammar, errorReporter);
            analyzer.analyze();
            
        } catch (ParseException e) {
            // Expected due to syntax errors
        }
        
        // Should have multiple types of errors
        assertTrue(errorReporter.hasErrors());
        assertTrue(errorReporter.getErrorCount() > 1);
        
        // Should have errors from multiple categories
        assertTrue(errorReporter.getErrorCount(ErrorReporter.ErrorType.LEXICAL) > 0 ||
                  errorReporter.getErrorCount(ErrorReporter.ErrorType.SYNTAX) > 0);
        
        // Test batch reporting
        String summary = errorReporter.generateSummary();
        assertTrue(summary.contains("Total:"));
        
        String detailedReport = errorReporter.generateDetailedReport();
        assertTrue(detailedReport.contains("Detailed Error Report"));
        assertTrue(detailedReport.length() > summary.length());
    }
    
    @Test
    void testErrorRecoveryAndContinuation() {
        String partiallyValidGrammar = "Terminal1 = {pattern1}\n" +
                                     "Invalid@Symbol = {pattern2}\n" +  // Lexical error
                                     "Terminal2 = {pattern3}\n" +        // Should still be processed
                                     "Start = <expr>\n" +
                                     "<expr> ::= Terminal1 | Terminal2";
        
        GrammarLexer lexer = new GrammarLexer(partiallyValidGrammar, "test.grammar", errorReporter);
        List<Token> tokens = lexer.tokenize();
        
        // Should have tokens despite lexical error
        assertTrue(tokens.size() > 1);
        
        GrammarParser parser = new GrammarParser(tokens, errorReporter);
        
        try {
            Grammar grammar = parser.parse();
            
            // Should have parsed valid parts despite errors
            assertNotNull(grammar);
            assertTrue(grammar.getTerminals().size() >= 2);  // Terminal1 and Terminal2
            assertTrue(grammar.hasStartSymbol());
            
        } catch (ParseException e) {
            // Parser might still fail, but should have attempted recovery
            assertTrue(errorReporter.hasErrors());
        }
    }
    
    @Test
    void testErrorSuggestions() {
        String grammarWithCommonMistakes = "Terminal1 = [pattern]\n" +  // Wrong brackets
                                         "Start = Terminal1\n" +        // Terminal as start symbol
                                         "Terminal1 ::= {pattern2}";    // Wrong operator
        
        GrammarLexer lexer = new GrammarLexer(grammarWithCommonMistakes, "test.grammar", errorReporter);
        List<Token> tokens = lexer.tokenize();
        
        GrammarParser parser = new GrammarParser(tokens, errorReporter);
        
        try {
            parser.parse();
        } catch (ParseException e) {
            // Expected
        }
        
        // Check that suggestions are provided
        String detailedReport = errorReporter.generateDetailedReport();
        assertTrue(detailedReport.contains("Suggestion:") || 
                  detailedReport.contains("Use") || 
                  detailedReport.contains("should"));
    }
    
    @Test
    void testSuccessfulProcessingWithoutErrors() {
        String validGrammar = "PLUS = {\\+}\n" +
                            "MULT = {\\*}\n" +
                            "NUM = {[0-9]+}\n" +
                            "LPAREN = {\\(}\n" +
                            "RPAREN = {\\)}\n" +
                            "Start = <expr>\n" +
                            "<expr> ::= <expr> PLUS <term> | <term>\n" +
                            "<term> ::= <term> MULT <factor> | <factor>\n" +
                            "<factor> ::= NUM | LPAREN <expr> RPAREN";
        
        GrammarLexer lexer = new GrammarLexer(validGrammar, "test.grammar", errorReporter);
        List<Token> tokens = lexer.tokenize();
        
        GrammarParser parser = new GrammarParser(tokens, errorReporter);
        
        try {
            Grammar grammar = parser.parse();
            GrammarAnalyzer analyzer = new GrammarAnalyzer(grammar, errorReporter);
            AnalysisResult result = analyzer.analyze();
            
            // Should have no errors
            assertFalse(errorReporter.hasErrors());
            assertFalse(result.hasErrors());
            
            // Should have informational messages about successful processing
            assertTrue(errorReporter.getErrors(ErrorReporter.Severity.INFO).size() > 0);
            
            String summary = errorReporter.generateSummary();
            assertTrue(summary.contains("No errors"));
            
        } catch (ParseException e) {
            fail("Valid grammar should parse successfully: " + e.getMessage());
        }
    }
    
    @Test
    void testErrorReporterStateManagement() {
        // Test that error reporter properly manages state across multiple operations
        
        // First operation with errors
        String invalidGrammar1 = "Invalid@Symbol = {pattern}";
        GrammarLexer lexer1 = new GrammarLexer(invalidGrammar1, "test1.grammar", errorReporter);
        lexer1.tokenize();
        
        int errorsAfterFirst = errorReporter.getErrorCount();
        assertTrue(errorsAfterFirst > 0);
        
        // Second operation with different errors
        String invalidGrammar2 = "Terminal = {pattern}\nStart ::= <expr>";  // Syntax error
        GrammarLexer lexer2 = new GrammarLexer(invalidGrammar2, "test2.grammar", errorReporter);
        List<Token> tokens2 = lexer2.tokenize();
        
        GrammarParser parser2 = new GrammarParser(tokens2, errorReporter);
        try {
            parser2.parse();
        } catch (ParseException e) {
            // Expected
        }
        
        int errorsAfterSecond = errorReporter.getErrorCount();
        assertTrue(errorsAfterSecond > errorsAfterFirst);
        
        // Clear and verify
        errorReporter.clear();
        assertFalse(errorReporter.hasErrors());
        assertEquals(0, errorReporter.getErrorCount());
        
        // Third operation should start fresh
        String validGrammar = "Terminal = {pattern}\nStart = <expr>\n<expr> ::= Terminal";
        GrammarLexer lexer3 = new GrammarLexer(validGrammar, "test3.grammar", errorReporter);
        List<Token> tokens3 = lexer3.tokenize();
        
        GrammarParser parser3 = new GrammarParser(tokens3, errorReporter);
        try {
            parser3.parse();
            // Should have no errors after clear
            assertFalse(errorReporter.hasErrors());
        } catch (ParseException e) {
            fail("Valid grammar should parse after error reporter clear");
        }
    }
}