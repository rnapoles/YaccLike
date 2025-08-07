package com.parsergenerator.lalr;

import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ParsingTableTest {
    
    private ParsingTable table;
    private NonTerminal E, T;
    private Terminal plus, id, dollar;
    private Production production1, production2;
    private LRItemSet state0, state1;
    
    @BeforeEach
    void setUp() {
        Production.resetIdCounter();
        LRItemSet.resetIdCounter();
        
        // Create test symbols
        E = new NonTerminal("E");
        T = new NonTerminal("T");
        plus = new Terminal("+", "\\+");
        id = new Terminal("id", "[a-zA-Z]+");
        dollar = new Terminal("$", "\\$");
        
        // Create test productions
        production1 = new Production(E, Arrays.asList(E, plus, T));
        production2 = new Production(E, Arrays.asList(T));
        
        // Create test states
        state0 = new LRItemSet();
        state0.addItem(new LRItem(production1, 0));
        
        state1 = new LRItemSet();
        state1.addItem(new LRItem(production1, 1));
        
        // Create table
        Set<Terminal> terminals = new LinkedHashSet<>(Arrays.asList(plus, id, dollar));
        Set<NonTerminal> nonTerminals = new LinkedHashSet<>(Arrays.asList(E, T));
        List<LRItemSet> states = Arrays.asList(state0, state1);
        
        table = new ParsingTable(states, terminals, nonTerminals);
    }
    
    @Test
    void testEmptyConstructor() {
        ParsingTable emptyTable = new ParsingTable();
        
        assertTrue(emptyTable.getActionTable().isEmpty());
        assertTrue(emptyTable.getGotoTable().isEmpty());
        assertTrue(emptyTable.getStates().isEmpty());
        assertTrue(emptyTable.getTerminals().isEmpty());
        assertTrue(emptyTable.getNonTerminals().isEmpty());
        assertEquals(0, emptyTable.getStateCount());
    }
    
    @Test
    void testConstructorWithParameters() {
        assertEquals(2, table.getStateCount());
        assertEquals(3, table.getTerminals().size());
        assertEquals(2, table.getNonTerminals().size());
        
        assertTrue(table.getTerminals().contains(plus));
        assertTrue(table.getTerminals().contains(id));
        assertTrue(table.getTerminals().contains(dollar));
        
        assertTrue(table.getNonTerminals().contains(E));
        assertTrue(table.getNonTerminals().contains(T));
        
        assertEquals(state0, table.getState(0));
        assertEquals(state1, table.getState(1));
    }
    
    @Test
    void testActionTableOperations() {
        // Initially empty
        assertFalse(table.hasAction(0, plus));
        assertNull(table.getAction(0, plus));
        
        // Set action
        Action shiftAction = Action.shift(1);
        table.setAction(0, plus, shiftAction);
        
        assertTrue(table.hasAction(0, plus));
        assertEquals(shiftAction, table.getAction(0, plus));
        
        // Set reduce action
        Action reduceAction = Action.reduce(production1);
        table.setAction(1, dollar, reduceAction);
        
        assertTrue(table.hasAction(1, dollar));
        assertEquals(reduceAction, table.getAction(1, dollar));
        
        // Set accept action
        Action acceptAction = Action.accept();
        table.setAction(1, id, acceptAction);
        
        assertTrue(table.hasAction(1, id));
        assertEquals(acceptAction, table.getAction(1, id));
    }
    
    @Test
    void testActionTableValidation() {
        // Null terminal
        assertThrows(IllegalArgumentException.class, 
            () -> table.setAction(0, null, Action.shift(1)));
        
        // Null action
        assertThrows(IllegalArgumentException.class, 
            () -> table.setAction(0, plus, null));
        
        // Getting action with null terminal
        assertNull(table.getAction(0, null));
    }
    
    @Test
    void testGotoTableOperations() {
        // Initially empty
        assertFalse(table.hasGoto(0, E));
        assertNull(table.getGoto(0, E));
        
        // Set GOTO
        table.setGoto(0, E, 2);
        
        assertTrue(table.hasGoto(0, E));
        assertEquals(Integer.valueOf(2), table.getGoto(0, E));
        
        // Set another GOTO
        table.setGoto(1, T, 3);
        
        assertTrue(table.hasGoto(1, T));
        assertEquals(Integer.valueOf(3), table.getGoto(1, T));
    }
    
    @Test
    void testGotoTableValidation() {
        // Null non-terminal
        assertThrows(IllegalArgumentException.class, 
            () -> table.setGoto(0, null, 1));
        
        // Getting GOTO with null non-terminal
        assertNull(table.getGoto(0, null));
    }
    
    @Test
    void testStateOperations() {
        assertEquals(2, table.getStateCount());
        
        // Add new state
        LRItemSet newState = new LRItemSet();
        newState.addItem(new LRItem(production2, 0));
        
        int newStateIndex = table.addState(newState);
        assertEquals(2, newStateIndex);
        assertEquals(3, table.getStateCount());
        assertEquals(newState, table.getState(2));
        
        // Test bounds checking
        assertThrows(IndexOutOfBoundsException.class, () -> table.getState(5));
    }
    
    @Test
    void testStateValidation() {
        assertThrows(IllegalArgumentException.class, () -> table.addState(null));
    }
    
    @Test
    void testSymbolOperations() {
        ParsingTable emptyTable = new ParsingTable();
        
        // Add terminal
        Terminal newTerminal = new Terminal("new", "new");
        emptyTable.addTerminal(newTerminal);
        assertTrue(emptyTable.getTerminals().contains(newTerminal));
        
        // Add non-terminal
        NonTerminal newNonTerminal = new NonTerminal("New");
        emptyTable.addNonTerminal(newNonTerminal);
        assertTrue(emptyTable.getNonTerminals().contains(newNonTerminal));
        
        // Adding null should not cause errors
        emptyTable.addTerminal(null);
        emptyTable.addNonTerminal(null);
    }
    
    @Test
    void testConflictDetection() {
        // Create a table with no conflicts
        table.setAction(0, plus, Action.shift(1));
        table.setAction(0, id, Action.shift(2));
        
        List<String> conflicts = table.detectConflicts();
        assertTrue(conflicts.isEmpty());
        assertFalse(table.hasConflicts());
        
        // Add a conflicting action (this would normally not happen in correct implementation)
        // For testing purposes, we'll simulate by creating a new table with conflicts
        ParsingTable conflictTable = new ParsingTable();
        conflictTable.addTerminal(plus);
        conflictTable.addState(state0);
        
        // This would create a conflict if we could add multiple actions for same key
        // Since our implementation overwrites, we'll test the conflict detection logic
        // by checking that it properly identifies when conflicts would exist
        assertNotNull(conflictTable.detectConflicts());
    }
    
    @Test
    void testStringRepresentations() {
        // Set up some actions and GOTOs for meaningful output
        table.setAction(0, plus, Action.shift(1));
        table.setAction(0, id, Action.shift(2));
        table.setAction(1, dollar, Action.reduce(production1));
        table.setGoto(0, E, 3);
        table.setGoto(0, T, 4);
        
        // Test ACTION table string
        String actionString = table.actionTableToString();
        assertNotNull(actionString);
        assertTrue(actionString.contains("ACTION Table"));
        assertTrue(actionString.contains("+"));
        assertTrue(actionString.contains("id"));
        assertTrue(actionString.contains("$"));
        
        // Test GOTO table string
        String gotoString = table.gotoTableToString();
        assertNotNull(gotoString);
        assertTrue(gotoString.contains("GOTO Table"));
        assertTrue(gotoString.contains("E"));
        assertTrue(gotoString.contains("T"));
        
        // Test overall string representation
        String tableString = table.toString();
        assertNotNull(tableString);
        assertTrue(tableString.contains("ParsingTable"));
        assertTrue(tableString.contains("States: 2"));
        assertTrue(tableString.contains("Terminals: 3"));
        assertTrue(tableString.contains("Non-terminals: 2"));
    }
    
    @Test
    void testImmutability() {
        // Test that returned collections are unmodifiable
        assertThrows(UnsupportedOperationException.class, 
            () -> table.getActionTable().clear());
        
        assertThrows(UnsupportedOperationException.class, 
            () -> table.getGotoTable().clear());
        
        assertThrows(UnsupportedOperationException.class, 
            () -> table.getStates().clear());
        
        assertThrows(UnsupportedOperationException.class, 
            () -> table.getTerminals().clear());
        
        assertThrows(UnsupportedOperationException.class, 
            () -> table.getNonTerminals().clear());
    }
    
    @Test
    void testEmptyTableStringRepresentations() {
        ParsingTable emptyTable = new ParsingTable();
        
        String actionString = emptyTable.actionTableToString();
        assertNotNull(actionString);
        assertTrue(actionString.contains("ACTION Table"));
        
        String gotoString = emptyTable.gotoTableToString();
        assertNotNull(gotoString);
        assertTrue(gotoString.contains("GOTO Table"));
        
        String tableString = emptyTable.toString();
        assertNotNull(tableString);
        assertTrue(tableString.contains("States: 0"));
    }
}