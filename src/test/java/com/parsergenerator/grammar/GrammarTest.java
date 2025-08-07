package com.parsergenerator.grammar;

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
 * Unit tests for the Grammar class.
 */
class GrammarTest {

    private Grammar grammar;
    private NonTerminal expr;
    private NonTerminal term;
    private NonTerminal factor;
    private Terminal plus;
    private Terminal mult;
    private Terminal number;
    private Terminal leftParen;
    private Terminal rightParen;

    @BeforeEach
    void setUp() {
        Production.resetIdCounter();
        
        grammar = new Grammar();
        expr = new NonTerminal("expr");
        term = new NonTerminal("term");
        factor = new NonTerminal("factor");
        plus = new Terminal("PLUS", "+");
        mult = new Terminal("MULT", "*");
        number = new Terminal("NUMBER", "[0-9]+");
        leftParen = new Terminal("LPAREN", "(");
        rightParen = new Terminal("RPAREN", ")");
    }

    @Test
    void testDefaultConstructor() {
        assertTrue(grammar.isEmpty());
        assertFalse(grammar.hasStartSymbol());
        assertNull(grammar.getStartSymbol());
        assertEquals(0, grammar.getProductionCount());
        assertEquals(0, grammar.getTerminalCount());
        assertEquals(0, grammar.getNonTerminalCount());
    }

    @Test
    void testConstructorWithStartSymbol() {
        Grammar grammarWithStart = new Grammar(expr);
        
        assertEquals(expr, grammarWithStart.getStartSymbol());
        assertTrue(grammarWithStart.hasStartSymbol());
        assertEquals(1, grammarWithStart.getNonTerminalCount());
        assertTrue(grammarWithStart.getNonTerminals().contains(expr));
    }

