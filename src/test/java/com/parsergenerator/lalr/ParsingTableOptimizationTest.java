package com.parsergenerator.lalr;

import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for parsing table optimization functionality.
 */
public class ParsingTableOptimizationTest {
    
    private ParsingTable table;
    private Terminal terminalA;
    private Terminal terminalB;
    private Terminal eof;
    private NonTerminal nonTerminalS;
    private NonTerminal nonTerminalE;
    
    @BeforeEach
    void setUp() {
        // Create test symbols
        terminalA = new Terminal("a");
        terminalB = new Terminal("b");
        eof = new Terminal("$");
        nonTerminalS = new NonTerminal("S");
        nonTerminalE = new NonTerminal("E");
        
        Set<Terminal> terminals = new HashSet<>(Arrays.asList(terminalA, terminalB, eof));
        Set<NonTerminal> nonTerminals = new HashSet<>(Arrays.asList(nonTerminalS, nonTerminalE));
        
        // Create test states
        List<LRItemSet> states = createTestStates();
        
        table = new ParsingTable(states, terminals, nonTerminals);
        populateTestTable();
    }
    
    private List<LRItemSet> createTestStates() {
        List<LRItemSet> states = new ArrayList<>();
        
        // Create simple test states
        for (int i = 0; i < 5; i++) {
            LRItemSet state = new LRItemSet();
            // Add some dummy items - in real implementation these would be proper LR items
            states.add(state);
        }
        
        return states;
    }
    
    private void populateTestTable() {
        // Add some test actions including error actions
        table.setAction(0, terminalA, Action.shift(1));
        table.setAction(0, terminalB, Action.error()); // Error action to be compressed
        Production dummyProd = new Production(nonTerminalS, Arrays.asList(terminalA));
        table.setAction(1, terminalA, Action.reduce(dummyProd));
        table.setAction(1, eof, Action.accept());
        table.setAction(2, terminalB, Action.error()); // Another error action
        
        // Add some GOTO entries
        table.setGoto(0, nonTerminalS, 2);
        table.setGoto(1, nonTerminalE, 3);
        table.setGoto(2, nonTerminalE, 4); // This might be redundant depending on state structure
    }
    
    @Test
    void testCompressTable() {
        // Count initial entries
        int initialActionEntries = table.getActionTable().size();
        int initialGotoEntries = table.getGotoTable().size();
        
        // Compress the table
        int removedEntries = table.compressTable();
        
        // Verify that error actions were removed
        assertTrue(removedEntries > 0, "Should have removed some entries during compression");
        
        // Verify that the table still has the essential entries
        assertNotNull(table.getAction(0, terminalA), "Essential shift action should remain");
        assertNotNull(table.getAction(1, eof), "Accept action should remain");
        
        // Verify that error actions were removed
        assertNull(table.getAction(0, terminalB), "Error action should be removed");
        assertNull(table.getAction(2, terminalB), "Error action should be removed");
        
        // Verify final counts
        assertTrue(table.getActionTable().size() < initialActionEntries, 
                  "ACTION table should be smaller after compression");
    }
    
    @Test
    void testCompressTableWithNoRedundantEntries() {
        // Create a clean table with no error actions
        List<LRItemSet> cleanStates = new ArrayList<>();
        cleanStates.add(new LRItemSet()); // State 0
        cleanStates.add(new LRItemSet()); // State 1
        
        Set<Terminal> cleanTerminals = new HashSet<>(Arrays.asList(terminalA));
        Set<NonTerminal> cleanNonTerminals = new HashSet<>(Arrays.asList(nonTerminalS));
        
        ParsingTable cleanTable = new ParsingTable(cleanStates, cleanTerminals, cleanNonTerminals);
        
        // Add only essential actions
        cleanTable.setAction(0, terminalA, Action.shift(1));
        cleanTable.setGoto(0, nonTerminalS, 1);
        
        // Compress should remove nothing
        int removedEntries = cleanTable.compressTable();
        assertEquals(0, removedEntries, "No entries should be removed from clean table");
    }
    
    @Test
    void testValidateTableCompleteness() {
        ValidationResult result = table.validateTable();
        
        // The result should contain information about the table state
        assertNotNull(result, "Validation result should not be null");
        
        // Check that validation runs without throwing exceptions
        assertDoesNotThrow(() -> table.validateTable(), 
                          "Table validation should not throw exceptions");
    }
    
    @Test
    void testValidateEmptyTable() {
        ParsingTable emptyTable = new ParsingTable();
        ValidationResult result = emptyTable.validateTable();
        
        assertTrue(result.hasErrors(), "Empty table should have validation errors");
        assertTrue(result.getErrors().stream()
                        .anyMatch(error -> error.contains("no states")),
                  "Should report missing states error");
    }
    
    @Test
    void testValidateTableWithInvalidTransitions() {
        // Create a table with invalid state references
        ParsingTable invalidTable = new ParsingTable();
        invalidTable.addTerminal(terminalA);
        
        // Add action pointing to non-existent state
        invalidTable.setAction(0, terminalA, Action.shift(999));
        
        ValidationResult result = invalidTable.validateTable();
        
        assertTrue(result.hasErrors(), "Table with invalid transitions should have errors");
        assertTrue(result.getErrors().stream()
                        .anyMatch(error -> error.contains("invalid state")),
                  "Should report invalid state reference");
    }
    
