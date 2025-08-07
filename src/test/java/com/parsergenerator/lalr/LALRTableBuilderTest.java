package com.parsergenerator.lalr;

import com.parsergenerator.analysis.AnalysisResult;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LALRTableBuilderTest {
    
    private Grammar grammar;
    private AnalysisResult successfulAnalysis;
    private AnalysisResult failedAnalysis;
    
    // Symbols for simple expression grammar
    private NonTerminal E, T, F;
    private Terminal plus, mult, lparen, rparen, id;
    
    @BeforeEach
    void setUp() {
        // Reset counters for consistent test results
        Production.resetIdCounter();
        LRItemSet.resetIdCounter();
        
        // Create symbols for simple expression grammar
        E = new NonTerminal("E");
        T = new NonTerminal("T");
        F = new NonTerminal("F");
        plus = new Terminal("+", "\\+");
        mult = new Terminal("*", "\\*");
        lparen = new Terminal("(", "\\(");
        rparen = new Terminal(")", "\\)");
        id = new Terminal("id", "[a-zA-Z][a-zA-Z0-9]*");
        
        // Create successful and failed analysis results
        successfulAnalysis = new AnalysisResult(Collections.emptyList(), Collections.emptyList(), false);
        failedAnalysis = new AnalysisResult(Arrays.asList("Grammar has errors"), Collections.emptyList(), true);
    }
    
    @Test
    void testConstructorValidation() {
        grammar = createSimpleGrammar();
        
        // Null grammar
        assertThrows(IllegalArgumentException.class, 
            () -> new LALRTableBuilder(null, successfulAnalysis));
        
        // Null analysis result
        assertThrows(IllegalArgumentException.class, 
            () -> new LALRTableBuilder(grammar, null));
        
        // Grammar without start symbol
        Grammar grammarWithoutStart = new Grammar();
        assertThrows(IllegalArgumentException.class, 
            () -> new LALRTableBuilder(grammarWithoutStart, successfulAnalysis));
    }
    
    @Test
    void testBuildTableWithAnalysisErrors() {
        grammar = createSimpleGrammar();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, failedAnalysis);
        
        assertThrows(IllegalStateException.class, builder::buildTable);
    }
    
    @Test
    void testBuildTableSimpleGrammar() {
        // Create a very simple grammar: S -> a
        grammar = new Grammar();
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a", "a");
        
        grammar.setStartSymbol(S);
        grammar.addProduction(new Production(S, a));
        
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        assertNotNull(table);
        assertTrue(table.getStateCount() > 0);
        assertFalse(table.getStates().isEmpty());
        assertFalse(table.getActionTable().isEmpty());
    }
    
    @Test
    void testBuildTableExpressionGrammar() {
        grammar = createExpressionGrammar();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        assertNotNull(table);
        assertTrue(table.getStateCount() > 0);
        
        // Verify that we have states
        List<LRItemSet> states = table.getStates();
        assertFalse(states.isEmpty());
        
        // Verify that we have ACTION entries
        assertFalse(table.getActionTable().isEmpty());
        
        // Verify that we have GOTO entries
        assertFalse(table.getGotoTable().isEmpty());
        
        // Check that terminals and non-terminals are properly set
        assertTrue(table.getTerminals().contains(plus));
        assertTrue(table.getTerminals().contains(mult));
        assertTrue(table.getTerminals().contains(id));
        assertTrue(table.getNonTerminals().contains(E));
        assertTrue(table.getNonTerminals().contains(T));
        assertTrue(table.getNonTerminals().contains(F));
    }
    
    @Test
    void testLR0AutomatonConstruction() {
        grammar = createSimpleGrammar();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        
        // Build the table to trigger automaton construction
        builder.buildTable();
        
        // Check that states were created
        List<LRItemSet> states = builder.getLR0States();
        assertFalse(states.isEmpty());
        
        // Check that transitions were created
        Map<Integer, Map<com.parsergenerator.symbol.Symbol, Integer>> transitions = builder.getTransitions();
        assertFalse(transitions.isEmpty());
        
        // Verify initial state exists
        assertTrue(states.size() > 0);
        LRItemSet initialState = states.get(0);
        assertNotNull(initialState);
        assertFalse(initialState.isEmpty());
    }
    
    @Test
    void testActionTableConstruction() {
        grammar = createSimpleGrammar();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Check that ACTION table has entries
        assertFalse(table.getActionTable().isEmpty());
        
        // Verify that we have at least one ACCEPT action
        boolean hasAcceptAction = table.getActionTable().values().stream()
            .anyMatch(Action::isAccept);
        assertTrue(hasAcceptAction, "Table should have at least one ACCEPT action");
        
        // Verify that we have SHIFT or REDUCE actions
        boolean hasShiftOrReduce = table.getActionTable().values().stream()
            .anyMatch(action -> action.isShift() || action.isReduce());
        assertTrue(hasShiftOrReduce, "Table should have SHIFT or REDUCE actions");
    }
    
    @Test
    void testGotoTableConstruction() {
        grammar = createExpressionGrammar();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Check that GOTO table has entries
        assertFalse(table.getGotoTable().isEmpty());
        
        // Verify that GOTO entries are for non-terminals only
        for (StateSymbolPair key : table.getGotoTable().keySet()) {
            assertTrue(key.getSymbol().isNonTerminal(), 
                "GOTO table should only contain non-terminals");
        }
    }
    
    @Test
    void testEndOfInputTerminal() {
        grammar = createSimpleGrammar();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        
        Terminal endOfInput = builder.getEndOfInputTerminal();
        assertNotNull(endOfInput);
        assertEquals("$", endOfInput.getName());
        
        // Build table and verify end-of-input is included in terminals
        ParsingTable table = builder.buildTable();
        assertTrue(table.getTerminals().contains(endOfInput));
    }
    
    @Test
    void testAutomatonStringRepresentation() {
        grammar = createSimpleGrammar();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        builder.buildTable();
        
        String automatonString = builder.automatonToString();
        assertNotNull(automatonString);
        assertFalse(automatonString.isEmpty());
        assertTrue(automatonString.contains("LR(0) Automaton"));
        assertTrue(automatonString.contains("State"));
    }
    
    @Test
    void testConflictDetection() {
        // Create a grammar that might have conflicts
        grammar = createAmbiguousGrammar();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // The table should be created even if there are conflicts
        assertNotNull(table);
        
        // Check conflict detection
        List<String> conflicts = table.detectConflicts();
        // Note: conflicts might or might not exist depending on the grammar
        assertNotNull(conflicts);
    }
    
    @Test
    void testTableStringRepresentations() {
        grammar = createSimpleGrammar();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        // Test ACTION table string representation
        String actionTableString = table.actionTableToString();
        assertNotNull(actionTableString);
        assertTrue(actionTableString.contains("ACTION Table"));
        
        // Test GOTO table string representation
        String gotoTableString = table.gotoTableToString();
        assertNotNull(gotoTableString);
        assertTrue(gotoTableString.contains("GOTO Table"));
        
        // Test overall table string representation
        String tableString = table.toString();
        assertNotNull(tableString);
        assertTrue(tableString.contains("ParsingTable"));
    }
    
    @Test
    void testStateCompaction() {
        // Create a grammar that will require state merging
        grammar = createGrammarRequiringMerging();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        assertNotNull(table);
        assertTrue(table.getStateCount() > 0);
        
        // Verify that all states are non-null
        for (LRItemSet state : table.getStates()) {
            assertNotNull(state);
        }
    }
    
    @Test
    void testTransitionConsistency() {
        grammar = createExpressionGrammar();
        LALRTableBuilder builder = new LALRTableBuilder(grammar, successfulAnalysis);
        ParsingTable table = builder.buildTable();
        
        Map<Integer, Map<com.parsergenerator.symbol.Symbol, Integer>> transitions = builder.getTransitions();
        
        // Verify that all transition targets are valid state indices
        for (Map<com.parsergenerator.symbol.Symbol, Integer> stateTransitions : transitions.values()) {
            for (Integer targetState : stateTransitions.values()) {
                assertTrue(targetState >= 0 && targetState < table.getStateCount(),
                    "Transition target " + targetState + " is out of bounds");
            }
        }
    }
    
    // Helper methods to create test grammars
    
    private Grammar createSimpleGrammar() {
        Grammar g = new Grammar();
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a", "a");
        
        g.setStartSymbol(S);
        g.addProduction(new Production(S, a));
        
        return g;
    }
    
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
    
    private Grammar createAmbiguousGrammar() {
        Grammar g = new Grammar();
        NonTerminal S = new NonTerminal("S");
        Terminal a = new Terminal("a", "a");
        Terminal b = new Terminal("b", "b");
        
        g.setStartSymbol(S);
        
        // Create a potentially ambiguous grammar
        // S -> S a | S b | a
        g.addProduction(new Production(S, S, a));
        g.addProduction(new Production(S, S, b));
        g.addProduction(new Production(S, a));
        
        return g;
    }
    
    private Grammar createGrammarRequiringMerging() {
        Grammar g = new Grammar();
        NonTerminal S = new NonTerminal("S");
        NonTerminal A = new NonTerminal("A");
        Terminal a = new Terminal("a", "a");
        Terminal b = new Terminal("b", "b");
        
        g.setStartSymbol(S);
        
        // Create a grammar that will generate states that can be merged
        // S -> A a | A b
        // A -> a
        g.addProduction(new Production(S, A, a));
        g.addProduction(new Production(S, A, b));
        g.addProduction(new Production(A, a));
        
        return g;
    }
}