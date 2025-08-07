package com.parsergenerator.grammar;

import com.parsergenerator.analysis.FirstFollowCalculator;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FIRST/FOLLOW computation in the Grammar class.
 * These tests verify that the Grammar class correctly integrates with
 * FirstFollowCalculator and provides proper access to analysis results.
 */
class GrammarFirstFollowIntegrationTest {

    private Grammar grammar;
    
    // Common terminals
    private Terminal plus, times, lparen, rparen, id;
    
    // Common non-terminals
    private NonTerminal expr, term, factor;

    @BeforeEach
    void setUp() {
        Production.resetIdCounter();
        grammar = new Grammar();
        
        // Initialize common terminals
        plus = new Terminal("+");
        times = new Terminal("*");
        lparen = new Terminal("(");
        rparen = new Terminal(")");
        id = new Terminal("id");
        
        // Initialize common non-terminals
        expr = new NonTerminal("expr");
        term = new NonTerminal("term");
        factor = new NonTerminal("factor");
    }

    @Test
    void testAnalysisNotComputedInitially() {
        assertFalse(grammar.isAnalysisComputed());
    }

    @Test
    void testAutomaticAnalysisComputation() {
        // Set up simple grammar: E ::= id
        grammar.addProduction(new Production(expr, id));
        grammar.setStartSymbol(expr);
        
        // Analysis should not be computed yet
        assertFalse(grammar.isAnalysisComputed());
        
        // Accessing FIRST set should trigger computation
        Set<Terminal> exprFirst = grammar.getFirstSet(expr);
        
        // Analysis should now be computed
        assertTrue(grammar.isAnalysisComputed());
        assertTrue(exprFirst.contains(id));
        assertEquals(1, exprFirst.size());
    }

    @Test
    void testManualAnalysisComputation() {
        // Set up simple grammar: E ::= id
        grammar.addProduction(new Production(expr, id));
        grammar.setStartSymbol(expr);
        
        // Manually compute analysis
        grammar.computeFirstFollowSets();
        
        assertTrue(grammar.isAnalysisComputed());
        
        Set<Terminal> exprFirst = grammar.getFirstSet(expr);
        assertTrue(exprFirst.contains(id));
        assertEquals(1, exprFirst.size());
    }

    @Test
    void testAnalysisInvalidationOnProductionAdd() {
        // Set up initial grammar
        grammar.addProduction(new Production(expr, id));
        grammar.setStartSymbol(expr);
        
        // Compute analysis
        grammar.computeFirstFollowSets();
        assertTrue(grammar.isAnalysisComputed());
        
        // Add another production - should invalidate analysis
        grammar.addProduction(new Production(expr, lparen, expr, rparen));
        assertFalse(grammar.isAnalysisComputed());
        
        // Accessing sets should recompute analysis
        Set<Terminal> exprFirst = grammar.getFirstSet(expr);
        assertTrue(grammar.isAnalysisComputed());
        assertTrue(exprFirst.contains(id));
        assertTrue(exprFirst.contains(lparen));
        assertEquals(2, exprFirst.size());
    }

    @Test
    void testAnalysisInvalidationOnStartSymbolChange() {
        // Set up grammar
        grammar.addProduction(new Production(expr, id));
        grammar.addProduction(new Production(term, times));
        grammar.setStartSymbol(expr);
        
        // Compute analysis
        grammar.computeFirstFollowSets();
        assertTrue(grammar.isAnalysisComputed());
        
        // Change start symbol - should invalidate analysis
        grammar.setStartSymbol(term);
        assertFalse(grammar.isAnalysisComputed());
        
        // FOLLOW sets should be different now
        Set<Terminal> termFollow = grammar.getFollowSet(term);
        assertTrue(termFollow.contains(FirstFollowCalculator.END_OF_INPUT));
    }

    @Test
    void testAnalysisInvalidationOnClear() {
        // Set up grammar and compute analysis
        grammar.addProduction(new Production(expr, id));
        grammar.setStartSymbol(expr);
        grammar.computeFirstFollowSets();
        assertTrue(grammar.isAnalysisComputed());
        
        // Clear grammar - should invalidate analysis
        grammar.clear();
        assertFalse(grammar.isAnalysisComputed());
    }

