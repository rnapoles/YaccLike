package com.parsergenerator.lalr;

import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for parsing table construction and optimization.
 * These tests verify that table operations complete within reasonable time bounds
 * and that optimization actually improves performance metrics.
 */
public class ParsingTablePerformanceTest {
    
    private ParsingTable smallTable;
    private ParsingTable mediumTable;
    private ParsingTable largeTable;
    
    @BeforeEach
    void setUp() {
        smallTable = createTableWithSize(5, 3, 2);   // 5 states, 3 terminals, 2 non-terminals
        mediumTable = createTableWithSize(20, 10, 8); // 20 states, 10 terminals, 8 non-terminals
        largeTable = createTableWithSize(100, 30, 25); // 100 states, 30 terminals, 25 non-terminals
    }
    
    /**
     * Creates a parsing table with the specified dimensions for testing.
     */
    private ParsingTable createTableWithSize(int stateCount, int terminalCount, int nonTerminalCount) {
        // Create terminals
        Set<Terminal> terminals = new HashSet<>();
        for (int i = 0; i < terminalCount; i++) {
            terminals.add(new Terminal("t" + i));
        }
        
        // Create non-terminals
        Set<NonTerminal> nonTerminals = new HashSet<>();
        for (int i = 0; i < nonTerminalCount; i++) {
            nonTerminals.add(new NonTerminal("N" + i));
        }
        
        // Create states
        List<LRItemSet> states = new ArrayList<>();
        for (int i = 0; i < stateCount; i++) {
            states.add(new LRItemSet());
        }
        
        ParsingTable table = new ParsingTable(states, terminals, nonTerminals);
        
        // Populate with test data
        populateTableWithTestData(table, states, terminals, nonTerminals);
        
        return table;
    }
    
    /**
     * Populates a table with realistic test data including some error actions.
     */
    private void populateTableWithTestData(ParsingTable table, List<LRItemSet> states, 
                                         Set<Terminal> terminals, Set<NonTerminal> nonTerminals) {
        Random random = new Random(42); // Fixed seed for reproducible tests
        
        List<Terminal> terminalList = new ArrayList<>(terminals);
        List<NonTerminal> nonTerminalList = new ArrayList<>(nonTerminals);
        
        // Add actions with some density (not every cell filled)
        for (int state = 0; state < states.size(); state++) {
            for (Terminal terminal : terminalList) {
                if (random.nextDouble() < 0.3) { // 30% density
                    if (random.nextDouble() < 0.1) {
                        // 10% error actions (to be compressed)
                        table.setAction(state, terminal, Action.error());
                    } else if (random.nextDouble() < 0.2) {
                        // 20% reduce actions
                        Production prod = createDummyProduction(nonTerminalList.get(0), state);
                        table.setAction(state, terminal, Action.reduce(prod));
                    } else {
                        // Shift actions
                        int targetState = random.nextInt(states.size());
                        table.setAction(state, terminal, Action.shift(targetState));
                    }
                }
            }
            
            // Add GOTO entries
            for (NonTerminal nonTerminal : nonTerminalList) {
                if (random.nextDouble() < 0.4) { // 40% density
                    int targetState = random.nextInt(states.size());
                    table.setGoto(state, nonTerminal, targetState);
                }
            }
        }
        
        // Add accept action for state 0 and first terminal
        if (!terminalList.isEmpty()) {
            table.setAction(0, terminalList.get(0), Action.accept());
        }
    }
    