    @Test
    void testConstructorWithNullStartSymbol() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Grammar(null);
        });
    }

    @Test
    void testSetStartSymbol() {
        grammar.setStartSymbol(expr);
        
        assertEquals(expr, grammar.getStartSymbol());
        assertTrue(grammar.hasStartSymbol());
        assertTrue(grammar.getNonTerminals().contains(expr));
    }

    @Test
    void testSetNullStartSymbol() {
        assertThrows(IllegalArgumentException.class, () -> {
            grammar.setStartSymbol(null);
        });
    }

    @Test
    void testAddProduction() {
        grammar.setStartSymbol(expr);
        Production production = new Production(expr, term, plus, expr);
        
        grammar.addProduction(production);
        
        assertEquals(1, grammar.getProductionCount());
        assertTrue(grammar.getProductions().contains(production));
        assertTrue(grammar.getNonTerminals().contains(expr));
        assertTrue(grammar.getNonTerminals().contains(term));
        assertTrue(grammar.getTerminals().contains(plus));
    }

    @Test
    void testAddNullProduction() {
        assertThrows(IllegalArgumentException.class, () -> {
            grammar.addProduction(null);
        });
    }

    @Test
    void testAddTerminal() {
        grammar.addTerminal(plus);
        
        assertEquals(1, grammar.getTerminalCount());
        assertTrue(grammar.getTerminals().contains(plus));
        assertEquals(plus, grammar.getSymbol("PLUS"));
        assertEquals(plus, grammar.getTerminal("PLUS"));
    }

    @Test
    void testAddNullTerminal() {
        assertThrows(IllegalArgumentException.class, () -> {
            grammar.addTerminal(null);
        });
    }

    @Test
    void testAddNonTerminal() {
        grammar.addNonTerminal(expr);
        
        assertEquals(1, grammar.getNonTerminalCount());
        assertTrue(grammar.getNonTerminals().contains(expr));
        assertEquals(expr, grammar.getSymbol("expr"));
        assertEquals(expr, grammar.getNonTerminal("expr"));
    }

    @Test
    void testAddNullNonTerminal() {
        assertThrows(IllegalArgumentException.class, () -> {
            grammar.addNonTerminal(null);
        });
    }

    @Test
    void testAddSymbol() {
        grammar.addSymbol(plus);
        grammar.addSymbol(expr);
        
        assertEquals(1, grammar.getTerminalCount());
        assertEquals(1, grammar.getNonTerminalCount());
        assertTrue(grammar.getTerminals().contains(plus));
        assertTrue(grammar.getNonTerminals().contains(expr));
    }

    @Test
    void testAddNullSymbol() {
        assertThrows(IllegalArgumentException.class, () -> {
            grammar.addSymbol(null);
        });
    }

    @Test
    void testSymbolLookup() {
        grammar.addTerminal(plus);
        grammar.addNonTerminal(expr);
        
        assertEquals(plus, grammar.getSymbol("PLUS"));
        assertEquals(expr, grammar.getSymbol("expr"));
        assertNull(grammar.getSymbol("nonexistent"));
        assertNull(grammar.getSymbol(null));
        
        assertTrue(grammar.hasSymbol("PLUS"));
        assertTrue(grammar.hasSymbol("expr"));
        assertFalse(grammar.hasSymbol("nonexistent"));
        assertFalse(grammar.hasSymbol(null));
    }

    @Test
    void testGetTerminalAndNonTerminal() {
        grammar.addTerminal(plus);
        grammar.addNonTerminal(expr);
        
        assertEquals(plus, grammar.getTerminal("PLUS"));
        assertNull(grammar.getTerminal("expr")); // expr is not a terminal
        
        assertEquals(expr, grammar.getNonTerminal("expr"));
        assertNull(grammar.getNonTerminal("PLUS")); // PLUS is not a non-terminal
    }

    @Test
    void testGetAllSymbols() {
        grammar.addTerminal(plus);
        grammar.addTerminal(mult);
        grammar.addNonTerminal(expr);
        grammar.addNonTerminal(term);
        
        Set<Symbol> allSymbols = grammar.getAllSymbols();
        assertEquals(4, allSymbols.size());
        assertTrue(allSymbols.contains(plus));
        assertTrue(allSymbols.contains(mult));
        assertTrue(allSymbols.contains(expr));
        assertTrue(allSymbols.contains(term));
    }

    @Test
    void testGetProductionsFor() {
        grammar.setStartSymbol(expr);
        Production prod1 = new Production(expr, term, plus, expr);
        Production prod2 = new Production(expr, term);
        Production prod3 = new Production(term, number);
        
        grammar.addProduction(prod1);
        grammar.addProduction(prod2);
        grammar.addProduction(prod3);
        
        Set<Production> exprProductions = grammar.getProductionsFor(expr);
        assertEquals(2, exprProductions.size());
        assertTrue(exprProductions.contains(prod1));
        assertTrue(exprProductions.contains(prod2));
        
        Set<Production> termProductions = grammar.getProductionsFor(term);
        assertEquals(1, termProductions.size());
        assertTrue(termProductions.contains(prod3));
        
        Set<Production> factorProductions = grammar.getProductionsFor(factor);
        assertTrue(factorProductions.isEmpty());
    }

    @Test
    void testGetProductionsForByName() {
        grammar.setStartSymbol(expr);
        Production prod1 = new Production(expr, term, plus, expr);
        Production prod2 = new Production(term, number);
        
        grammar.addProduction(prod1);
        grammar.addProduction(prod2);
        
        Set<Production> exprProductions = grammar.getProductionsFor("expr");
        assertEquals(1, exprProductions.size());
        assertTrue(exprProductions.contains(prod1));
        
        Set<Production> nonexistentProductions = grammar.getProductionsFor("nonexistent");
        assertTrue(nonexistentProductions.isEmpty());
    }

    @Test
    void testGetProductionsForNull() {
        Set<Production> productions = grammar.getProductionsFor((NonTerminal) null);
        assertTrue(productions.isEmpty());
    }

    @Test
    void testValidateEmptyGrammar() {
        List<String> errors = grammar.validateGrammar();
        
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> error.contains("start symbol")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("at least one production")));
    }

    @Test
    void testValidateGrammarWithoutStartSymbol() {
        Production production = new Production(expr, term);
        grammar.addProduction(production);
        
        List<String> errors = grammar.validateGrammar();
        
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> error.contains("start symbol")));
    }

    @Test
    void testValidateGrammarWithStartSymbolWithoutProductions() {
        grammar.setStartSymbol(expr);
        Production production = new Production(term, number);
        grammar.addProduction(production);
        
        List<String> errors = grammar.validateGrammar();
        
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> error.contains("Start symbol") && error.contains("no productions")));
    }

    @Test
    void testValidateGrammarWithReachableNonTerminalWithoutProductions() {
        grammar.setStartSymbol(expr);
        // Since addProduction automatically adds all symbols, we test
        // the case where a non-terminal is reachable but has no productions
        
        NonTerminal undefined = new NonTerminal("undefined");
        Production production = new Production(expr, undefined);
        grammar.addProduction(production);
        
        List<String> errors = grammar.validateGrammar();
        
        // The grammar should have validation errors because 'undefined' 
        // is reachable from the start symbol but has no productions
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> 
            error.contains("reachable but has no productions")));
    }

    @Test
    void testValidateGrammarWithUnreachableNonTerminals() {
        grammar.setStartSymbol(expr);
        Production prod1 = new Production(expr, number);
        Production prod2 = new Production(term, mult); // term is unreachable
        
        grammar.addProduction(prod1);
        grammar.addProduction(prod2);
        
        List<String> errors = grammar.validateGrammar();
        
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> error.contains("unreachable")));
    }

    @Test
    void testValidGrammar() {
        grammar.setStartSymbol(expr);
        Production prod1 = new Production(expr, expr, plus, term);
        Production prod2 = new Production(expr, term);
        Production prod3 = new Production(term, term, mult, factor);
        Production prod4 = new Production(term, factor);
        Production prod5 = new Production(factor, leftParen, expr, rightParen);
        Production prod6 = new Production(factor, number);
        
        grammar.addProduction(prod1);
        grammar.addProduction(prod2);
        grammar.addProduction(prod3);
        grammar.addProduction(prod4);
        grammar.addProduction(prod5);
        grammar.addProduction(prod6);
        
        List<String> errors = grammar.validateGrammar();
        assertTrue(errors.isEmpty());
        assertTrue(grammar.isValid());
    }

    @Test
    void testClear() {
        grammar.setStartSymbol(expr);
        Production production = new Production(expr, term, plus, expr);
        grammar.addProduction(production);
        
        assertFalse(grammar.isEmpty());
        assertTrue(grammar.hasStartSymbol());
        
        grammar.clear();
        
        assertTrue(grammar.isEmpty());
        assertFalse(grammar.hasStartSymbol());
        assertNull(grammar.getStartSymbol());
        assertEquals(0, grammar.getProductionCount());
        assertEquals(0, grammar.getTerminalCount());
        assertEquals(0, grammar.getNonTerminalCount());
    }

    @Test
    void testUnmodifiableCollections() {
        grammar.setStartSymbol(expr);
        Production production = new Production(expr, term);
        grammar.addProduction(production);
        
        Set<Production> productions = grammar.getProductions();
        Set<Terminal> terminals = grammar.getTerminals();
        Set<NonTerminal> nonTerminals = grammar.getNonTerminals();
        Set<Symbol> allSymbols = grammar.getAllSymbols();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            productions.add(new Production(term, number));
        });
        
        assertThrows(UnsupportedOperationException.class, () -> {
            terminals.add(plus);
        });
        
        assertThrows(UnsupportedOperationException.class, () -> {
            nonTerminals.add(factor);
        });
        
        assertThrows(UnsupportedOperationException.class, () -> {
            allSymbols.add(mult);
        });
    }

    @Test
    void testToString() {
        grammar.setStartSymbol(expr);
        Production production = new Production(expr, term, plus, expr);
        grammar.addProduction(production);
        
        String result = grammar.toString();
        
        assertTrue(result.contains("Grammar"));
        assertTrue(result.contains("Start: expr"));
        assertTrue(result.contains("Productions:"));
        assertTrue(result.contains("expr ::= term PLUS expr"));
        assertTrue(result.contains("Terminals: 1"));
        assertTrue(result.contains("Non-terminals: 2"));
    }

    @Test
    void testToDetailedString() {
        grammar.setStartSymbol(expr);
        Production production = new Production(expr, term, plus, expr);
        grammar.addProduction(production);
        
        String result = grammar.toDetailedString();
        
        assertTrue(result.contains("Grammar"));
        assertTrue(result.contains("Start Symbol: expr"));
        assertTrue(result.contains("Productions (1):"));
        assertTrue(result.contains("[0] expr ::= term PLUS expr"));
        assertTrue(result.contains("Terminals (1):"));
        assertTrue(result.contains("Non-terminals (2):"));
    }

    @Test
    void testComplexArithmeticGrammar() {
        // Build a complete arithmetic grammar
        grammar.setStartSymbol(expr);
        
        // expr ::= expr + term | term
        Production prod1 = new Production(expr, expr, plus, term);
        Production prod2 = new Production(expr, term);
        
        // term ::= term * factor | factor
        Production prod3 = new Production(term, term, mult, factor);
        Production prod4 = new Production(term, factor);
        
        // factor ::= ( expr ) | NUMBER
        Production prod5 = new Production(factor, leftParen, expr, rightParen);
        Production prod6 = new Production(factor, number);
        
        grammar.addProduction(prod1);
        grammar.addProduction(prod2);
        grammar.addProduction(prod3);
        grammar.addProduction(prod4);
        grammar.addProduction(prod5);
        grammar.addProduction(prod6);
        
        // Verify grammar structure
        assertEquals(6, grammar.getProductionCount());
        assertEquals(5, grammar.getTerminalCount()); // +, *, (, ), NUMBER
        assertEquals(3, grammar.getNonTerminalCount()); // expr, term, factor
        assertTrue(grammar.isValid());
        
        // Verify productions for each non-terminal
        assertEquals(2, grammar.getProductionsFor(expr).size());
        assertEquals(2, grammar.getProductionsFor(term).size());
        assertEquals(2, grammar.getProductionsFor(factor).size());
    }
}