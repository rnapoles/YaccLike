package com.parsergenerator.lalr;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;

import java.util.*;

/**
 * Represents a set of LR items that form a parser state in the LALR automaton.
 * An LRItemSet contains a collection of LR items and provides methods for
 * computing closure and GOTO functions, which are fundamental operations
 * in LR parser construction.
 */
public class LRItemSet {
    private final Set<LRItem> items;
    private final int id;
    private static int nextId = 0;

    /**
     * Constructs a new empty LRItemSet.
     */
    public LRItemSet() {
        this.items = new LinkedHashSet<>();
        this.id = nextId++;
    }

    /**
     * Constructs a new LRItemSet with the specified items.
     *
     * @param items the initial items for this set
     * @throws IllegalArgumentException if items is null
     */
    public LRItemSet(Collection<LRItem> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items collection cannot be null");
        }
        this.items = new LinkedHashSet<>(items);
        this.id = nextId++;
    }

    /**
     * Returns the unique identifier of this item set.
     *
     * @return the item set ID
     */
    public int getId() {
        return id;
    }

    /**
     * Adds an item to this set.
     *
     * @param item the item to add
     * @return true if the item was added (wasn't already present), false otherwise
     * @throws IllegalArgumentException if item is null
     */
    public boolean addItem(LRItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        return items.add(item);
    }

    /**
     * Adds multiple items to this set.
     *
     * @param newItems the items to add
     * @return true if any items were added, false otherwise
     * @throws IllegalArgumentException if newItems is null
     */
    public boolean addItems(Collection<LRItem> newItems) {
        if (newItems == null) {
            throw new IllegalArgumentException("Items collection cannot be null");
        }
        return items.addAll(newItems);
    }

    /**
     * Removes an item from this set.
     *
     * @param item the item to remove
     * @return true if the item was removed, false otherwise
     */
    public boolean removeItem(LRItem item) {
        return items.remove(item);
    }

    /**
     * Checks if this set contains the specified item.
     *
     * @param item the item to check for
     * @return true if the item is present, false otherwise
     */
    public boolean containsItem(LRItem item) {
        return items.contains(item);
    }

    /**
     * Returns an unmodifiable view of the items in this set.
     *
     * @return the set of items
     */
    public Set<LRItem> getItems() {
        return Collections.unmodifiableSet(items);
    }

    /**
     * Returns the number of items in this set.
     *
     * @return the item count
     */
    public int size() {
        return items.size();
    }

    /**
     * Checks if this item set is empty.
     *
     * @return true if the set contains no items, false otherwise
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Clears all items from this set.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Computes the closure of this item set according to the given grammar.
     * The closure operation adds all items that can be derived from the current
     * items by expanding non-terminals that appear immediately after the dot.
     *
     * @param grammar the grammar to use for closure computation
     * @return a new LRItemSet containing the closure of this set
     * @throws IllegalArgumentException if grammar is null
     */
    public LRItemSet closure(Grammar grammar) {
        if (grammar == null) {
            throw new IllegalArgumentException("Grammar cannot be null");
        }

        LRItemSet closureSet = new LRItemSet(this.items);
        boolean changed = true;

        while (changed) {
            changed = false;
            Set<LRItem> currentItems = new HashSet<>(closureSet.items);

            for (LRItem item : currentItems) {
                if (item.canAdvance()) {
                    Symbol nextSymbol = item.getNextSymbol();
                    
                    if (nextSymbol.isNonTerminal()) {
                        NonTerminal nonTerminal = (NonTerminal) nextSymbol;
                        
                        // Compute FIRST set of symbols after the non-terminal
                        Set<Terminal> firstSet = computeFirstOfBeta(item, grammar);
                        
                        // Add items for all productions of this non-terminal
                        for (Production production : grammar.getProductionsFor(nonTerminal)) {
                            LRItem newItem = new LRItem(production, 0, firstSet);
                            
                            // Check if we need to merge lookaheads with existing item
                            boolean itemAdded = false;
                            for (LRItem existingItem : closureSet.items) {
                                if (existingItem.hasSameCore(newItem)) {
                                    // Merge lookaheads
                                    Set<Terminal> oldLookaheads = new HashSet<>(existingItem.getLookaheads());
                                    existingItem.addLookaheads(newItem.getLookaheads());
                                    if (!oldLookaheads.equals(existingItem.getLookaheads())) {
                                        changed = true;
                                    }
                                    itemAdded = true;
                                    break;
                                }
                            }
                            
                            if (!itemAdded && closureSet.addItem(newItem)) {
                                changed = true;
                            }
                        }
                    }
                }
            }
        }

        return closureSet;
    }

    /**
     * Computes the GOTO function for this item set and the given symbol.
     * The GOTO function returns the set of items that result from advancing
     * the dot past the specified symbol in all applicable items.
     *
     * @param symbol the symbol to advance past
     * @param grammar the grammar to use for closure computation
     * @return a new LRItemSet representing the GOTO result
     * @throws IllegalArgumentException if symbol or grammar is null
     */
    public LRItemSet gotoSet(Symbol symbol, Grammar grammar) {
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }
        if (grammar == null) {
            throw new IllegalArgumentException("Grammar cannot be null");
        }

        LRItemSet gotoSet = new LRItemSet();

        // Find all items that can advance past the given symbol
        for (LRItem item : items) {
            if (item.canAdvance() && item.getNextSymbol().equals(symbol)) {
                LRItem advancedItem = item.advance();
                gotoSet.addItem(advancedItem);
            }
        }

        // Return the closure of the advanced items
        return gotoSet.closure(grammar);
    }

    /**
     * Returns the set of symbols that can be used with the GOTO function
     * from this item set (i.e., symbols that appear immediately after
     * the dot in at least one item).
     *
     * @return the set of symbols for which GOTO is defined
     */
    public Set<Symbol> getGotoSymbols() {
        Set<Symbol> gotoSymbols = new HashSet<>();
        
        for (LRItem item : items) {
            if (item.canAdvance()) {
                gotoSymbols.add(item.getNextSymbol());
            }
        }
        
        return gotoSymbols;
    }

    /**
     * Returns all items in this set that have the dot at the end (reduce items).
     *
     * @return the set of complete items
     */
    public Set<LRItem> getCompleteItems() {
        Set<LRItem> completeItems = new HashSet<>();
        
        for (LRItem item : items) {
            if (item.isComplete()) {
                completeItems.add(item);
            }
        }
        
        return completeItems;
    }

    /**
     * Returns all items in this set that can advance past the given symbol.
     *
     * @param symbol the symbol to check for
     * @return the set of items that can advance past the symbol
     */
    public Set<LRItem> getItemsForSymbol(Symbol symbol) {
        Set<LRItem> matchingItems = new HashSet<>();
        
        for (LRItem item : items) {
            if (item.canAdvance() && item.getNextSymbol().equals(symbol)) {
                matchingItems.add(item);
            }
        }
        
        return matchingItems;
    }

    /**
     * Returns the core of this item set (all items without lookaheads).
     * This is useful for comparing item sets ignoring lookaheads.
     *
     * @return a new LRItemSet containing the core items
     */
    public LRItemSet getCore() {
        LRItemSet coreSet = new LRItemSet();
        
        for (LRItem item : items) {
            coreSet.addItem(item.getCore());
        }
        
        return coreSet;
    }

    /**
     * Checks if this item set has the same core as another item set
     * (same items ignoring lookaheads).
     *
     * @param other the other item set to compare with
     * @return true if both sets have the same core, false otherwise
     */
    public boolean hasSameCore(LRItemSet other) {
        if (other == null) {
            return false;
        }
        
        return this.getCore().equals(other.getCore());
    }

    /**
     * Merges the lookaheads from another item set with the same core.
     * This is used in LALR construction to combine compatible states.
     *
     * @param other the other item set to merge lookaheads from
     * @return true if any lookaheads were added, false otherwise
     * @throws IllegalArgumentException if other is null or doesn't have the same core
     */
    public boolean mergeLookaheads(LRItemSet other) {
        if (other == null) {
            throw new IllegalArgumentException("Other item set cannot be null");
        }
        if (!this.hasSameCore(other)) {
            throw new IllegalArgumentException("Cannot merge lookaheads: item sets have different cores");
        }

        boolean changed = false;
        
        for (LRItem otherItem : other.items) {
            for (LRItem thisItem : items) {
                if (thisItem.hasSameCore(otherItem)) {
                    Set<Terminal> oldLookaheads = new HashSet<>(thisItem.getLookaheads());
                    thisItem.addLookaheads(otherItem.getLookaheads());
                    if (!oldLookaheads.equals(thisItem.getLookaheads())) {
                        changed = true;
                    }
                    break;
                }
            }
        }
        
        return changed;
    }

    /**
     * Computes the FIRST set of the symbols following a non-terminal in an item.
     * This is used during closure computation to determine lookaheads for new items.
     *
     * @param item the item containing the non-terminal
     * @param grammar the grammar to use for FIRST set computation
     * @return the FIRST set of symbols following the non-terminal
     */
    private Set<Terminal> computeFirstOfBeta(LRItem item, Grammar grammar) {
        Set<Terminal> firstSet = new HashSet<>();
        
        if (!item.canAdvance()) {
            return firstSet;
        }
        
        Production production = item.getProduction();
        int dotPos = item.getDotPosition();
        
        // Get symbols after the non-terminal (β in A → α B β)
        List<Symbol> beta = new ArrayList<>();
        for (int i = dotPos + 1; i < production.getRightSideLength(); i++) {
            beta.add(production.getRightSideSymbol(i));
        }
        
        if (beta.isEmpty()) {
            // If β is empty, use the lookaheads of the current item
            firstSet.addAll(item.getLookaheads());
        } else {
            // Compute FIRST(β)
            firstSet.addAll(grammar.getFirstSetOfSequence(beta));
            
            // If β is nullable, also include the lookaheads of the current item
            if (isSequenceNullable(beta, grammar)) {
                firstSet.addAll(item.getLookaheads());
            }
        }
        
        return firstSet;
    }

    /**
     * Checks if a sequence of symbols is nullable (can derive epsilon).
     *
     * @param symbols the sequence of symbols to check
     * @param grammar the grammar to use for nullability checking
     * @return true if the sequence is nullable, false otherwise
     */
    private boolean isSequenceNullable(List<Symbol> symbols, Grammar grammar) {
        for (Symbol symbol : symbols) {
            if (!grammar.isNullable(symbol)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resets the item set ID counter. Used primarily for testing.
     */
    public static void resetIdCounter() {
        nextId = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LRItemSet that = (LRItemSet) obj;
        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("I").append(id).append(" = {\n");
        
        for (LRItem item : items) {
            sb.append("  ").append(item.toString()).append("\n");
        }
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * Returns a detailed string representation of this item set.
     *
     * @return detailed string representation
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LRItemSet[").append(id).append("] (").append(items.size()).append(" items) {\n");
        
        for (LRItem item : items) {
            sb.append("  ").append(item.toString()).append("\n");
        }
        
        sb.append("}");
        return sb.toString();
    }
}