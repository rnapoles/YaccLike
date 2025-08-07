package com.parsergenerator.grammar;

import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Production class.
 */
class ProductionTest {

    private NonTerminal expr;
    private NonTerminal term;
    private Terminal plus;
    private Terminal number;
    private Terminal leftParen;
    private Terminal rightParen;

    @BeforeEach
    void setUp() {
        Production.resetIdCounter();
        
        expr = new NonTerminal("expr");
        term = new NonTerminal("term");
        plus = new Terminal("PLUS", "+");
        number = new Terminal("NUMBER", "[0-9]+");
        leftParen = new Terminal("LPAREN", "(");
        rightParen = new Terminal("RPAREN", ")");
    }

    @Test
    void testConstructorWithList() {
        List<Symbol> rightSide = Arrays.asList(term, plus, expr);
        Production production = new Production(expr, rightSide);

        assertEquals(expr, production.getLeftSide());
        assertEquals(rightSide, production.getRightSide());
        assertEquals(0, production.getId());
        assertEquals(3, production.getRightSideLength());
        assertFalse(production.isEpsilonProduction());
    }

    @Test
    void testConstructorWithVarArgs() {
        Production production = new Production(expr, term, plus, expr);

        assertEquals(expr, production.getLeftSide());
        assertEquals(Arrays.asList(term, plus, expr), production.getRightSide());
        assertEquals(0, production.getId());
        assertEquals(3, production.getRightSideLength());
        assertFalse(production.isEpsilonProduction());
    }

    @Test
    void testConstructorWithEmptyRightSide() {
        Production production = new Production(expr, Collections.emptyList());

        assertEquals(expr, production.getLeftSide());
        assertTrue(production.getRightSide().isEmpty());
        assertEquals(0, production.getId());
        assertEquals(0, production.getRightSideLength());
        assertTrue(production.isEpsilonProduction());
    }

    @Test
    void testConstructorWithNoVarArgs() {
        Production production = new Production(expr);

        assertEquals(expr, production.getLeftSide());
        assertTrue(production.getRightSide().isEmpty());
        assertTrue(production.isEpsilonProduction());
    }

    @Test
    void testConstructorWithNullLeftSide() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Production(null, Arrays.asList(term, plus));
        });
    }

    @Test
    void testConstructorWithNullRightSideList() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Production(expr, (List<Symbol>) null);
        });
    }

    @Test
    void testConstructorFiltersNullSymbols() {
        Production production = new Production(expr, term, null, plus, null);

        assertEquals(Arrays.asList(term, plus), production.getRightSide());
        assertEquals(2, production.getRightSideLength());
    }

    @Test
    void testUniqueIds() {
        Production prod1 = new Production(expr, term);
        Production prod2 = new Production(expr, plus);
        Production prod3 = new Production(term, number);

        assertEquals(0, prod1.getId());
        assertEquals(1, prod2.getId());
        assertEquals(2, prod3.getId());
    }

    @Test
    void testGetRightSideSymbol() {
        Production production = new Production(expr, term, plus, expr);

        assertEquals(term, production.getRightSideSymbol(0));
        assertEquals(plus, production.getRightSideSymbol(1));
        assertEquals(expr, production.getRightSideSymbol(2));
    }

    @Test
    void testGetRightSideSymbolOutOfBounds() {
        Production production = new Production(expr, term, plus);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            production.getRightSideSymbol(-1);
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            production.getRightSideSymbol(3);
        });
    }

    @Test
    void testContainsSymbol() {
        Production production = new Production(expr, term, plus, expr);

        assertTrue(production.containsSymbol(term));
        assertTrue(production.containsSymbol(plus));
        assertTrue(production.containsSymbol(expr));
        assertFalse(production.containsSymbol(number));
    }

    @Test
    void testContainsSymbolInEpsilonProduction() {
        Production production = new Production(expr);

        assertFalse(production.containsSymbol(term));
        assertFalse(production.containsSymbol(plus));
    }

    @Test
    void testIsValid() {
        Production validProduction = new Production(expr, term, plus, expr);
        assertTrue(validProduction.isValid());

        Production epsilonProduction = new Production(expr);
        assertTrue(epsilonProduction.isValid());
    }

    @Test
    void testValidationWithNullSymbolInList() {
        List<Symbol> rightSideWithNull = Arrays.asList(term, null, plus);
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Production(expr, rightSideWithNull);
        });
    }

    @Test
    void testEquals() {
        Production prod1 = new Production(expr, term, plus, expr);
        Production prod2 = new Production(expr, term, plus, expr);
        Production prod3 = new Production(expr, term, plus, term);
        Production prod4 = new Production(term, plus, expr);

        assertEquals(prod1, prod2);
        assertNotEquals(prod1, prod3);
        assertNotEquals(prod1, prod4);
        assertNotEquals(prod1, null);
        assertNotEquals(prod1, "not a production");
    }

    @Test
    void testHashCode() {
        Production prod1 = new Production(expr, term, plus, expr);
        Production prod2 = new Production(expr, term, plus, expr);
        Production prod3 = new Production(expr, term, plus, term);

        assertEquals(prod1.hashCode(), prod2.hashCode());
        assertNotEquals(prod1.hashCode(), prod3.hashCode());
    }

    @Test
    void testToString() {
        Production production = new Production(expr, term, plus, expr);
        assertEquals("expr ::= term PLUS expr", production.toString());
    }

    @Test
    void testToStringEpsilonProduction() {
        Production production = new Production(expr);
        assertEquals("expr ::= ε", production.toString());
    }

    @Test
    void testToDetailedString() {
        Production production = new Production(expr, term, plus, expr);
        assertEquals("[0] expr ::= term PLUS expr", production.toDetailedString());
    }

    @Test
    void testRightSideIsUnmodifiable() {
        Production production = new Production(expr, term, plus, expr);
        List<Symbol> rightSide = production.getRightSide();

        assertThrows(UnsupportedOperationException.class, () -> {
            rightSide.add(number);
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            rightSide.remove(0);
        });
    }

    @Test
    void testComplexGrammarProductions() {
        // Test more complex productions like those in arithmetic grammar
        NonTerminal factor = new NonTerminal("factor");
        
        // expr ::= expr + term
        Production exprProd = new Production(expr, expr, plus, term);
        assertEquals("expr ::= expr PLUS term", exprProd.toString());
        
        // factor ::= ( expr )
        Production factorProd = new Production(factor, leftParen, expr, rightParen);
        assertEquals("factor ::= LPAREN expr RPAREN", factorProd.toString());
        
        // term ::= NUMBER
        Production termProd = new Production(term, number);
        assertEquals("term ::= NUMBER", termProd.toString());
    }

    @Test
    void testResetIdCounter() {
        Production prod1 = new Production(expr, term);
        Production prod2 = new Production(expr, plus);
        
        assertEquals(0, prod1.getId());
        assertEquals(1, prod2.getId());
        
        Production.resetIdCounter();
        
        Production prod3 = new Production(expr, number);
        assertEquals(0, prod3.getId());
    }
}