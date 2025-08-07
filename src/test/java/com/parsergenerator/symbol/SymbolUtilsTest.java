package com.parsergenerator.symbol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@DisplayName("SymbolUtils Tests")
class SymbolUtilsTest {

    @Test
    @DisplayName("Filter terminals from mixed symbol collection")
    void testGetTerminals() {
        Terminal term1 = new Terminal("PLUS");
        Terminal term2 = new Terminal("MINUS");
        NonTerminal nt1 = new NonTerminal("Expression");
        NonTerminal nt2 = new NonTerminal("Term");
        
        List<Symbol> symbols = Arrays.asList(term1, nt1, term2, nt2);
        Set<Terminal> terminals = SymbolUtils.getTerminals(symbols);
        
        assertEquals(2, terminals.size());
        assertTrue(terminals.contains(term1));
        assertTrue(terminals.contains(term2));
    }

    @Test
    @DisplayName("Filter non-terminals from mixed symbol collection")
    void testGetNonTerminals() {
        Terminal term1 = new Terminal("PLUS");
        Terminal term2 = new Terminal("MINUS");
        NonTerminal nt1 = new NonTerminal("Expression");
        NonTerminal nt2 = new NonTerminal("Term");
        
        List<Symbol> symbols = Arrays.asList(term1, nt1, term2, nt2);
        Set<NonTerminal> nonTerminals = SymbolUtils.getNonTerminals(symbols);
        
        assertEquals(2, nonTerminals.size());
        assertTrue(nonTerminals.contains(nt1));
        assertTrue(nonTerminals.contains(nt2));
    }

    @Test
    @DisplayName("Handle null collections gracefully")
    void testNullCollections() {
        Set<Terminal> terminals = SymbolUtils.getTerminals(null);
        Set<NonTerminal> nonTerminals = SymbolUtils.getNonTerminals(null);
        
        assertNotNull(terminals);
        assertNotNull(nonTerminals);
        assertTrue(terminals.isEmpty());
        assertTrue(nonTerminals.isEmpty());
    }

    @Test
    @DisplayName("Terminal name recognition heuristics")
    void testIsTerminalName() {
        // Typical terminal names (uppercase)
        assertTrue(SymbolUtils.isTerminalName("IDENTIFIER"));
        assertTrue(SymbolUtils.isTerminalName("NUMBER"));
        assertTrue(SymbolUtils.isTerminalName("PLUS"));
        
        // Names with special characters
        assertTrue(SymbolUtils.isTerminalName("+="));
        assertTrue(SymbolUtils.isTerminalName("->"));
        
        // Non-terminal names (angle brackets)
        assertFalse(SymbolUtils.isTerminalName("<Expression>"));
        assertFalse(SymbolUtils.isTerminalName("<Term>"));
        
        // Mixed case (considered non-terminal by convention)
        assertFalse(SymbolUtils.isTerminalName("Expression"));
        assertFalse(SymbolUtils.isTerminalName("term"));
        
        // Invalid names
        assertFalse(SymbolUtils.isTerminalName(null));
        assertFalse(SymbolUtils.isTerminalName(""));
        assertFalse(SymbolUtils.isTerminalName("   "));
    }

    @Test
    @DisplayName("Symbol creation utilities")
    void testSymbolCreation() {
        Terminal terminal = SymbolUtils.createTerminal("IDENTIFIER");
        assertEquals("IDENTIFIER", terminal.getName());
        assertTrue(terminal.isTerminal());
        
        Terminal terminalWithPattern = SymbolUtils.createTerminal("NUMBER", "[0-9]+");
        assertEquals("NUMBER", terminalWithPattern.getName());
        assertEquals("[0-9]+", terminalWithPattern.getPattern());
        
        NonTerminal nonTerminal = SymbolUtils.createNonTerminal("Expression");
        assertEquals("Expression", nonTerminal.getName());
        assertTrue(nonTerminal.isNonTerminal());
    }

    @Test
    @DisplayName("Symbol name validation")
    void testIsValidSymbolName() {
        // Valid names
        assertTrue(SymbolUtils.isValidSymbolName("IDENTIFIER"));
        assertTrue(SymbolUtils.isValidSymbolName("Expression"));
        assertTrue(SymbolUtils.isValidSymbolName("<Term>"));
        assertTrue(SymbolUtils.isValidSymbolName("_underscore"));
        assertTrue(SymbolUtils.isValidSymbolName("name123"));
        
        // Invalid names
        assertFalse(SymbolUtils.isValidSymbolName(null));
        assertFalse(SymbolUtils.isValidSymbolName(""));
        assertFalse(SymbolUtils.isValidSymbolName("   "));
        assertFalse(SymbolUtils.isValidSymbolName("name with spaces"));
        assertFalse(SymbolUtils.isValidSymbolName("123name")); // starts with number
        
        // Very long name
        String longName = "a".repeat(101);
        assertFalse(SymbolUtils.isValidSymbolName(longName));
    }

    @Test
    @DisplayName("Utility class cannot be instantiated")
    void testUtilityClassInstantiation() {
        var exception = assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
            // Use reflection to try to instantiate the utility class
            var constructor = SymbolUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
        
        // Verify the cause is the expected UnsupportedOperationException
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }
}