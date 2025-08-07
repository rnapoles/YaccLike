package com.parsergenerator.symbol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Symbol Tests")
class SymbolTest {

    @Test
    @DisplayName("Terminal symbol creation and properties")
    void testTerminalSymbol() {
        Terminal terminal = new Terminal("IDENTIFIER");
        
        assertEquals("IDENTIFIER", terminal.getName());
        assertEquals(SymbolType.TERMINAL, terminal.getType());
        assertTrue(terminal.isTerminal());
        assertFalse(terminal.isNonTerminal());
        assertFalse(terminal.hasPattern());
        assertNull(terminal.getPattern());
    }

    @Test
    @DisplayName("Terminal symbol with pattern")
    void testTerminalWithPattern() {
        Terminal terminal = new Terminal("NUMBER", "[0-9]+");
        
        assertEquals("NUMBER", terminal.getName());
        assertEquals("[0-9]+", terminal.getPattern());
        assertTrue(terminal.hasPattern());
        assertEquals("NUMBER = {[0-9]+}", terminal.toString());
    }

    @Test
    @DisplayName("NonTerminal symbol creation and properties")
    void testNonTerminalSymbol() {
        NonTerminal nonTerminal = new NonTerminal("Expression");
        
        assertEquals("Expression", nonTerminal.getName());
        assertEquals(SymbolType.NON_TERMINAL, nonTerminal.getType());
        assertFalse(nonTerminal.isTerminal());
        assertTrue(nonTerminal.isNonTerminal());
        assertFalse(nonTerminal.hasProductions());
        assertEquals(0, nonTerminal.getProductionCount());
        assertEquals("<Expression>", nonTerminal.toString());
    }

    @Test
    @DisplayName("Symbol equality and hash code")
    void testSymbolEquality() {
        Terminal term1 = new Terminal("PLUS");
        Terminal term2 = new Terminal("PLUS");
        Terminal term3 = new Terminal("MINUS");
        
        assertEquals(term1, term2);
        assertNotEquals(term1, term3);
        assertEquals(term1.hashCode(), term2.hashCode());
        
        NonTerminal nt1 = new NonTerminal("Expr");
        NonTerminal nt2 = new NonTerminal("Expr");
        NonTerminal nt3 = new NonTerminal("Term");
        
        assertEquals(nt1, nt2);
        assertNotEquals(nt1, nt3);
        assertEquals(nt1.hashCode(), nt2.hashCode());
        
        // Terminal and NonTerminal with same name should not be equal
        Terminal termExpr = new Terminal("Expr");
        assertNotEquals(nt1, termExpr);
    }

    @Test
    @DisplayName("Invalid symbol names should throw exception")
    void testInvalidSymbolNames() {
        assertThrows(IllegalArgumentException.class, () -> new Terminal(null));
        assertThrows(IllegalArgumentException.class, () -> new Terminal(""));
        assertThrows(IllegalArgumentException.class, () -> new Terminal("   "));
        
        assertThrows(IllegalArgumentException.class, () -> new NonTerminal(null));
        assertThrows(IllegalArgumentException.class, () -> new NonTerminal(""));
        assertThrows(IllegalArgumentException.class, () -> new NonTerminal("   "));
    }

    @Test
    @DisplayName("Symbol name trimming")
    void testSymbolNameTrimming() {
        Terminal terminal = new Terminal("  IDENTIFIER  ");
        NonTerminal nonTerminal = new NonTerminal("  Expression  ");
        
        assertEquals("IDENTIFIER", terminal.getName());
        assertEquals("Expression", nonTerminal.getName());
    }
}