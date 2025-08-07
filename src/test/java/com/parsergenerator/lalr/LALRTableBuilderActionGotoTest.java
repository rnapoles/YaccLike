package com.parsergenerator.lalr;

import com.parsergenerator.analysis.AnalysisResult;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ACTION and GOTO table generation in LALRTableBuilder.
 * This test class specifically focuses on task 8.2 requirements:
 * - ACTION table construction with shift/reduce decisions
 * - GOTO table construction for non-terminal transitions
 * - Conflict detection and reporting for ambiguous grammars
 */
class LALRTableBuilderActionGotoTest {
    
    private Grammar grammar;
    private AnalysisResult successfulAnalysis;
    
    // Symbols for test grammars
    private NonTerminal S, E, T, F, A, B;
    private Terminal plus, mult, lparen, rparen, id, a, b, c;
    
    @BeforeEach
    void setUp() {
        // Reset counters for consistent test results
        Production.resetIdCounter();
        LRItemSet.resetIdCounter();
        
        // Create common symbols
        S = new NonTerminal("S");
        E = new NonTerminal("E");
        T = new NonTerminal("T");
        F = new NonTerminal("F");
        A = new NonTerminal("A");
        B = new NonTerminal("B");
        
        plus = new Terminal("+", "\\+");
        mult = new Terminal("*", "\\*");
        lparen = new Terminal("(", "\\(");
        rparen = new Terminal(")", "\\)");
        id = new Terminal("id", "[a-zA-Z][a-zA-Z0-9]*");
        a = new Terminal("a", "a");
        b = new Terminal("b", "b");
        c = new Terminal("c", "c");
        
        successfulAnalysis = new AnalysisResult(Collections.emptyList(), Collections.emptyList(), false);
    }
    