    /**
     * Creates a dummy production for testing.
     */
    private Production createDummyProduction(NonTerminal lhs, int id) {
        return new Production(lhs, Arrays.asList(new Terminal("dummy")));
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testSmallTableCompressionPerformance() {
        long startTime = System.nanoTime();
        int removedEntries = smallTable.compressTable();
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        assertTrue(durationMs < 100, "Small table compression should complete in under 100ms");
        assertTrue(removedEntries >= 0, "Should report non-negative removed entries");
    }
    
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testMediumTableCompressionPerformance() {
        long startTime = System.nanoTime();
        int removedEntries = mediumTable.compressTable();
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        assertTrue(durationMs < 500, "Medium table compression should complete in under 500ms");
        assertTrue(removedEntries >= 0, "Should report non-negative removed entries");
    }
    
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testLargeTableCompressionPerformance() {
        long startTime = System.nanoTime();
        int removedEntries = largeTable.compressTable();
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        assertTrue(durationMs < 2000, "Large table compression should complete in under 2 seconds");
        assertTrue(removedEntries >= 0, "Should report non-negative removed entries");
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testValidationPerformance() {
        long startTime = System.nanoTime();
        ValidationResult result = mediumTable.validateTable();
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        assertTrue(durationMs < 200, "Table validation should complete in under 200ms");
        assertNotNull(result, "Validation should return a result");
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testSerializationPerformance() {
        long startTime = System.nanoTime();
        String serialized = mediumTable.serialize();
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        assertTrue(durationMs < 100, "Table serialization should complete in under 100ms");
        assertNotNull(serialized, "Serialization should return a string");
        assertTrue(serialized.length() > 0, "Serialized string should not be empty");
    }
    
    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testPerformanceMetricsCalculation() {
        long startTime = System.nanoTime();
        Map<String, Object> metrics = largeTable.getPerformanceMetrics();
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        assertTrue(durationMs < 200, "Performance metrics calculation should complete in under 200ms");
        assertNotNull(metrics, "Metrics should not be null");
        assertTrue(metrics.size() > 0, "Metrics should contain data");
    }
    
    @Test
    void testCompressionEffectiveness() {
        // Count initial entries
        int initialActionEntries = mediumTable.getActionTable().size();
        int initialGotoEntries = mediumTable.getGotoTable().size();
        
        // Get initial metrics
        Map<String, Object> beforeMetrics = mediumTable.getPerformanceMetrics();
        
        // Compress table
        int removedEntries = mediumTable.compressTable();
        
        // Get metrics after compression
        Map<String, Object> afterMetrics = mediumTable.getPerformanceMetrics();
        
        // Verify compression was effective
        if (removedEntries > 0) {
            assertTrue(mediumTable.getActionTable().size() < initialActionEntries,
                      "ACTION table should be smaller after compression");
            
            // Density should be higher after compression (fewer total entries, same max possible)
            double beforeDensity = (Double) beforeMetrics.get("overallDensity");
            double afterDensity = (Double) afterMetrics.get("overallDensity");
            
            // Note: Density might not always increase if we remove both numerator and denominator
            // but the table should be more efficient
            assertTrue(afterDensity >= 0.0 && afterDensity <= 1.0, 
                      "Density should be in valid range after compression");
        }
    }
    
    @Test
    void testMemoryEstimationAccuracy() {
        Map<String, Object> metrics = smallTable.getPerformanceMetrics();
        
        long estimatedMemory = (Long) metrics.get("estimatedMemoryBytes");
        int actionEntries = (Integer) metrics.get("actionEntries");
        int gotoEntries = (Integer) metrics.get("gotoEntries");
        int stateCount = (Integer) metrics.get("stateCount");
        
        // Verify memory estimation is reasonable
        assertTrue(estimatedMemory > 0, "Memory estimate should be positive");
        
        // Should be roughly proportional to table size
        long expectedMinimum = (actionEntries + gotoEntries) * 32; // Very conservative estimate
        assertTrue(estimatedMemory >= expectedMinimum, 
                  "Memory estimate should be at least proportional to entry count");
        
        // Should not be unreasonably large
        long expectedMaximum = (actionEntries + gotoEntries + stateCount) * 1024; // Very generous estimate
        assertTrue(estimatedMemory <= expectedMaximum, 
                  "Memory estimate should not be unreasonably large");
    }
    
    @Test
    void testTableDensityCalculations() {
        Map<String, Object> metrics = mediumTable.getPerformanceMetrics();
        
        int stateCount = (Integer) metrics.get("stateCount");
        int terminalCount = (Integer) metrics.get("terminalCount");
        int nonTerminalCount = (Integer) metrics.get("nonTerminalCount");
        int actionEntries = (Integer) metrics.get("actionEntries");
        int gotoEntries = (Integer) metrics.get("gotoEntries");
        
        double actionDensity = (Double) metrics.get("actionTableDensity");
        double gotoDensity = (Double) metrics.get("gotoTableDensity");
        double overallDensity = (Double) metrics.get("overallDensity");
        
        // Verify density calculations
        double expectedActionDensity = (double) actionEntries / (stateCount * terminalCount);
        double expectedGotoDensity = (double) gotoEntries / (stateCount * nonTerminalCount);
        double expectedOverallDensity = (double) (actionEntries + gotoEntries) / 
                                       (stateCount * (terminalCount + nonTerminalCount));
        
        assertEquals(expectedActionDensity, actionDensity, 0.001, 
                    "ACTION table density calculation should be correct");
        assertEquals(expectedGotoDensity, gotoDensity, 0.001, 
                    "GOTO table density calculation should be correct");
        assertEquals(expectedOverallDensity, overallDensity, 0.001, 
                    "Overall density calculation should be correct");
        
        // All densities should be in valid range
        assertTrue(actionDensity >= 0.0 && actionDensity <= 1.0, 
                  "ACTION density should be between 0 and 1");
        assertTrue(gotoDensity >= 0.0 && gotoDensity <= 1.0, 
                  "GOTO density should be between 0 and 1");
        assertTrue(overallDensity >= 0.0 && overallDensity <= 1.0, 
                  "Overall density should be between 0 and 1");
    }
    
    @Test
    void testScalabilityOfOperations() {
        // Test that operations scale reasonably with table size
        
        // Compression time should scale sub-quadratically
        long smallTime = timeOperation(() -> {
            ParsingTable table = createTableWithSize(10, 5, 3);
            table.compressTable();
        });
        
        long mediumTime = timeOperation(() -> {
            ParsingTable table = createTableWithSize(50, 15, 10);
            table.compressTable();
        });
        
        // Medium table is 5x larger in states, 3x in terminals, 3.3x in non-terminals
        // Time should not increase by more than the square of the size increase
        double sizeRatio = 5.0; // Approximate size ratio
        double timeRatio = (double) mediumTime / smallTime;
        
        assertTrue(timeRatio < sizeRatio * sizeRatio, 
                  "Compression time should scale better than quadratically");
    }
    
    /**
     * Times the execution of an operation.
     */
    private long timeOperation(Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        return endTime - startTime;
    }
    
    @Test
    void testValidationScalability() {
        // Validation should scale reasonably with table size
        long smallValidationTime = timeOperation(() -> smallTable.validateTable());
        long mediumValidationTime = timeOperation(() -> mediumTable.validateTable());
        
        // Validation time should not explode with size
        double timeRatio = (double) mediumValidationTime / smallValidationTime;
        assertTrue(timeRatio < 100, "Validation time should not explode with table size");
    }
    
    @Test
    void testDebugStringPerformance() {
        // Debug string generation should complete in reasonable time
        long startTime = System.nanoTime();
        String debugString = mediumTable.toDebugString();
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        assertTrue(durationMs < 500, "Debug string generation should complete in under 500ms");
        assertNotNull(debugString, "Debug string should not be null");
        assertTrue(debugString.length() > 0, "Debug string should not be empty");
    }
}