    @Test
    void testArithmeticGrammarIntegration() {
        // Set up arithmetic grammar:
        // E ::= E + T | T
        // T ::= T * F | F
        // F ::= ( E ) | id
        
        grammar.addProduction(new Production(expr, expr, plus, term));
        grammar.addProduction(new Production(expr, term));
        grammar.addProduction(new Production(term, term, times, factor));
        grammar.addProduction(new Production(term, factor));
        grammar.addProduction(new Production(factor, lparen, expr, rparen));
        grammar.addProduction(new Production(factor, id));
        grammar.setStartSymbol(expr);
        
        // Test FIRST sets through Grammar interface
        Set<Terminal> exprFirst = grammar.getFirstSet(expr);
        assertTrue(exprFirst.contains(lparen));
        assertTrue(exprFirst.contains(id));
        assertEquals(2, exprFirst.size());
        
        Set<Terminal> termFirst = grammar.getFirstSet(term);
        assertTrue(termFirst.contains(lparen));
        assertTrue(termFirst.contains(id));
        assertEquals(2, termFirst.size());
        
        Set<Terminal> factorFirst = grammar.getFirstSet(factor);
        assertTrue(factorFirst.contains(lparen));
        assertTrue(factorFirst.contains(id));
        assertEquals(2, factorFirst.size());
        
        // Test FOLLOW sets through Grammar interface
        Set<Terminal> exprFollow = grammar.getFollowSet(expr);
        assertTrue(exprFollow.contains(FirstFollowCalculator.END_OF_INPUT));
        assertTrue(exprFollow.contains(plus));
        assertTrue(exprFollow.contains(rparen));
        assertEquals(3, exprFollow.size());
        
        Set<Terminal> termFollow = grammar.getFollowSet(term);
        assertTrue(termFollow.contains(FirstFollowCalculator.END_OF_INPUT));
        assertTrue(termFollow.contains(plus));
        assertTrue(termFollow.contains(times));
        assertTrue(termFollow.contains(rparen));
        assertEquals(4, termFollow.size());
        
        Set<Terminal> factorFollow = grammar.getFollowSet(factor);
        assertTrue(factorFollow.contains(FirstFollowCalculator.END_OF_INPUT));
        assertTrue(factorFollow.contains(plus));
        assertTrue(factorFollow.contains(times));
        assertTrue(factorFollow.contains(rparen));
        assertEquals(4, factorFollow.size());
        
        // Test nullable symbols
        assertFalse(grammar.isNullable(expr));
        assertFalse(grammar.isNullable(term));
        assertFalse(grammar.isNullable(factor));
    }

    @Test
    void testEpsilonProductionIntegration() {
        // Grammar: S ::= A B
        //          A ::= a | ε
        //          B ::= b | ε
        
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        NonTerminal B = new NonTerminal("B");
        
        grammar.addProduction(new Production(S, A, B));
        grammar.addProduction(new Production(A, a));
        grammar.addProduction(new Production(A)); // epsilon production
        grammar.addProduction(new Production(B, b));
        grammar.addProduction(new Production(B)); // epsilon production
        grammar.setStartSymbol(S);
        
        // Test nullable symbols through Grammar interface
        assertTrue(grammar.isNullable(A));
        assertTrue(grammar.isNullable(B));
        assertTrue(grammar.isNullable(S));
        
        Set<Symbol> nullableSymbols = grammar.getNullableSymbols();
        assertTrue(nullableSymbols.contains(A));
        assertTrue(nullableSymbols.contains(B));
        assertTrue(nullableSymbols.contains(S));
        assertEquals(3, nullableSymbols.size());
        
        // Test FIRST sets
        Set<Terminal> sFirst = grammar.getFirstSet(S);
        assertTrue(sFirst.contains(a)); // from A
        assertTrue(sFirst.contains(b)); // from B (when A is epsilon)
        assertEquals(2, sFirst.size());
        
        // Test FOLLOW sets
        Set<Terminal> aFollow = grammar.getFollowSet(A);
        assertTrue(aFollow.contains(b)); // from FIRST(B)
        assertTrue(aFollow.contains(FirstFollowCalculator.END_OF_INPUT)); // B is nullable, so FOLLOW(S)
        assertEquals(2, aFollow.size());
    }