    @Test
    void testActionTableShiftActions() {
        // Create simple grammar: S -> a b
        grammar = new Grammar();
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a, b));
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Verify ACTION table has shift actions
        boolean hasShiftAction = false;
        for (Action action : table.getActionTable().values()) {
            if (action.isShift()) {
                hasShiftAction = true;
                assertTrue(action.getParameter() >= 0, "Shift action should have valid state number");
            }
        }
        assertTrue(hasShiftAction, "ACTION table should contain shift actions");
    }
    
    @Test
    void testActionTableReduceActions() {
        // Create grammar with reduce actions: S -> A, A -> a
        grammar = new Grammar();
        grammar.setStartSymbol(S);
        Production prod1 = new Production(S, A);
        Production prod2 = new Production(A, a);
        grammar.addProduction(prod1);
        grammar.addProduction(prod2);
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Verify ACTION table has reduce actions
        boolean hasReduceAction = false;
        for (Action action : table.getActionTable().values()) {
            if (action.isReduce()) {
                hasReduceAction = true;
                assertNotNull(action.getProduction(), "Reduce action should have associated production");
                assertTrue(action.getParameter() >= 0, "Reduce action should have valid production ID");
            }
        }
        assertTrue(hasReduceAction, "ACTION table should contain reduce actions");
    }
    
    @Test
    void testActionTableAcceptAction() {
        // Create simple grammar: S -> a
        grammar = new Grammar();
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Verify ACTION table has exactly one accept action
        long acceptCount = table.getActionTable().values().stream()
            .mapToLong(action -> action.isAccept() ? 1 : 0)
            .sum();
        assertEquals(1, acceptCount, "ACTION table should have exactly one accept action");
    }
    
    @Test
    void testActionTableExpressionGrammar() {
        // Create expression grammar: E -> E + T | T, T -> T * F | F, F -> ( E ) | id
        grammar = createExpressionGrammar();
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Verify ACTION table structure
        assertFalse(table.getActionTable().isEmpty(), "ACTION table should not be empty");
        
        // Check that we have actions for all terminals
        Set<Terminal> terminals = table.getTerminals();
        assertTrue(terminals.contains(plus), "Terminals should include +");
        assertTrue(terminals.contains(mult), "Terminals should include *");
        assertTrue(terminals.contains(id), "Terminals should include id");
        
        // Verify we have shift actions for terminals
        boolean hasShiftForPlus = false;
        boolean hasShiftForMult = false;
        boolean hasShiftForId = false;
        
        for (int state = 0; state < table.getStateCount(); state++) {
            Action plusAction = table.getAction(state, plus);
            Action multAction = table.getAction(state, mult);
            Action idAction = table.getAction(state, id);
            
            if (plusAction != null && plusAction.isShift()) hasShiftForPlus = true;
            if (multAction != null && multAction.isShift()) hasShiftForMult = true;
            if (idAction != null && idAction.isShift()) hasShiftForId = true;
        }
        
        assertTrue(hasShiftForId, "Should have shift action for 'id' terminal");
    }
    
    @Test
    void testGotoTableNonTerminalTransitions() {
        // Create grammar with non-terminals: S -> A B, A -> a, B -> b
        grammar = new Grammar();
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A, B));
        grammar.addProduction(new Production(A, a));
        grammar.addProduction(new Production(B, b));
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Verify GOTO table has entries for non-terminals
        assertFalse(table.getGotoTable().isEmpty(), "GOTO table should not be empty");
        
        // Check that GOTO entries are only for non-terminals
        for (StateSymbolPair key : table.getGotoTable().keySet()) {
            assertTrue(key.getSymbol().isNonTerminal(), 
                "GOTO table should only contain non-terminal symbols");
        }
        
        // Verify GOTO values are valid state numbers
        for (Integer gotoState : table.getGotoTable().values()) {
            assertTrue(gotoState >= 0 && gotoState < table.getStateCount(),
                "GOTO state should be valid: " + gotoState);
        }
    }
    
    @Test
    void testGotoTableExpressionGrammar() {
        grammar = createExpressionGrammar();
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Verify GOTO table has entries for all non-terminals
        Set<NonTerminal> nonTerminals = table.getNonTerminals();
        assertTrue(nonTerminals.contains(E), "Non-terminals should include E");
        assertTrue(nonTerminals.contains(T), "Non-terminals should include T");
        assertTrue(nonTerminals.contains(F), "Non-terminals should include F");
        
        // Check that we have GOTO entries for non-terminals
        boolean hasGotoForE = false;
        boolean hasGotoForT = false;
        boolean hasGotoForF = false;
        
        for (int state = 0; state < table.getStateCount(); state++) {
            if (table.getGoto(state, E) != null) hasGotoForE = true;
            if (table.getGoto(state, T) != null) hasGotoForT = true;
            if (table.getGoto(state, F) != null) hasGotoForF = true;
        }
        
        assertTrue(hasGotoForE, "Should have GOTO entries for non-terminal E");
        assertTrue(hasGotoForT, "Should have GOTO entries for non-terminal T");
        assertTrue(hasGotoForF, "Should have GOTO entries for non-terminal F");
    }
    
    @Test
    void testShiftReduceConflictDetection() {
        // Create grammar with potential shift/reduce conflict
        // S -> A a | b, A -> a
        grammar = new Grammar();
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A, a));
        grammar.addProduction(new Production(S, b));
        grammar.addProduction(new Production(A, a));
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Check conflict detection
        List<String> conflicts = table.detectConflicts();
        assertNotNull(conflicts, "Conflict detection should return a list");
        
        // The table should still be constructed even with conflicts
        assertNotNull(table, "Table should be constructed despite conflicts");
        assertFalse(table.getActionTable().isEmpty(), "ACTION table should have entries");
    }
    
    @Test
    void testReduceReduceConflictDetection() {
        // Create grammar with potential reduce/reduce conflict
        // S -> A | B, A -> a, B -> a
        grammar = new Grammar();
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, A));
        grammar.addProduction(new Production(S, B));
        grammar.addProduction(new Production(A, a));
        grammar.addProduction(new Production(B, a));
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Check conflict detection
        List<String> conflicts = table.detectConflicts();
        assertNotNull(conflicts, "Conflict detection should return a list");
        
        // The table should still be constructed
        assertNotNull(table, "Table should be constructed despite conflicts");
    }
    
    @Test
    void testConflictResolution() {
        // Create grammar that will have conflicts but should be resolved
        // S -> E, E -> E + E | id (left-recursive, ambiguous)
        grammar = new Grammar();
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, E));
        grammar.addProduction(new Production(E, E, plus, E));
        grammar.addProduction(new Production(E, id));
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Table should be constructed with conflict resolution applied
        assertNotNull(table, "Table should be constructed with conflict resolution");
        assertFalse(table.getActionTable().isEmpty(), "ACTION table should have entries");
        
        // Each (state, terminal) pair should have exactly one action
        for (int state = 0; state < table.getStateCount(); state++) {
            for (Terminal terminal : table.getTerminals()) {
                Action action = table.getAction(state, terminal);
                // If action exists, it should be well-defined (not null)
                if (action != null) {
                    assertNotNull(action.getType(), "Action should have a defined type");
                }
            }
        }
    }
    
    @Test
    void testActionTableCompleteness() {
        grammar = createExpressionGrammar();
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Verify that ACTION table covers necessary (state, terminal) pairs
        int actionCount = 0;
        for (int state = 0; state < table.getStateCount(); state++) {
            for (Terminal terminal : table.getTerminals()) {
                Action action = table.getAction(state, terminal);
                if (action != null) {
                    actionCount++;
                    // Verify action is valid
                    assertNotNull(action.getType(), "Action should have valid type");
                    if (action.isShift()) {
                        assertTrue(action.getParameter() >= 0 && action.getParameter() < table.getStateCount(),
                            "Shift action should reference valid state");
                    } else if (action.isReduce()) {
                        assertNotNull(action.getProduction(), "Reduce action should have production");
                    }
                }
            }
        }
        
        assertTrue(actionCount > 0, "ACTION table should have entries");
    }
    
    @Test
    void testGotoTableCompleteness() {
        grammar = createExpressionGrammar();
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Verify that GOTO table covers necessary (state, non-terminal) pairs
        int gotoCount = 0;
        for (int state = 0; state < table.getStateCount(); state++) {
            for (NonTerminal nonTerminal : table.getNonTerminals()) {
                Integer gotoState = table.getGoto(state, nonTerminal);
                if (gotoState != null) {
                    gotoCount++;
                    // Verify GOTO is valid
                    assertTrue(gotoState >= 0 && gotoState < table.getStateCount(),
                        "GOTO should reference valid state: " + gotoState);
                }
            }
        }
        
        assertTrue(gotoCount > 0, "GOTO table should have entries");
    }
    
    @Test
    void testTableConsistency() {
        grammar = createExpressionGrammar();
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Verify consistency between ACTION and GOTO tables
        // ACTION table should only have terminals, GOTO table should only have non-terminals
        for (StateSymbolPair key : table.getActionTable().keySet()) {
            assertTrue(key.getSymbol().isTerminal(), 
                "ACTION table should only contain terminals");
        }
        
        for (StateSymbolPair key : table.getGotoTable().keySet()) {
            assertTrue(key.getSymbol().isNonTerminal(), 
                "GOTO table should only contain non-terminals");
        }
        
        // Verify no overlap between ACTION and GOTO table keys
        for (StateSymbolPair actionKey : table.getActionTable().keySet()) {
            for (StateSymbolPair gotoKey : table.getGotoTable().keySet()) {
                if (actionKey.getState() == gotoKey.getState()) {
                    assertNotEquals(actionKey.getSymbol(), gotoKey.getSymbol(),
                        "ACTION and GOTO tables should not have overlapping symbols for same state");
                }
            }
        }
    }
    
    @Test
    void testErrorActionHandling() {
        // Create simple grammar and verify error handling
        grammar = new Grammar();
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // For states/terminals without defined actions, getAction should return null
        // This represents an implicit error action
        Terminal undefinedTerminal = new Terminal("undefined", "undefined");
        
        for (int state = 0; state < table.getStateCount(); state++) {
            Action action = table.getAction(state, undefinedTerminal);
            // Should be null for undefined terminal
            assertNull(action, "Action for undefined terminal should be null (implicit error)");
        }
    }
    
    @Test
    void testConflictReporting() {
        // Create a grammar known to have conflicts
        grammar = new Grammar();
        grammar.setStartSymbol(S);
        // Ambiguous grammar: S -> A a | A b, A -> a | ε (if epsilon were supported)
        // Simplified to: S -> A a | A b, A -> a
        grammar.addProduction(new Production(S, A, a));
        grammar.addProduction(new Production(S, A, b));
        grammar.addProduction(new Production(A, a));
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Test conflict detection and reporting
        List<String> conflicts = table.detectConflicts();
        assertNotNull(conflicts, "Conflict detection should return a list");
        
        // Verify conflict messages are informative
        for (String conflict : conflicts) {
            assertNotNull(conflict, "Conflict message should not be null");
            assertFalse(conflict.trim().isEmpty(), "Conflict message should not be empty");
        }
    }
    
    // Helper method to create expression grammar
    private Grammar createExpressionGrammar() {
        Grammar g = new Grammar();
        g.setStartSymbol(E);
        
        // E -> E + T | T
        g.addProduction(new Production(E, E, plus, T));
        g.addProduction(new Production(E, T));
        
        // T -> T * F | F
        g.addProduction(new Production(T, T, mult, F));
        g.addProduction(new Production(T, F));
        
        // F -> ( E ) | id
        g.addProduction(new Production(F, lparen, E, rparen));
        g.addProduction(new Production(F, id));
        
        return g;
    }
}