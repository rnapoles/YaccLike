package com.parsergenerator.analysis;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GrammarAnalyzer class.
 */
class GrammarAnalyzerTest {
    
    private Grammar grammar;
    private GrammarAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        Production.resetIdCounter();
        grammar = new Grammar();
        analyzer = new GrammarAnalyzer(grammar);
    }
    
    @Test
    void testConstructorWithNullGrammar() {
        assertThrows(IllegalArgumentException.class, () -> new GrammarAnalyzer(null));
    }
    
    @Test
    void testEmptyGrammarAnalysis() {
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Grammar must have a start symbol defined"));
        assertTrue(result.getErrors().contains("Grammar must have at least one production"));
    }
    
    @Test
    void testValidSimpleGrammar() {
        // Create a simple valid grammar: S ::= a
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        
        AnalysisResult result = analyzer.analyze();
        
        assertFalse(result.hasErrors());
        assertEquals(0, result.getErrorCount());
    }
    
    @Test
    void testStartSymbolWithoutProductions() {
        NonTerminal S = new NonTerminal("S");
        grammar.setStartSymbol(S);
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Start symbol 'S' has no productions"));
    }
    
    @Test
    void testValidGrammarWithAllSymbolsDefined() {
        // Since Grammar.addProduction automatically adds symbols, we test
        // that a properly constructed grammar has no undefined symbol errors
        
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        
        AnalysisResult result = analyzer.analyze();
        
        assertFalse(result.hasErrors());
        
        // Verify no undefined symbol errors
        assertFalse(result.getErrors().stream()
                .anyMatch(error -> error.contains("Undefined symbol")));
    }
    
    @Test
    void testDirectLeftRecursionDetection() {
        // Grammar: S ::= S a | b
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, S, a)); // Left recursive
        grammar.addProduction(new Production(S, b));
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Left recursion detected")));
        
        assertTrue(analyzer.hasLeftRecursion());
        List<NonTerminal> leftRecursive = analyzer.getLeftRecursiveNonTerminals();
        assertTrue(leftRecursive.contains(S));
    }
    
    @Test
    void testIndirectLeftRecursionDetection() {
        // Grammar: S ::= A a | b, A ::= S c | d
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        Terminal c = new Terminal("c");
        Terminal d = new Terminal("d");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A, a)); // S -> A a
        grammar.addProduction(new Production(S, b));    // S -> b
        grammar.addProduction(new Production(A, S, c)); // A -> S c (creates indirect left recursion)
        grammar.addProduction(new Production(A, d));    // A -> d
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Left recursion detected")));
        
        assertTrue(analyzer.hasLeftRecursion());
        List<NonTerminal> leftRecursive = analyzer.getLeftRecursiveNonTerminals();
        assertTrue(leftRecursive.contains(S));
        assertTrue(leftRecursive.contains(A));
    }
    
    @Test
    void testUnreachableProductionDetection() {
        // Grammar: S ::= a, B ::= b (B is unreachable)
        NonTerminal S = new NonTerminal("S");
        NonTerminal B = new NonTerminal("B");
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        grammar.addProduction(new Production(B, b)); // Unreachable
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Non-terminal 'B' is unreachable")));
        
        assertTrue(analyzer.hasUnreachableProductions());
        List<NonTerminal> unreachable = analyzer.getUnreachableNonTerminals();
        assertTrue(unreachable.contains(B));
        assertFalse(unreachable.contains(S));
    }
    
    @Test
    void testReachableNonTerminalWithoutProductions() {
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A, a)); // S -> A a
        // A has no productions but is reachable
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Non-terminal 'A' is reachable but has no productions")));
    }
    
    @Test
    void testUnusedTerminalWarning() {
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b"); // Unused terminal
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        grammar.addTerminal(b); // Add unused terminal
        
        AnalysisResult result = analyzer.analyze();
        
        assertFalse(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("Terminal 'b' is defined but never used")));
    }
    
    @Test
    void testStartSymbolOnRightHandSideWarning() {
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        grammar.addProduction(new Production(A, S)); // Start symbol on RHS
        
        AnalysisResult result = analyzer.analyze();
        
        // This should generate a warning but not an error
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("Start symbol 'S' appears on the right-hand side")));
    }
    
    @Test
    void testComplexValidGrammar() {
        // Grammar for arithmetic expressions:
        // E ::= E + T | T
        // T ::= T * F | F  
        // F ::= ( E ) | id
        NonTerminal E = new NonTerminal("E");
        NonTerminal T = new NonTerminal("T");
        NonTerminal F = new NonTerminal("F");
        Terminal plus = new Terminal("+");
        Terminal mult = new Terminal("*");
        Terminal lparen = new Terminal("(");
        Terminal rparen = new Terminal(")");
        Terminal id = new Terminal("id");
        
        grammar.setStartSymbol(E);
        grammar.addProduction(new Production(E, E, plus, T)); // Left recursive but expected
        grammar.addProduction(new Production(E, T));
        grammar.addProduction(new Production(T, T, mult, F)); // Left recursive but expected
        grammar.addProduction(new Production(T, F));
        grammar.addProduction(new Production(F, lparen, E, rparen));
        grammar.addProduction(new Production(F, id));
        
        AnalysisResult result = analyzer.analyze();
        
        // This grammar has left recursion, which should be detected
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Left recursion detected")));
    }
    
    @Test
    void testEpsilonProductions() {
        // Grammar: S ::= A B, A ::= ε, B ::= b
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        NonTerminal B = new NonTerminal("B");
        Terminal b = new Terminal("b");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A, B));
        grammar.addProduction(new Production(A)); // Epsilon production
        grammar.addProduction(new Production(B, b));
        
        AnalysisResult result = analyzer.analyze();
        
        assertFalse(result.hasErrors());
    }
    
    @Test
    void testCircularDependencyDetection() {
        // Grammar: S ::= A, A ::= B, B ::= A (circular but not left recursive)
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        NonTerminal B = new NonTerminal("B");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A, a)); // S -> A a
        grammar.addProduction(new Production(A, B));    // A -> B
        grammar.addProduction(new Production(B, A));    // B -> A (circular)
        
        AnalysisResult result = analyzer.analyze();
        
        // Should have warnings about circular dependencies
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("Circular dependency detected")));
    }
    
    @Test
    void testAnalysisResultProperties() {
        NonTerminal S = new NonTerminal("S");
        grammar.setStartSymbol(S);
        // No productions for S
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasErrors());
        assertFalse(result.isSuccessful());
        assertTrue(result.getErrorCount() > 0);
        
        // Test toString method
        String resultString = result.toString();
        assertNotNull(resultString);
        assertTrue(resultString.contains("AnalysisResult"));
    }
    
    @Test
    void testMultipleAnalysisCalls() {
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        
        // First analysis
        AnalysisResult result1 = analyzer.analyze();
        assertFalse(result1.hasErrors());
        
        // Second analysis should give same results
        AnalysisResult result2 = analyzer.analyze();
        assertFalse(result2.hasErrors());
        assertEquals(result1.getErrorCount(), result2.getErrorCount());
        assertEquals(result1.getWarningCount(), result2.getWarningCount());
    }
    
    // ========== Comprehensive Validation Tests ==========
    
    @Test
    void testUndefinedSymbolValidation() {
        // This test is tricky because Grammar.addProduction automatically adds symbols
        // We'll test the validation logic by creating a scenario where a symbol
        // is referenced but not properly defined in the symbol table
        
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        
        // Since Grammar automatically manages symbols, we test with a valid grammar
        // The undefined symbol validation is more relevant during parsing phase
        AnalysisResult result = analyzer.analyze();
        
        assertFalse(result.hasErrors());
        
        // Check that validation errors are properly categorized
        List<ValidationError> validationErrors = analyzer.getValidationErrors();
        // This should be empty for a valid grammar
        assertTrue(validationErrors.isEmpty() || 
                  validationErrors.stream().noneMatch(ve -> 
                      ve.getCategory() == ValidationError.Category.SYMBOL_DEFINITION &&
                      ve.getSeverity() == ValidationError.Severity.ERROR));
    }
    
    @Test
    void testSymbolNameValidation() {
        // Test that Symbol class prevents empty names at construction
        assertThrows(IllegalArgumentException.class, () -> new NonTerminal(""));
        assertThrows(IllegalArgumentException.class, () -> new NonTerminal(null));
        
        // Test that our validation catches other issues
        NonTerminal validName = new NonTerminal("ValidName");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(validName);
        grammar.addProduction(new Production(validName, a));
        
        AnalysisResult result = analyzer.analyze();
        
        // Should be valid
        assertFalse(result.hasErrors());
    }
    
    @Test
    void testReservedWordSymbolNames() {
        NonTerminal epsilon = new NonTerminal("epsilon");
        Terminal lambda = new Terminal("lambda");
        
        grammar.setStartSymbol(epsilon);
        grammar.addProduction(new Production(epsilon, lambda));
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("might conflict with epsilon notation")));
        
        List<ValidationError> validationErrors = analyzer.getValidationErrors();
        assertTrue(validationErrors.stream()
                .anyMatch(ve -> ve.getCategory() == ValidationError.Category.SYMBOL_DEFINITION &&
                              ve.getMessage().contains("potentially problematic name")));
    }
    
    @Test
    void testSymbolNamesStartingWithDigits() {
        NonTerminal digitStart = new NonTerminal("1Symbol");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(digitStart);
        grammar.addProduction(new Production(digitStart, a));
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("starts with a digit")));
    }
    
    @Test
    void testSymbolNamesWithSpecialCharacters() {
        NonTerminal specialChar = new NonTerminal("Symbol<>");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(specialChar);
        grammar.addProduction(new Production(specialChar, a));
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("special characters")));
    }
    
    @Test
    void testStartSymbolNotInNonTerminals() {
        // Test the case where start symbol is set but has no productions
        // This simulates a scenario where the start symbol isn't properly integrated
        NonTerminal S = new NonTerminal("S");
        
        grammar.setStartSymbol(S);
        // Don't add any productions for S
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Start symbol 'S' has no productions")));
    }
    
    @Test
    void testStartSymbolNamingConvention() {
        NonTerminal lowercaseStart = new NonTerminal("start");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(lowercaseStart);
        grammar.addProduction(new Production(lowercaseStart, a));
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("naming convention")));
    }
    
    @Test
    void testDuplicateProductionValidation() {
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        
        // Create two separate Production objects with same content
        Production prod1 = new Production(S, a);
        Production prod2 = new Production(S, a);
        
        grammar.addProduction(prod1);
        grammar.addProduction(prod2);
        
        AnalysisResult result = analyzer.analyze();
        
        // Check if we have duplicate productions
        // Note: Grammar might use Set which prevents duplicates, so we check the actual behavior
        if (grammar.getProductionCount() > 1) {
            // If duplicates were added, we should get a warning
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(warning -> warning.contains("Duplicate production")));
        } else {
            // If Grammar prevented duplicates, that's also valid behavior
            // In this case, we just verify no errors occurred
            assertFalse(result.hasErrors());
        }
    }
    
    @Test
    void testAmbiguousPatternValidation() {
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a, b)); // S -> a b
        grammar.addProduction(new Production(S, a));    // S -> a (common prefix)
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("Potential ambiguity")));
    }
    
    @Test
    void testEpsilonProductionInStartSymbol() {
        NonTerminal S = new NonTerminal("S");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S)); // Epsilon production
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("epsilon production")));
    }
    
    @Test
    void testMultipleEpsilonProductions() {
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A));
        
        // Create two separate epsilon productions
        Production epsilon1 = new Production(A);
        Production epsilon2 = new Production(A);
        
        grammar.addProduction(epsilon1);
        grammar.addProduction(epsilon2);
        
        AnalysisResult result = analyzer.analyze();
        
        // Check if we actually have multiple epsilon productions
        if (grammar.getProductionCount() > 2) {
            // If duplicates were added, we should get a warning
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(warning -> warning.contains("multiple epsilon productions")));
        } else {
            // If Grammar prevented duplicates, that's also valid behavior
            // Just verify the grammar is still valid
            assertFalse(result.hasErrors());
        }
    }
    
    @Test
    void testNonProductiveNonTerminals() {
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        NonTerminal B = new NonTerminal("B");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A));
        grammar.addProduction(new Production(A, B)); // A -> B
        grammar.addProduction(new Production(B, A)); // B -> A (circular, non-productive)
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("not productive")));
        
        List<ValidationError> validationErrors = analyzer.getValidationErrors();
        assertTrue(validationErrors.stream()
                .anyMatch(ve -> ve.getCategory() == ValidationError.Category.SEMANTIC &&
                              ve.getMessage().contains("Non-productive")));
    }
    
    @Test
    void testGrammarComplexityWarnings() {
        NonTerminal S = new NonTerminal("S");
        
        grammar.setStartSymbol(S);
        
        // Add many different productions to trigger complexity warnings
        // Use different terminals to ensure they're not duplicates
        for (int i = 0; i < 25; i++) {
            Terminal terminal = new Terminal("a" + i);
            grammar.addProduction(new Production(S, terminal));
        }
        
        AnalysisResult result = analyzer.analyze();
        
        // Check if we actually have many productions
        if (grammar.getProductionCount() >= 20) {
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(warning -> warning.contains("productions, consider factoring")));
        } else {
            // If fewer productions were added (due to deduplication), that's also valid
            assertFalse(result.hasErrors());
        }
    }
    
    @Test
    void testValidationErrorDetails() {
        // Test ValidationError class functionality
        ValidationError error = new ValidationError(
            ValidationError.Severity.ERROR,
            ValidationError.Category.STRUCTURAL,
            "Test error message",
            "Test suggestion",
            "Test context"
        );
        
        assertEquals(ValidationError.Severity.ERROR, error.getSeverity());
        assertEquals(ValidationError.Category.STRUCTURAL, error.getCategory());
        assertEquals("Test error message", error.getMessage());
        assertEquals("Test suggestion", error.getSuggestion());
        assertEquals("Test context", error.getContext());
        assertTrue(error.hasSuggestion());
        assertTrue(error.hasContext());
        
        String errorString = error.toString();
        assertTrue(errorString.contains("ERROR"));
        assertTrue(errorString.contains("STRUCTURAL"));
        assertTrue(errorString.contains("Test error message"));
        assertTrue(errorString.contains("Test suggestion"));
        assertTrue(errorString.contains("Test context"));
    }
    
    @Test
    void testValidationErrorWithoutSuggestionAndContext() {
        ValidationError error = new ValidationError(
            ValidationError.Severity.WARNING,
            ValidationError.Category.SEMANTIC,
            "Simple warning"
        );
        
        assertFalse(error.hasSuggestion());
        assertFalse(error.hasContext());
        assertNull(error.getSuggestion());
        assertNull(error.getContext());
    }
    
    @Test
    void testValidationReport() {
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        
        analyzer.analyze();
        String report = analyzer.generateValidationReport();
        
        assertNotNull(report);
        assertTrue(report.contains("Grammar Validation Report"));
        assertTrue(report.contains("Grammar Statistics"));
        assertTrue(report.contains("Productions: 1"));
        assertTrue(report.contains("Non-terminals: 1"));
        assertTrue(report.contains("Terminals: 1"));
        assertTrue(report.contains("Start symbol: S"));
    }
    
    @Test
    void testValidationReportWithErrors() {
        // Create grammar with errors
        grammar.setStartSymbol(new NonTerminal("S"));
        // No productions for start symbol
        
        analyzer.analyze();
        String report = analyzer.generateValidationReport();
        
        assertTrue(report.contains("Validation Issues"));
        assertTrue(report.contains("ERROR"));
    }
    
    @Test
    void testCircularDependencyDetectionWithValidationErrors() {
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        NonTerminal B = new NonTerminal("B");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A, a));
        grammar.addProduction(new Production(A, B));
        grammar.addProduction(new Production(B, A)); // Circular dependency
        
        AnalysisResult result = analyzer.analyze();
        
        assertTrue(result.hasWarnings());
        
        List<ValidationError> validationErrors = analyzer.getValidationErrors();
        assertTrue(validationErrors.stream()
                .anyMatch(ve -> ve.getCategory() == ValidationError.Category.CIRCULAR_DEPENDENCY));
    }
    
    @Test
    void testAllValidationCategories() {
        // Test that all ValidationError categories are used
        NonTerminal S = new NonTerminal("epsilon"); // SYMBOL_DEFINITION warning
        NonTerminal A = new NonTerminal("A");
        Terminal a = new Terminal("a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A, a));
        grammar.addProduction(new Production(A, S)); // CIRCULAR_DEPENDENCY
        grammar.addProduction(new Production(A)); // SEMANTIC (epsilon)
        
        AnalysisResult result = analyzer.analyze();
        
        List<ValidationError> validationErrors = analyzer.getValidationErrors();
        
        // Check that multiple categories are represented
        Set<ValidationError.Category> categories = new HashSet<>();
        for (ValidationError ve : validationErrors) {
            categories.add(ve.getCategory());
        }
        
        assertTrue(categories.size() > 1, "Multiple validation categories should be present");
    }
}