    @Test
    void testSerializeTable() {
        String serialized = table.serialize();
        
        assertNotNull(serialized, "Serialized table should not be null");
        assertTrue(serialized.contains("LALR_TABLE_V1"), "Should contain version header");
        assertTrue(serialized.contains("STATES="), "Should contain state count");
        assertTrue(serialized.contains("TERMINALS:"), "Should contain terminals section");
        assertTrue(serialized.contains("NONTERMINALS:"), "Should contain non-terminals section");
        assertTrue(serialized.contains("ACTION:"), "Should contain ACTION section");
        assertTrue(serialized.contains("GOTO:"), "Should contain GOTO section");
        
        // Verify that terminal names appear in serialization
        assertTrue(serialized.contains(terminalA.getName()), "Should contain terminal 'a'");
        assertTrue(serialized.contains(nonTerminalS.getName()), "Should contain non-terminal 'S'");
    }
    
    @Test
    void testToDebugString() {
        String debugString = table.toDebugString();
        
        assertNotNull(debugString, "Debug string should not be null");
        assertTrue(debugString.contains("LALR PARSING TABLE DEBUG INFO"), "Should contain debug header");
        assertTrue(debugString.contains("Table Statistics:"), "Should contain statistics section");
        assertTrue(debugString.contains("Performance Metrics:"), "Should contain performance metrics");
        assertTrue(debugString.contains("State Details:"), "Should contain state details");
        assertTrue(debugString.contains("ACTION Table Summary:"), "Should contain ACTION table");
        assertTrue(debugString.contains("GOTO Table Summary:"), "Should contain GOTO table");
    }
    
    @Test
    void testGetPerformanceMetrics() {
        Map<String, Object> metrics = table.getPerformanceMetrics();
        
        assertNotNull(metrics, "Performance metrics should not be null");
        
        // Check that all expected metrics are present
        assertTrue(metrics.containsKey("stateCount"), "Should contain state count");
        assertTrue(metrics.containsKey("terminalCount"), "Should contain terminal count");
        assertTrue(metrics.containsKey("nonTerminalCount"), "Should contain non-terminal count");
        assertTrue(metrics.containsKey("actionEntries"), "Should contain action entries count");
        assertTrue(metrics.containsKey("gotoEntries"), "Should contain goto entries count");
        assertTrue(metrics.containsKey("actionTableDensity"), "Should contain action table density");
        assertTrue(metrics.containsKey("gotoTableDensity"), "Should contain goto table density");
        assertTrue(metrics.containsKey("overallDensity"), "Should contain overall density");
        assertTrue(metrics.containsKey("conflictCount"), "Should contain conflict count");
        assertTrue(metrics.containsKey("validationErrors"), "Should contain validation error count");
        assertTrue(metrics.containsKey("validationWarnings"), "Should contain validation warning count");
        assertTrue(metrics.containsKey("estimatedMemoryBytes"), "Should contain memory estimate");
        
        // Verify metric types and ranges
        assertTrue((Integer) metrics.get("stateCount") >= 0, "State count should be non-negative");
        assertTrue((Double) metrics.get("actionTableDensity") >= 0.0 && 
                  (Double) metrics.get("actionTableDensity") <= 1.0, 
                  "Action table density should be between 0 and 1");
        assertTrue((Double) metrics.get("gotoTableDensity") >= 0.0 && 
                  (Double) metrics.get("gotoTableDensity") <= 1.0, 
                  "GOTO table density should be between 0 and 1");
        assertTrue((Long) metrics.get("estimatedMemoryBytes") >= 0, 
                  "Memory estimate should be non-negative");
    }
    
    @Test
    void testTableDensityCalculation() {
        Map<String, Object> metrics = table.getPerformanceMetrics();
        
        int stateCount = (Integer) metrics.get("stateCount");
        int terminalCount = (Integer) metrics.get("terminalCount");
        int nonTerminalCount = (Integer) metrics.get("nonTerminalCount");
        int actionEntries = (Integer) metrics.get("actionEntries");
        int gotoEntries = (Integer) metrics.get("gotoEntries");
        
        // Calculate expected densities
        double expectedActionDensity = stateCount > 0 && terminalCount > 0 ? 
                                     (double) actionEntries / (stateCount * terminalCount) : 0.0;
        double expectedGotoDensity = stateCount > 0 && nonTerminalCount > 0 ? 
                                   (double) gotoEntries / (stateCount * nonTerminalCount) : 0.0;
        
        assertEquals(expectedActionDensity, (Double) metrics.get("actionTableDensity"), 0.001,
                    "Action table density calculation should be correct");
        assertEquals(expectedGotoDensity, (Double) metrics.get("gotoTableDensity"), 0.001,
                    "GOTO table density calculation should be correct");
    }
    
    @Test
    void testValidationResultIntegration() {
        ValidationResult result = table.validateTable();
        
        // Test that ValidationResult methods work correctly
        assertNotNull(result.getErrors(), "Errors list should not be null");
        assertNotNull(result.getWarnings(), "Warnings list should not be null");
        
        // Test formatted report
        String report = result.getFormattedReport();
        assertNotNull(report, "Formatted report should not be null");
        
        // Test toString
        String toString = result.toString();
        assertNotNull(toString, "toString should not be null");
        assertTrue(toString.contains("ValidationResult"), "toString should contain class name");
    }
    
    @Test
    void testTableCompressionPreservesEssentialEntries() {
        // Record essential entries before compression
        Action shiftAction = table.getAction(0, terminalA);
        Action acceptAction = table.getAction(1, eof);
        Integer gotoEntry = table.getGoto(0, nonTerminalS);
        
        // Compress table
        table.compressTable();
        
        // Verify essential entries are preserved
        assertEquals(shiftAction, table.getAction(0, terminalA), 
                    "Essential shift action should be preserved");
        assertEquals(acceptAction, table.getAction(1, eof), 
                    "Accept action should be preserved");
        assertEquals(gotoEntry, table.getGoto(0, nonTerminalS), 
                    "Essential GOTO entry should be preserved");
    }
}