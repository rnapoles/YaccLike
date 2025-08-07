package com.parsergenerator.analysis;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for FirstFollowCalculator.
 * Tests include various grammar scenarios including epsilon productions,
 * left recursion, and complex FIRST/FOLLOW relationships.
 */
class FirstFollowCalculatorTest {

    private Grammar grammar;
    private FirstFollowCalculator calculator;
    
    // Common terminals
    private Terminal plus, times, lparen, rparen, id, num;
    
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
        num = new Terminal("num");
        
        // Initialize common non-terminals
        expr = new NonTerminal("expr");
        term = new NonTerminal("term");
        factor = new NonTerminal("factor");
    }

    @Test
    void testConstructorWithNullGrammar() {
        assertThrows(IllegalArgumentException.class, () -> new FirstFollowCalculator(null));
    }

    @Test
    void testSimpleGrammarWithoutEpsilon() {
        // Grammar: E ::= T | E + T
        //          T ::= F | T * F  
        //          F ::= id | ( E )
        
        grammar.addTerminal(plus);
        grammar.addTerminal(times);
        grammar.addTerminal(lparen);
        grammar.addTerminal(rparen);
        grammar.addTerminal(id);
        
        grammar.addProduction(new Production(expr, term));
        grammar.addProduction(new Production(expr, expr, plus, term));
        grammar.addProduction(new Production(term, factor));
        grammar.addProduction(new Production(term, term, times, factor));
        grammar.addProduction(new Production(factor, id));
        grammar.addProduction(new Production(factor, lparen, expr, rparen));
        
        grammar.setStartSymbol(expr);
        
        calculator = new FirstFollowCalculator(grammar);
        
        // Test FIRST sets
        Set<Terminal> exprFirst = calculator.getFirstSet(expr);
        assertTrue(exprFirst.contains(id));
        assertTrue(exprFirst.contains(lparen));
        assertEquals(2, exprFirst.size());
        
        Set<Terminal> termFirst = calculator.getFirstSet(term);
        assertTrue(termFirst.contains(id));
        assertTrue(termFirst.contains(lparen));
        assertEquals(2, termFirst.size());
        
        Set<Terminal> factorFirst = calculator.getFirstSet(factor);
        assertTrue(factorFirst.contains(id));
        assertTrue(factorFirst.contains(lparen));
        assertEquals(2, factorFirst.size());
        
        // Test FOLLOW sets
        Set<Terminal> exprFollow = calculator.getFollowSet(expr);
        assertTrue(exprFollow.contains(FirstFollowCalculator.END_OF_INPUT));
        assertTrue(exprFollow.contains(rparen));
        assertTrue(exprFollow.contains(plus));
        assertEquals(3, exprFollow.size());
        
        Set<Terminal> termFollow = calculator.getFollowSet(term);
        assertTrue(termFollow.contains(FirstFollowCalculator.END_OF_INPUT));
        assertTrue(termFollow.contains(rparen));
        assertTrue(termFollow.contains(plus));
        assertTrue(termFollow.contains(times));
        assertEquals(4, termFollow.size());
        
        Set<Terminal> factorFollow = calculator.getFollowSet(factor);
        assertTrue(factorFollow.contains(FirstFollowCalculator.END_OF_INPUT));
        assertTrue(factorFollow.contains(rparen));
        assertTrue(factorFollow.contains(plus));
        assertTrue(factorFollow.contains(times));
        assertEquals(4, factorFollow.size());
        
        // Test nullable symbols
        assertFalse(calculator.isNullable(expr));
        assertFalse(calculator.isNullable(term));
        assertFalse(calculator.isNullable(factor));
    }

    @Test
    void testGrammarWithEpsilonProductions() {
        // Grammar: S ::= A B
        //          A ::= a | ε
        //          B ::= b | ε
        
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        NonTerminal B = new NonTerminal("B");
        
        grammar.addTerminal(a);
        grammar.addTerminal(b);
        
        grammar.addProduction(new Production(S, A, B));
        grammar.addProduction(new Production(A, a));
        grammar.addProduction(new Production(A)); // epsilon production
        grammar.addProduction(new Production(B, b));
        grammar.addProduction(new Production(B)); // epsilon production
        
        grammar.setStartSymbol(S);
        
        calculator = new FirstFollowCalculator(grammar);
        
        // Test nullable symbols
        assertTrue(calculator.isNullable(A));
        assertTrue(calculator.isNullable(B));
        assertTrue(calculator.isNullable(S)); // S is nullable because both A and B are nullable
        
        // Test FIRST sets
        Set<Terminal> sFirst = calculator.getFirstSet(S);
        assertTrue(sFirst.contains(a)); // from A
        assertTrue(sFirst.contains(b)); // from B (when A is epsilon)
        assertEquals(2, sFirst.size());
        
        Set<Terminal> aFirst = calculator.getFirstSet(A);
        assertTrue(aFirst.contains(a));
        assertEquals(1, aFirst.size());
        
        Set<Terminal> bFirst = calculator.getFirstSet(B);
        assertTrue(bFirst.contains(b));
        assertEquals(1, bFirst.size());
        
        // Test FOLLOW sets
        Set<Terminal> aFollow = calculator.getFollowSet(A);
        assertTrue(aFollow.contains(b)); // from FIRST(B)
        assertTrue(aFollow.contains(FirstFollowCalculator.END_OF_INPUT)); // B is nullable, so FOLLOW(S)
        assertEquals(2, aFollow.size());
        
        Set<Terminal> bFollow = calculator.getFollowSet(B);
        assertTrue(bFollow.contains(FirstFollowCalculator.END_OF_INPUT)); // FOLLOW(S)
        assertEquals(1, bFollow.size());
    }

    @Test
    void testComplexGrammarWithMultipleEpsilons() {
        // Grammar: S ::= A B C
        //          A ::= a A | ε
        //          B ::= b B | ε  
        //          C ::= c
        
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        Terminal c = new Terminal("c");
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        NonTerminal B = new NonTerminal("B");
        NonTerminal C = new NonTerminal("C");
        
        grammar.addTerminal(a);
        grammar.addTerminal(b);
        grammar.addTerminal(c);
        
        grammar.addProduction(new Production(S, A, B, C));
        grammar.addProduction(new Production(A, a, A));
        grammar.addProduction(new Production(A)); // epsilon
        grammar.addProduction(new Production(B, b, B));
        grammar.addProduction(new Production(B)); // epsilon
        grammar.addProduction(new Production(C, c));
        
        grammar.setStartSymbol(S);
        
        calculator = new FirstFollowCalculator(grammar);
        
        // Test nullable symbols
        assertTrue(calculator.isNullable(A));
        assertTrue(calculator.isNullable(B));
        assertFalse(calculator.isNullable(C));
        assertFalse(calculator.isNullable(S)); // S is not nullable because C is not nullable
        
        // Test FIRST sets
        Set<Terminal> sFirst = calculator.getFirstSet(S);
        assertTrue(sFirst.contains(a)); // from A
        assertTrue(sFirst.contains(b)); // from B (when A is epsilon)
        assertTrue(sFirst.contains(c)); // from C (when A and B are epsilon)
        assertEquals(3, sFirst.size());
        
        // Test FOLLOW sets
        Set<Terminal> aFollow = calculator.getFollowSet(A);
        assertTrue(aFollow.contains(b)); // from FIRST(B)
        assertTrue(aFollow.contains(c)); // from FIRST(C) when B is epsilon
        assertEquals(2, aFollow.size());
        
        Set<Terminal> bFollow = calculator.getFollowSet(B);
        assertTrue(bFollow.contains(c)); // from FIRST(C)
        assertEquals(1, bFollow.size());
    }

    @Test
    void testArithmeticGrammarExample() {
        // Classic arithmetic grammar:
        // E ::= E + T | T
        // T ::= T * F | F
        // F ::= ( E ) | id
        
        grammar.addTerminal(plus);
        grammar.addTerminal(times);
        grammar.addTerminal(lparen);
        grammar.addTerminal(rparen);
        grammar.addTerminal(id);
        
        grammar.addProduction(new Production(expr, expr, plus, term));
        grammar.addProduction(new Production(expr, term));
        grammar.addProduction(new Production(term, term, times, factor));
        grammar.addProduction(new Production(term, factor));
        grammar.addProduction(new Production(factor, lparen, expr, rparen));
        grammar.addProduction(new Production(factor, id));
        
        grammar.setStartSymbol(expr);
        
        calculator = new FirstFollowCalculator(grammar);
        
        // Test FIRST sets
        Set<Terminal> exprFirst = calculator.getFirstSet(expr);
        assertTrue(exprFirst.contains(lparen));
        assertTrue(exprFirst.contains(id));
        assertEquals(2, exprFirst.size());
        
        Set<Terminal> termFirst = calculator.getFirstSet(term);
        assertTrue(termFirst.contains(lparen));
        assertTrue(termFirst.contains(id));
        assertEquals(2, termFirst.size());
        
        Set<Terminal> factorFirst = calculator.getFirstSet(factor);
        assertTrue(factorFirst.contains(lparen));
        assertTrue(factorFirst.contains(id));
        assertEquals(2, factorFirst.size());
        
        // Test FOLLOW sets
        Set<Terminal> exprFollow = calculator.getFollowSet(expr);
        assertTrue(exprFollow.contains(FirstFollowCalculator.END_OF_INPUT));
        assertTrue(exprFollow.contains(plus));
        assertTrue(exprFollow.contains(rparen));
        assertEquals(3, exprFollow.size());
        
        Set<Terminal> termFollow = calculator.getFollowSet(term);
        assertTrue(termFollow.contains(FirstFollowCalculator.END_OF_INPUT));
        assertTrue(termFollow.contains(plus));
        assertTrue(termFollow.contains(times));
        assertTrue(termFollow.contains(rparen));
        assertEquals(4, termFollow.size());
        
        Set<Terminal> factorFollow = calculator.getFollowSet(factor);
        assertTrue(factorFollow.contains(FirstFollowCalculator.END_OF_INPUT));
        assertTrue(factorFollow.contains(plus));
        assertTrue(factorFollow.contains(times));
        assertTrue(factorFollow.contains(rparen));
        assertEquals(4, factorFollow.size());
    }

    @Test
    void testFirstSetOfSequence() {
        // Grammar: S ::= A B C
        //          A ::= a | ε
        //          B ::= b | ε
        //          C ::= c
        
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        Terminal c = new Terminal("c");
        NonTerminal A = new NonTerminal("A");
        NonTerminal B = new NonTerminal("B");
        NonTerminal C = new NonTerminal("C");
        
        grammar.addTerminal(a);
        grammar.addTerminal(b);
        grammar.addTerminal(c);
        
        grammar.addProduction(new Production(A, a));
        grammar.addProduction(new Production(A)); // epsilon
        grammar.addProduction(new Production(B, b));
        grammar.addProduction(new Production(B)); // epsilon
        grammar.addProduction(new Production(C, c));
        
        calculator = new FirstFollowCalculator(grammar);
        
        // Test FIRST of sequence A B C
        Set<Terminal> abcFirst = calculator.getFirstSetOfSequence(Arrays.asList(A, B, C));
        assertTrue(abcFirst.contains(a)); // from A
        assertTrue(abcFirst.contains(b)); // from B when A is epsilon
        assertTrue(abcFirst.contains(c)); // from C when A and B are epsilon
        assertEquals(3, abcFirst.size());
        
        // Test FIRST of sequence B C
        Set<Terminal> bcFirst = calculator.getFirstSetOfSequence(Arrays.asList(B, C));
        assertTrue(bcFirst.contains(b)); // from B
        assertTrue(bcFirst.contains(c)); // from C when B is epsilon
        assertEquals(2, bcFirst.size());
        
        // Test FIRST of empty sequence
        Set<Terminal> emptyFirst = calculator.getFirstSetOfSequence(Arrays.asList());
        assertTrue(emptyFirst.isEmpty());
        
        // Test FIRST of null sequence
        Set<Terminal> nullFirst = calculator.getFirstSetOfSequence(null);
        assertTrue(nullFirst.isEmpty());
    }

    @Test
    void testGetFirstSetWithNullSymbol() {
        calculator = new FirstFollowCalculator(grammar);
        Set<Terminal> result = calculator.getFirstSet(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetFollowSetWithNullNonTerminal() {
        calculator = new FirstFollowCalculator(grammar);
        Set<Terminal> result = calculator.getFollowSet(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testTerminalFirstSets() {
        grammar.addTerminal(id);
        grammar.addTerminal(num);
        
        calculator = new FirstFollowCalculator(grammar);
        
        // Terminal symbols should have themselves in their FIRST set
        Set<Terminal> idFirst = calculator.getFirstSet(id);
        assertTrue(idFirst.contains(id));
        assertEquals(1, idFirst.size());
        
        Set<Terminal> numFirst = calculator.getFirstSet(num);
        assertTrue(numFirst.contains(num));
        assertEquals(1, numFirst.size());
    }

    @Test
    void testGrammarWithOnlyEpsilonProduction() {
        // Grammar: S ::= ε
        NonTerminal S = new NonTerminal("S");
        
        grammar.addProduction(new Production(S)); // epsilon production
        grammar.setStartSymbol(S);
        
        calculator = new FirstFollowCalculator(grammar);
        
        // S should be nullable
        assertTrue(calculator.isNullable(S));
        
        // FIRST(S) should be empty (no terminals can start strings from S)
        Set<Terminal> sFirst = calculator.getFirstSet(S);
        assertTrue(sFirst.isEmpty());
        
        // FOLLOW(S) should contain $
        Set<Terminal> sFollow = calculator.getFollowSet(S);
        assertTrue(sFollow.contains(FirstFollowCalculator.END_OF_INPUT));
        assertEquals(1, sFollow.size());
    }

    @Test
    void testNullableSymbolsSet() {
        // Grammar: S ::= A B
        //          A ::= a | ε
        //          B ::= b | ε
        
        Terminal a = new Terminal("a");
        Terminal b = new Terminal("b");
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        NonTerminal B = new NonTerminal("B");
        
        grammar.addTerminal(a);
        grammar.addTerminal(b);
        
        grammar.addProduction(new Production(S, A, B));
        grammar.addProduction(new Production(A, a));
        grammar.addProduction(new Production(A)); // epsilon
        grammar.addProduction(new Production(B, b));
        grammar.addProduction(new Production(B)); // epsilon
        
        grammar.setStartSymbol(S);
        
        calculator = new FirstFollowCalculator(grammar);
        
        Set<com.parsergenerator.symbol.Symbol> nullableSymbols = calculator.getNullableSymbols();
        assertTrue(nullableSymbols.contains(A));
        assertTrue(nullableSymbols.contains(B));
        assertTrue(nullableSymbols.contains(S));
        assertEquals(3, nullableSymbols.size());
    }

    @Test
    void testToStringOutput() {
        // Simple grammar for testing toString
        Terminal a = new Terminal("a");
        NonTerminal A = new NonTerminal("A");
        
        grammar.addTerminal(a);
        grammar.addProduction(new Production(A, a));
        grammar.setStartSymbol(A);
        
        calculator = new FirstFollowCalculator(grammar);
        
        String output = calculator.toString();
        assertNotNull(output);
        assertTrue(output.contains("FirstFollowCalculator Results:"));
        assertTrue(output.contains("FIRST sets:"));
        assertTrue(output.contains("FOLLOW sets:"));
        assertTrue(output.contains("Nullable symbols:"));
    }
}