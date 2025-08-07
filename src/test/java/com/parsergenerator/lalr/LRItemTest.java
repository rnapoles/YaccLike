package com.parsergenerator.lalr;

import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LRItemTest {
    
    private NonTerminal A, B, C;
    private Terminal a, b, c;
    private Production production1, production2, epsilonProduction;
    
    @BeforeEach
    void setUp() {
        // Reset production ID counter for consistent test results
        Production.resetIdCounter();
        
        // Create test symbols
        A = new NonTerminal("A");
        B = new NonTerminal("B");
        C = new NonTerminal("C");
        a = new Terminal("a");
        b = new Terminal("b");
        c = new Terminal("c");
        
        // Create test productions
        // A ::= B C a
        production1 = new Production(A, Arrays.asList(B, C, a));
        // B ::= b
        production2 = new Production(B, Arrays.asList(b));
        // C ::= ε (epsilon production)
        epsilonProduction = new Production(C);
    }
    
    @Test
    void testConstructorWithProductionAndDotPosition() {
        LRItem item = new LRItem(production1, 1);
        
        assertEquals(production1, item.getProduction());
        assertEquals(1, item.getDotPosition());
        assertTrue(item.getLookaheads().isEmpty());
    }
    
    @Test
    void testConstructorWithLookaheads() {
        Set<Terminal> lookaheads = new HashSet<>(Arrays.asList(a, b));
        LRItem item = new LRItem(production1, 2, lookaheads);
        
        assertEquals(production1, item.getProduction());
        assertEquals(2, item.getDotPosition());
        assertEquals(2, item.getLookaheads().size());
        assertTrue(item.getLookaheads().contains(a));
        assertTrue(item.getLookaheads().contains(b));
    }
    
    @Test
    void testConstructorValidation() {
        // Null production
        assertThrows(IllegalArgumentException.class, () -> new LRItem(null, 0));
        
        // Invalid dot position - negative
        assertThrows(IllegalArgumentException.class, () -> new LRItem(production1, -1));
        
        // Invalid dot position - too large
        assertThrows(IllegalArgumentException.class, () -> new LRItem(production1, 4));
        
        // Null lookaheads
        assertThrows(IllegalArgumentException.class, () -> new LRItem(production1, 0, null));
    }
    
    @Test
    void testGetNextSymbol() {
        // A ::= • B C a (dot at position 0)
        LRItem item1 = new LRItem(production1, 0);
        assertEquals(B, item1.getNextSymbol());
        
        // A ::= B • C a (dot at position 1)
        LRItem item2 = new LRItem(production1, 1);
        assertEquals(C, item2.getNextSymbol());
        
        // A ::= B C • a (dot at position 2)
        LRItem item3 = new LRItem(production1, 2);
        assertEquals(a, item3.getNextSymbol());
        
        // A ::= B C a • (dot at end)
        LRItem item4 = new LRItem(production1, 3);
        assertNull(item4.getNextSymbol());
    }
    
    @Test
    void testIsComplete() {
        // Not complete - dot not at end
        LRItem item1 = new LRItem(production1, 0);
        assertFalse(item1.isComplete());
        
        LRItem item2 = new LRItem(production1, 2);
        assertFalse(item2.isComplete());
        
        // Complete - dot at end
        LRItem item3 = new LRItem(production1, 3);
        assertTrue(item3.isComplete());
        
        // Epsilon production is complete at position 0
        LRItem epsilonItem = new LRItem(epsilonProduction, 0);
        assertTrue(epsilonItem.isComplete());
    }
    
    @Test
    void testCanAdvance() {
        // Can advance - dot not at end
        LRItem item1 = new LRItem(production1, 0);
        assertTrue(item1.canAdvance());
        
        LRItem item2 = new LRItem(production1, 2);
        assertTrue(item2.canAdvance());
        
        // Cannot advance - dot at end
        LRItem item3 = new LRItem(production1, 3);
        assertFalse(item3.canAdvance());
        
        // Epsilon production cannot advance
        LRItem epsilonItem = new LRItem(epsilonProduction, 0);
        assertFalse(epsilonItem.canAdvance());
    }
    
    @Test
    void testAdvance() {
        // A ::= • B C a -> A ::= B • C a
        LRItem item1 = new LRItem(production1, 0);
        LRItem advanced1 = item1.advance();
        
        assertEquals(production1, advanced1.getProduction());
        assertEquals(1, advanced1.getDotPosition());
        assertEquals(C, advanced1.getNextSymbol());
        
        // A ::= B C • a -> A ::= B C a •
        LRItem item2 = new LRItem(production1, 2);
        LRItem advanced2 = item2.advance();
        
        assertEquals(production1, advanced2.getProduction());
        assertEquals(3, advanced2.getDotPosition());
        assertTrue(advanced2.isComplete());
    }
    
    @Test
    void testAdvanceWithLookaheads() {
        Set<Terminal> originalLookaheads = new HashSet<>(Arrays.asList(a, b));
        LRItem item = new LRItem(production1, 0, originalLookaheads);
        
        // Advance should copy lookaheads
        LRItem advanced = item.advance();
        assertEquals(2, advanced.getLookaheads().size());
        assertTrue(advanced.getLookaheads().contains(a));
        assertTrue(advanced.getLookaheads().contains(b));
        
        // Advance with new lookaheads
        Set<Terminal> newLookaheads = new HashSet<>(Arrays.asList(c));
        LRItem advancedWithNew = item.advance(newLookaheads);
        assertEquals(1, advancedWithNew.getLookaheads().size());
        assertTrue(advancedWithNew.getLookaheads().contains(c));
    }
    
    @Test
    void testAdvanceValidation() {
        // Cannot advance complete item
        LRItem completeItem = new LRItem(production1, 3);
        assertThrows(IllegalStateException.class, completeItem::advance);
        
        // Cannot advance with null lookaheads
        LRItem item = new LRItem(production1, 0);
        assertThrows(IllegalArgumentException.class, () -> item.advance(null));
    }
    
    @Test
    void testLookaheadManagement() {
        LRItem item = new LRItem(production1, 0);
        
        // Initially empty
        assertTrue(item.getLookaheads().isEmpty());
        
        // Add single lookahead
        item.addLookahead(a);
        assertEquals(1, item.getLookaheads().size());
        assertTrue(item.getLookaheads().contains(a));
        
        // Add multiple lookaheads
        Set<Terminal> newLookaheads = new HashSet<>(Arrays.asList(b, c));
        item.addLookaheads(newLookaheads);
        assertEquals(3, item.getLookaheads().size());
        assertTrue(item.getLookaheads().contains(a));
        assertTrue(item.getLookaheads().contains(b));
        assertTrue(item.getLookaheads().contains(c));
        
        // Adding duplicate should not increase size
        item.addLookahead(a);
        assertEquals(3, item.getLookaheads().size());
    }
    
    @Test
    void testLookaheadValidation() {
        LRItem item = new LRItem(production1, 0);
        
        // Null lookahead
        assertThrows(IllegalArgumentException.class, () -> item.addLookahead(null));
        
        // Null lookaheads set
        assertThrows(IllegalArgumentException.class, () -> item.addLookaheads(null));
    }
    
    @Test
    void testGetCore() {
        Set<Terminal> lookaheads = new HashSet<>(Arrays.asList(a, b));
        LRItem item = new LRItem(production1, 1, lookaheads);
        
        LRItem core = item.getCore();
        assertEquals(production1, core.getProduction());
        assertEquals(1, core.getDotPosition());
        assertTrue(core.getLookaheads().isEmpty());
    }
    
    @Test
    void testHasSameCore() {
        LRItem item1 = new LRItem(production1, 1, new HashSet<>(Arrays.asList(a)));
        LRItem item2 = new LRItem(production1, 1, new HashSet<>(Arrays.asList(b)));
        LRItem item3 = new LRItem(production1, 2, new HashSet<>(Arrays.asList(a)));
        LRItem item4 = new LRItem(production2, 1, new HashSet<>(Arrays.asList(a)));
        
        // Same production and dot position, different lookaheads
        assertTrue(item1.hasSameCore(item2));
        
        // Same production, different dot position
        assertFalse(item1.hasSameCore(item3));
        
        // Different production, same dot position
        assertFalse(item1.hasSameCore(item4));
        
        // Null comparison
        assertFalse(item1.hasSameCore(null));
    }
    
    @Test
    void testEquals() {
        Set<Terminal> lookaheads1 = new HashSet<>(Arrays.asList(a, b));
        Set<Terminal> lookaheads2 = new HashSet<>(Arrays.asList(a, b));
        Set<Terminal> lookaheads3 = new HashSet<>(Arrays.asList(a, c));
        
        LRItem item1 = new LRItem(production1, 1, lookaheads1);
        LRItem item2 = new LRItem(production1, 1, lookaheads2);
        LRItem item3 = new LRItem(production1, 1, lookaheads3);
        LRItem item4 = new LRItem(production1, 2, lookaheads1);
        LRItem item5 = new LRItem(production2, 1, lookaheads1);
        
        // Same production, dot position, and lookaheads
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
        
        // Different lookaheads
        assertNotEquals(item1, item3);
        
        // Different dot position
        assertNotEquals(item1, item4);
        
        // Different production
        assertNotEquals(item1, item5);
        
        // Self equality
        assertEquals(item1, item1);
        
        // Null comparison
        assertNotEquals(item1, null);
        
        // Different class
        assertNotEquals(item1, "not an LRItem");
    }
    
    @Test
    void testToString() {
        // A ::= • B C a
        LRItem item1 = new LRItem(production1, 0);
        String str1 = item1.toString();
        assertTrue(str1.contains("A ::="));
        assertTrue(str1.contains("•"));
        assertTrue(str1.contains("B"));
        assertTrue(str1.contains("C"));
        assertTrue(str1.contains("a"));
        
        // A ::= B • C a
        LRItem item2 = new LRItem(production1, 1);
        String str2 = item2.toString();
        assertTrue(str2.contains("A ::="));
        assertTrue(str2.contains("B"));
        assertTrue(str2.contains("•"));
        assertTrue(str2.contains("C"));
        assertTrue(str2.contains("a"));
        
        // A ::= B C a •
        LRItem item3 = new LRItem(production1, 3);
        String str3 = item3.toString();
        assertTrue(str3.contains("A ::="));
        assertTrue(str3.contains("B"));
        assertTrue(str3.contains("C"));
        assertTrue(str3.contains("a"));
        assertTrue(str3.contains("•"));
        
        // With lookaheads
        Set<Terminal> lookaheads = new HashSet<>(Arrays.asList(a, b));
        LRItem item4 = new LRItem(production1, 1, lookaheads);
        String str4 = item4.toString();
        assertTrue(str4.contains("{"));
        assertTrue(str4.contains("a"));
        assertTrue(str4.contains("b"));
        
        // Epsilon production
        LRItem epsilonItem = new LRItem(epsilonProduction, 0);
        String epsilonStr = epsilonItem.toString();
        assertTrue(epsilonStr.contains("C ::="));
        assertTrue(epsilonStr.contains("•"));
    }
    
    @Test
    void testToCoreString() {
        Set<Terminal> lookaheads = new HashSet<>(Arrays.asList(a, b));
        LRItem item = new LRItem(production1, 1, lookaheads);
        
        String coreStr = item.toCoreString();
        String fullStr = item.toString();
        
        // Core string should not contain lookaheads
        assertFalse(coreStr.contains("{"));
        assertTrue(fullStr.contains("{"));
        
        // But should contain the production and dot
        assertTrue(coreStr.contains("A ::= B •"));
    }
    
    @Test
    void testLookaheadsImmutability() {
        Set<Terminal> originalLookaheads = new HashSet<>(Arrays.asList(a));
        LRItem item = new LRItem(production1, 0, originalLookaheads);
        
        // Modifying original set should not affect item
        originalLookaheads.add(b);
        assertEquals(1, item.getLookaheads().size());
        
        // Returned set should be unmodifiable
        Set<Terminal> returnedLookaheads = item.getLookaheads();
        assertThrows(UnsupportedOperationException.class, () -> returnedLookaheads.add(c));
    }
    
    @Test
    void testEpsilonProductionHandling() {
        // C ::= ε
        LRItem epsilonItem = new LRItem(epsilonProduction, 0);
        
        assertTrue(epsilonItem.isComplete());
        assertFalse(epsilonItem.canAdvance());
        assertNull(epsilonItem.getNextSymbol());
        assertThrows(IllegalStateException.class, epsilonItem::advance);
        
        String str = epsilonItem.toString();
        assertTrue(str.contains("C ::= •"));
    }
}