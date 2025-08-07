package com.parsergenerator.lalr;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LRItemSetTest {
    
    private NonTerminal S, A, B, C;
    private Terminal a, b, c, d, dollar;
    private Production prod1, prod2, prod3, prod4, prod5, epsilonProd;
    private Grammar grammar;
    
    @BeforeEach
    void setUp() {
        // Reset counters for consistent test results
        Production.resetIdCounter();
        LRItemSet.resetIdCounter();
        
        // Create test symbols
        S = new NonTerminal("S");
        A = new NonTerminal("A");
        B = new NonTerminal("B");
        C = new NonTerminal("C");
        a = new Terminal("a");
        b = new Terminal("b");
        c = new Terminal("c");
        d = new Terminal("d");
        dollar = new Terminal("$");
        
        // Create test productions for a simple grammar
        // S ::= A B
        prod1 = new Production(S, Arrays.asList(A, B));
        // A ::= a A
        prod2 = new Production(A, Arrays.asList(a, A));
        // A ::= c
        prod3 = new Production(A, Arrays.asList(c));
        // B ::= b B
        prod4 = new Production(B, Arrays.asList(b, B));
        // B ::= d
        prod5 = new Production(B, Arrays.asList(d));
        // C ::= ε (epsilon production)
        epsilonProd = new Production(C);
        
        // Create grammar
        grammar = new Grammar(S);
        grammar.addProduction(prod1);
        grammar.addProduction(prod2);
        grammar.addProduction(prod3);
        grammar.addProduction(prod4);
        grammar.addProduction(prod5);
        grammar.addProduction(epsilonProd);
        
        // Compute FIRST/FOLLOW sets
        grammar.computeFirstFollowSets();
    }
    
    @Test
    void testDefaultConstructor() {
        LRItemSet itemSet = new LRItemSet();
        
        assertTrue(itemSet.isEmpty());
        assertEquals(0, itemSet.size());
        assertEquals(0, itemSet.getId());
    }
    
    @Test
    void testConstructorWithItems() {
        LRItem item1 = new LRItem(prod1, 0);
        LRItem item2 = new LRItem(prod2, 1);
        Collection<LRItem> items = Arrays.asList(item1, item2);
        
        LRItemSet itemSet = new LRItemSet(items);
        
        assertEquals(2, itemSet.size());
        assertTrue(itemSet.containsItem(item1));
        assertTrue(itemSet.containsItem(item2));
    }
    
    @Test
    void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new LRItemSet(null));
    }
    
    @Test
    void testAddItem() {
        LRItemSet itemSet = new LRItemSet();
        LRItem item = new LRItem(prod1, 0);
        
        assertTrue(itemSet.addItem(item));
        assertEquals(1, itemSet.size());
        assertTrue(itemSet.containsItem(item));
        
        // Adding same item again should return false
        assertFalse(itemSet.addItem(item));
        assertEquals(1, itemSet.size());
    }
    
    @Test
    void testAddItemValidation() {
        LRItemSet itemSet = new LRItemSet();
        assertThrows(IllegalArgumentException.class, () -> itemSet.addItem(null));
    }
    
    @Test
    void testAddItems() {
        LRItemSet itemSet = new LRItemSet();
        LRItem item1 = new LRItem(prod1, 0);
        LRItem item2 = new LRItem(prod2, 1);
        Collection<LRItem> items = Arrays.asList(item1, item2);
        
        assertTrue(itemSet.addItems(items));
        assertEquals(2, itemSet.size());
        assertTrue(itemSet.containsItem(item1));
        assertTrue(itemSet.containsItem(item2));
        
        // Adding same items again should return false
        assertFalse(itemSet.addItems(items));
        assertEquals(2, itemSet.size());
    }
    
    @Test
    void testAddItemsValidation() {
        LRItemSet itemSet = new LRItemSet();
        assertThrows(IllegalArgumentException.class, () -> itemSet.addItems(null));
    }
    
    @Test
    void testRemoveItem() {
        LRItem item1 = new LRItem(prod1, 0);
        LRItem item2 = new LRItem(prod2, 1);
        LRItemSet itemSet = new LRItemSet(Arrays.asList(item1, item2));
        
        assertTrue(itemSet.removeItem(item1));
        assertEquals(1, itemSet.size());
        assertFalse(itemSet.containsItem(item1));
        assertTrue(itemSet.containsItem(item2));
        
        // Removing non-existent item should return false
        assertFalse(itemSet.removeItem(item1));
        assertEquals(1, itemSet.size());
    }
    
    @Test
    void testClear() {
        LRItem item1 = new LRItem(prod1, 0);
        LRItem item2 = new LRItem(prod2, 1);
        LRItemSet itemSet = new LRItemSet(Arrays.asList(item1, item2));
        
        assertEquals(2, itemSet.size());
        itemSet.clear();
        assertEquals(0, itemSet.size());
        assertTrue(itemSet.isEmpty());
    }
    
    @Test
    void testGetItems() {
        LRItem item1 = new LRItem(prod1, 0);
        LRItem item2 = new LRItem(prod2, 1);
        LRItemSet itemSet = new LRItemSet(Arrays.asList(item1, item2));
        
        Set<LRItem> items = itemSet.getItems();
        assertEquals(2, items.size());
        assertTrue(items.contains(item1));
        assertTrue(items.contains(item2));
        
        // Returned set should be unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> items.add(new LRItem(prod3, 0)));
    }
    
    @Test
    void testClosure() {
        // Create initial item: S ::= • A B
        LRItem initialItem = new LRItem(prod1, 0, new HashSet<>(Arrays.asList(dollar)));
        LRItemSet initialSet = new LRItemSet(Arrays.asList(initialItem));
        
        // Compute closure
        LRItemSet closureSet = initialSet.closure(grammar);
        
        // Should contain original item
        assertTrue(closureSet.containsItem(initialItem));
        
        // Should contain items for A productions since A is after dot
        boolean hasAProd2 = false, hasAProd3 = false;
        for (LRItem item : closureSet.getItems()) {
            if (item.getProduction().equals(prod2) && item.getDotPosition() == 0) {
                hasAProd2 = true;
            }
            if (item.getProduction().equals(prod3) && item.getDotPosition() == 0) {
                hasAProd3 = true;
            }
        }
        assertTrue(hasAProd2, "Should contain A ::= • a A");
        assertTrue(hasAProd3, "Should contain A ::= • c");
        
        // Size should be at least 3 (original + 2 A productions)
        assertTrue(closureSet.size() >= 3);
    }
    
    @Test
    void testClosureValidation() {
        LRItemSet itemSet = new LRItemSet();
        assertThrows(IllegalArgumentException.class, () -> itemSet.closure(null));
    }
    
    @Test
    void testClosureWithEpsilonProduction() {
        // Create a grammar with epsilon production
        NonTerminal X = new NonTerminal("X");
        Production xProd = new Production(X, Arrays.asList(C, a));
        Grammar epsilonGrammar = new Grammar(X);
        epsilonGrammar.addProduction(xProd);
        epsilonGrammar.addProduction(epsilonProd);
        epsilonGrammar.computeFirstFollowSets();
        
        // Create initial item: X ::= • C a
        LRItem initialItem = new LRItem(xProd, 0, new HashSet<>(Arrays.asList(dollar)));
        LRItemSet initialSet = new LRItemSet(Arrays.asList(initialItem));
        
        // Compute closure
        LRItemSet closureSet = initialSet.closure(epsilonGrammar);
        
        // Should contain original item
        assertTrue(closureSet.containsItem(initialItem));
        
        // Should contain epsilon production item: C ::= •
        boolean hasEpsilonItem = false;
        for (LRItem item : closureSet.getItems()) {
            if (item.getProduction().equals(epsilonProd) && item.getDotPosition() == 0) {
                hasEpsilonItem = true;
                break;
            }
        }
        assertTrue(hasEpsilonItem, "Should contain C ::= •");
    }
    
    @Test
    void testGotoSet() {
        // Create initial item set: {S ::= • A B}
        LRItem initialItem = new LRItem(prod1, 0, new HashSet<>(Arrays.asList(dollar)));
        LRItemSet initialSet = new LRItemSet(Arrays.asList(initialItem));
        LRItemSet closureSet = initialSet.closure(grammar);
        
        // Compute GOTO(I0, A)
        LRItemSet gotoA = closureSet.gotoSet(A, grammar);
        
        // Should contain S ::= A • B
        boolean hasAdvancedItem = false;
        for (LRItem item : gotoA.getItems()) {
            if (item.getProduction().equals(prod1) && item.getDotPosition() == 1) {
                hasAdvancedItem = true;
                break;
            }
        }
        assertTrue(hasAdvancedItem, "Should contain S ::= A • B");
        
        // Should also contain closure items for B
        boolean hasBProd4 = false, hasBProd5 = false;
        for (LRItem item : gotoA.getItems()) {
            if (item.getProduction().equals(prod4) && item.getDotPosition() == 0) {
                hasBProd4 = true;
            }
            if (item.getProduction().equals(prod5) && item.getDotPosition() == 0) {
                hasBProd5 = true;
            }
        }
        assertTrue(hasBProd4, "Should contain B ::= • b B");
        assertTrue(hasBProd5, "Should contain B ::= • d");
    }
    
    @Test
    void testGotoSetValidation() {
        LRItemSet itemSet = new LRItemSet();
        assertThrows(IllegalArgumentException.class, () -> itemSet.gotoSet(null, grammar));
        assertThrows(IllegalArgumentException.class, () -> itemSet.gotoSet(A, null));
    }
    
    @Test
    void testGotoSetWithNoMatchingItems() {
        // Create item set with no items that can advance past 'a'
        LRItem item = new LRItem(prod1, 2); // S ::= A B •
        LRItemSet itemSet = new LRItemSet(Arrays.asList(item));
        
        LRItemSet gotoSet = itemSet.gotoSet(a, grammar);
        assertTrue(gotoSet.isEmpty());
    }
    
    @Test
    void testGetGotoSymbols() {
        // Create item set with various items
        LRItem item1 = new LRItem(prod1, 0); // S ::= • A B
        LRItem item2 = new LRItem(prod2, 0); // A ::= • a A
        LRItem item3 = new LRItem(prod3, 0); // A ::= • c
        LRItem item4 = new LRItem(prod1, 2); // S ::= A B • (complete)
        
        LRItemSet itemSet = new LRItemSet(Arrays.asList(item1, item2, item3, item4));
        Set<Symbol> gotoSymbols = itemSet.getGotoSymbols();
        
        assertEquals(3, gotoSymbols.size());
        assertTrue(gotoSymbols.contains(A));
        assertTrue(gotoSymbols.contains(a));
        assertTrue(gotoSymbols.contains(c));
        assertFalse(gotoSymbols.contains(B)); // item4 is complete, so B is not a goto symbol
    }
    
    @Test
    void testGetCompleteItems() {
        // Create item set with mix of complete and incomplete items
        LRItem item1 = new LRItem(prod1, 0); // S ::= • A B (incomplete)
        LRItem item2 = new LRItem(prod1, 2); // S ::= A B • (complete)
        LRItem item3 = new LRItem(prod3, 1); // A ::= c • (complete)
        LRItem item4 = new LRItem(epsilonProd, 0); // C ::= • (complete)
        
        LRItemSet itemSet = new LRItemSet(Arrays.asList(item1, item2, item3, item4));
        Set<LRItem> completeItems = itemSet.getCompleteItems();
        
        assertEquals(3, completeItems.size());
        assertTrue(completeItems.contains(item2));
        assertTrue(completeItems.contains(item3));
        assertTrue(completeItems.contains(item4));
        assertFalse(completeItems.contains(item1));
    }
    
    @Test
    void testGetItemsForSymbol() {
        // Create item set with various items
        LRItem item1 = new LRItem(prod1, 0); // S ::= • A B
        LRItem item2 = new LRItem(prod2, 0); // A ::= • a A
        LRItem item3 = new LRItem(prod2, 1); // A ::= a • A
        LRItem item4 = new LRItem(prod3, 0); // A ::= • c
        
        LRItemSet itemSet = new LRItemSet(Arrays.asList(item1, item2, item3, item4));
        
        // Get items for symbol A
        Set<LRItem> itemsForA = itemSet.getItemsForSymbol(A);
        assertEquals(2, itemsForA.size());
        assertTrue(itemsForA.contains(item1)); // S ::= • A B
        assertTrue(itemsForA.contains(item3)); // A ::= a • A
        
        // Get items for symbol a
        Set<LRItem> itemsForA_terminal = itemSet.getItemsForSymbol(a);
        assertEquals(1, itemsForA_terminal.size());
        assertTrue(itemsForA_terminal.contains(item2)); // A ::= • a A
        
        // Get items for symbol not present
        Set<LRItem> itemsForB = itemSet.getItemsForSymbol(B);
        assertEquals(0, itemsForB.size());
    }
    
    @Test
    void testGetCore() {
        // Create items with lookaheads
        Set<Terminal> lookaheads1 = new HashSet<>(Arrays.asList(a, b));
        Set<Terminal> lookaheads2 = new HashSet<>(Arrays.asList(c, d));
        
        LRItem item1 = new LRItem(prod1, 0, lookaheads1);
        LRItem item2 = new LRItem(prod2, 1, lookaheads2);
        
        LRItemSet itemSet = new LRItemSet(Arrays.asList(item1, item2));
        LRItemSet core = itemSet.getCore();
        
        assertEquals(2, core.size());
        
        // Core items should have no lookaheads
        for (LRItem coreItem : core.getItems()) {
            assertTrue(coreItem.getLookaheads().isEmpty());
        }
        
        // But should have same productions and dot positions
        boolean hasItem1Core = false, hasItem2Core = false;
        for (LRItem coreItem : core.getItems()) {
            if (coreItem.getProduction().equals(prod1) && coreItem.getDotPosition() == 0) {
                hasItem1Core = true;
            }
            if (coreItem.getProduction().equals(prod2) && coreItem.getDotPosition() == 1) {
                hasItem2Core = true;
            }
        }
        assertTrue(hasItem1Core);
        assertTrue(hasItem2Core);
    }
    
    @Test
    void testHasSameCore() {
        // Create two item sets with same cores but different lookaheads
        LRItem item1a = new LRItem(prod1, 0, new HashSet<>(Arrays.asList(a)));
        LRItem item2a = new LRItem(prod2, 1, new HashSet<>(Arrays.asList(b)));
        LRItemSet set1 = new LRItemSet(Arrays.asList(item1a, item2a));
        
        LRItem item1b = new LRItem(prod1, 0, new HashSet<>(Arrays.asList(c)));
        LRItem item2b = new LRItem(prod2, 1, new HashSet<>(Arrays.asList(d)));
        LRItemSet set2 = new LRItemSet(Arrays.asList(item1b, item2b));
        
        // Should have same core
        assertTrue(set1.hasSameCore(set2));
        assertTrue(set2.hasSameCore(set1));
        
        // Create set with different core
        LRItem item3 = new LRItem(prod1, 1, new HashSet<>(Arrays.asList(a)));
        LRItemSet set3 = new LRItemSet(Arrays.asList(item3, item2a));
        
        assertFalse(set1.hasSameCore(set3));
        assertFalse(set1.hasSameCore(null));
    }
    
    @Test
    void testMergeLookaheads() {
        // Create two item sets with same cores but different lookaheads
        LRItem item1a = new LRItem(prod1, 0, new HashSet<>(Arrays.asList(a)));
        LRItem item2a = new LRItem(prod2, 1, new HashSet<>(Arrays.asList(b)));
        LRItemSet set1 = new LRItemSet(Arrays.asList(item1a, item2a));
        
        LRItem item1b = new LRItem(prod1, 0, new HashSet<>(Arrays.asList(c)));
        LRItem item2b = new LRItem(prod2, 1, new HashSet<>(Arrays.asList(d)));
        LRItemSet set2 = new LRItemSet(Arrays.asList(item1b, item2b));
        
        // Merge lookaheads
        assertTrue(set1.mergeLookaheads(set2));
        
        // Check that lookaheads were merged
        for (LRItem item : set1.getItems()) {
            if (item.getProduction().equals(prod1) && item.getDotPosition() == 0) {
                assertEquals(2, item.getLookaheads().size());
                assertTrue(item.getLookaheads().contains(a));
                assertTrue(item.getLookaheads().contains(c));
            }
            if (item.getProduction().equals(prod2) && item.getDotPosition() == 1) {
                assertEquals(2, item.getLookaheads().size());
                assertTrue(item.getLookaheads().contains(b));
                assertTrue(item.getLookaheads().contains(d));
            }
        }
        
        // Merging again should return false (no changes)
        assertFalse(set1.mergeLookaheads(set2));
    }
    
    @Test
    void testMergeLookaheadsValidation() {
        LRItemSet set1 = new LRItemSet();
        LRItemSet set2 = new LRItemSet();
        
        // Null set
        assertThrows(IllegalArgumentException.class, () -> set1.mergeLookaheads(null));
        
        // Different cores
        set1.addItem(new LRItem(prod1, 0));
        set2.addItem(new LRItem(prod1, 1));
        assertThrows(IllegalArgumentException.class, () -> set1.mergeLookaheads(set2));
    }
    
    @Test
    void testEquals() {
        LRItem item1 = new LRItem(prod1, 0, new HashSet<>(Arrays.asList(a)));
        LRItem item2 = new LRItem(prod2, 1, new HashSet<>(Arrays.asList(b)));
        
        LRItemSet set1 = new LRItemSet(Arrays.asList(item1, item2));
        LRItemSet set2 = new LRItemSet(Arrays.asList(item1, item2));
        LRItemSet set3 = new LRItemSet(Arrays.asList(item1));
        
        // Same items
        assertEquals(set1, set2);
        assertEquals(set1.hashCode(), set2.hashCode());
        
        // Different items
        assertNotEquals(set1, set3);
        
        // Self equality
        assertEquals(set1, set1);
        
        // Null comparison
        assertNotEquals(set1, null);
        
        // Different class
        assertNotEquals(set1, "not an LRItemSet");
    }
    
    @Test
    void testToString() {
        LRItem item1 = new LRItem(prod1, 0);
        LRItem item2 = new LRItem(prod2, 1);
        LRItemSet itemSet = new LRItemSet(Arrays.asList(item1, item2));
        
        String str = itemSet.toString();
        assertTrue(str.contains("I" + itemSet.getId()));
        assertTrue(str.contains("S ::="));
        assertTrue(str.contains("A ::="));
        assertTrue(str.contains("•"));
    }
    
    @Test
    void testToDetailedString() {
        LRItem item1 = new LRItem(prod1, 0);
        LRItem item2 = new LRItem(prod2, 1);
        LRItemSet itemSet = new LRItemSet(Arrays.asList(item1, item2));
        
        String detailedStr = itemSet.toDetailedString();
        assertTrue(detailedStr.contains("LRItemSet"));
        assertTrue(detailedStr.contains("[" + itemSet.getId() + "]"));
        assertTrue(detailedStr.contains("2 items"));
        assertTrue(detailedStr.contains("S ::="));
        assertTrue(detailedStr.contains("A ::="));
    }
    
    @Test
    void testIdGeneration() {
        LRItemSet.resetIdCounter();
        
        LRItemSet set1 = new LRItemSet();
        LRItemSet set2 = new LRItemSet();
        LRItemSet set3 = new LRItemSet();
        
        assertEquals(0, set1.getId());
        assertEquals(1, set2.getId());
        assertEquals(2, set3.getId());
    }
    
    @Test
    void testComplexClosureScenario() {
        // Create a more complex grammar to test closure computation
        // S' ::= S
        // S ::= A a | b
        // A ::= B C | c
        // B ::= d | ε
        // C ::= e
        
        NonTerminal Sp = new NonTerminal("S'");
        NonTerminal S_nt = new NonTerminal("S");
        NonTerminal A_nt = new NonTerminal("A");
        NonTerminal B_nt = new NonTerminal("B");
        NonTerminal C_nt = new NonTerminal("C");
        Terminal a_t = new Terminal("a");
        Terminal b_t = new Terminal("b");
        Terminal c_t = new Terminal("c");
        Terminal d_t = new Terminal("d");
        Terminal e_t = new Terminal("e");
        
        Production spProd = new Production(Sp, Arrays.asList(S_nt));
        Production sProd1 = new Production(S_nt, Arrays.asList(A_nt, a_t));
        Production sProd2 = new Production(S_nt, Arrays.asList(b_t));
        Production aProd1 = new Production(A_nt, Arrays.asList(B_nt, C_nt));
        Production aProd2 = new Production(A_nt, Arrays.asList(c_t));
        Production bProd1 = new Production(B_nt, Arrays.asList(d_t));
        Production bProd2 = new Production(B_nt); // epsilon
        Production cProd = new Production(C_nt, Arrays.asList(e_t));
        
        Grammar complexGrammar = new Grammar(Sp);
        complexGrammar.addProduction(spProd);
        complexGrammar.addProduction(sProd1);
        complexGrammar.addProduction(sProd2);
        complexGrammar.addProduction(aProd1);
        complexGrammar.addProduction(aProd2);
        complexGrammar.addProduction(bProd1);
        complexGrammar.addProduction(bProd2);
        complexGrammar.addProduction(cProd);
        complexGrammar.computeFirstFollowSets();
        
        // Create initial item: S' ::= • S
        LRItem initialItem = new LRItem(spProd, 0, new HashSet<>(Arrays.asList(dollar)));
        LRItemSet initialSet = new LRItemSet(Arrays.asList(initialItem));
        
        // Compute closure
        LRItemSet closureSet = initialSet.closure(complexGrammar);
        
        // Should contain multiple items due to closure
        assertTrue(closureSet.size() >= 5); // At least initial + S productions + A productions
        
        // Verify specific items are present
        boolean hasSpItem = false, hasSProd1 = false, hasSProd2 = false;
        boolean hasAProd1 = false, hasAProd2 = false;
        
        for (LRItem item : closureSet.getItems()) {
            if (item.getProduction().equals(spProd) && item.getDotPosition() == 0) {
                hasSpItem = true;
            }
            if (item.getProduction().equals(sProd1) && item.getDotPosition() == 0) {
                hasSProd1 = true;
            }
            if (item.getProduction().equals(sProd2) && item.getDotPosition() == 0) {
                hasSProd2 = true;
            }
            if (item.getProduction().equals(aProd1) && item.getDotPosition() == 0) {
                hasAProd1 = true;
            }
            if (item.getProduction().equals(aProd2) && item.getDotPosition() == 0) {
                hasAProd2 = true;
            }
        }
        
        assertTrue(hasSpItem, "Should contain S' ::= • S");
        assertTrue(hasSProd1, "Should contain S ::= • A a");
        assertTrue(hasSProd2, "Should contain S ::= • b");
        assertTrue(hasAProd1, "Should contain A ::= • B C");
        assertTrue(hasAProd2, "Should contain A ::= • c");
    }
}