    @Test
    void testFirstSetOfSequenceIntegration() {
        // Grammar: S ::= A B C
        //          A ::= a | ε
        //          B ::= b | ε
        //          C ::= c
        
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        Terminal c = new Terminal("c");
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        NonTerminal B = new NonTerminal("B");
        NonTerminal C = new NonTerminal("C");
        
        grammar.addProduction(new Production(S, A, B, C));
        grammar.addProduction(new Production(A, a));
        grammar.addProduction(new Production(A)); // epsilon
        grammar.addProduction(new Production(B, b));
        grammar.addProduction(new Production(B)); // epsilon
        grammar.addProduction(new Production(C, c));
        grammar.setStartSymbol(S);
        
        // Test FIRST of sequence A B C through Grammar interface
        Set<Terminal> abcFirst = grammar.getFirstSetOfSequence(Arrays.asList(A, B, C));
        assertTrue(abcFirst.contains(a)); // from A
        assertTrue(abcFirst.contains(b)); // from B when A is epsilon
        assertTrue(abcFirst.contains(c)); // from C when A and B are epsilon
        assertEquals(3, abcFirst.size());
        
        // Test FIRST of sequence B C
        Set<Terminal> bcFirst = grammar.getFirstSetOfSequence(Arrays.asList(B, C));
        assertTrue(bcFirst.contains(b)); // from B
        assertTrue(bcFirst.contains(c)); // from C when B is epsilon
        assertEquals(2, bcFirst.size());
    }

    @Test
    void testGetFirstFollowCalculatorAccess() {
        // Set up simple grammar
        grammar.addProduction(new Production(expr, id));
        grammar.setStartSymbol(expr);
        
        // Get calculator instance - should trigger computation
        FirstFollowCalculator calculator = grammar.getFirstFollowCalculator();
        assertNotNull(calculator);
        assertTrue(grammar.isAnalysisComputed());
        
        // Should return same instance on subsequent calls
        FirstFollowCalculator calculator2 = grammar.getFirstFollowCalculator();
        assertSame(calculator, calculator2);
    }

    @Test
    void testAnalysisCaching() {
        // Set up grammar
        grammar.addProduction(new Production(expr, id));
        grammar.setStartSymbol(expr);
        
        // First access should compute analysis
        Set<Terminal> first1 = grammar.getFirstSet(expr);
        assertTrue(grammar.isAnalysisComputed());
        
        // Second access should use cached results (same content, but new unmodifiable wrapper)
        Set<Terminal> first2 = grammar.getFirstSet(expr);
        assertEquals(first1, first2); // Should have same content
        assertTrue(first2.contains(id));
        
        // FOLLOW access should also use cached results
        Set<Terminal> follow1 = grammar.getFollowSet(expr);
        Set<Terminal> follow2 = grammar.getFollowSet(expr);
        assertEquals(follow1, follow2); // Should have same content
        
        // Verify the calculator instance is cached
        FirstFollowCalculator calc1 = grammar.getFirstFollowCalculator();
        FirstFollowCalculator calc2 = grammar.getFirstFollowCalculator();
        assertSame(calc1, calc2); // Calculator instance should be cached
    }

    @Test
    void testInvalidateAnalysisMethod() {
        // Set up grammar and compute analysis
        grammar.addProduction(new Production(expr, id));
        grammar.setStartSymbol(expr);
        grammar.computeFirstFollowSets();
        assertTrue(grammar.isAnalysisComputed());
        
        // Manually invalidate analysis
        grammar.invalidateAnalysis();
        assertFalse(grammar.isAnalysisComputed());
        
        // Next access should recompute
        Set<Terminal> exprFirst = grammar.getFirstSet(expr);
        assertTrue(grammar.isAnalysisComputed());
        assertTrue(exprFirst.contains(id));
    }

    @Test
    void testNullSymbolHandling() {
        // Set up grammar
        grammar.addProduction(new Production(expr, id));
        grammar.setStartSymbol(expr);
        
        // Test null symbol handling
        Set<Terminal> nullFirst = grammar.getFirstSet(null);
        assertTrue(nullFirst.isEmpty());
        
        Set<Terminal> nullFollow = grammar.getFollowSet(null);
        assertTrue(nullFollow.isEmpty());
        
        assertFalse(grammar.isNullable(null));
    }
}