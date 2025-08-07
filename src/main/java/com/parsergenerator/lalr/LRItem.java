package com.parsergenerator.lalr;

import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an LR item, which is a production with a dot position indicating
 * the current parsing state. The dot position shows how much of the production
 * has been recognized so far.
 * 
 * For example, if we have production A ::= B C D, the possible items are:
 * - A ::= • B C D (dot at position 0)
 * - A ::= B • C D (dot at position 1)
 * - A ::= B C • D (dot at position 2)
 * - A ::= B C D • (dot at position 3)
 */
public class LRItem {
    private final Production production;
    private final int dotPosition;
    private final Set<Terminal> lookaheads;

    /**
     * Constructs a new LR item with the specified production and dot position.
     * No lookahead symbols are initially set.
     *
     * @param production the production rule
     * @param dotPosition the position of the dot (0-based)
     * @throws IllegalArgumentException if production is null or dotPosition is invalid
     */
    public LRItem(Production production, int dotPosition) {
        this(production, dotPosition, new HashSet<>());
    }

    /**
     * Constructs a new LR item with the specified production, dot position, and lookaheads.
     *
     * @param production the production rule
     * @param dotPosition the position of the dot (0-based)
     * @param lookaheads the set of lookahead terminals
     * @throws IllegalArgumentException if production is null, dotPosition is invalid, or lookaheads is null
     */
    public LRItem(Production production, int dotPosition, Set<Terminal> lookaheads) {
        if (production == null) {
            throw new IllegalArgumentException("Production cannot be null");
        }
        if (dotPosition < 0 || dotPosition > production.getRightSideLength()) {
            throw new IllegalArgumentException("Dot position " + dotPosition + 
                " is invalid for production with " + production.getRightSideLength() + " symbols");
        }
        if (lookaheads == null) {
            throw new IllegalArgumentException("Lookaheads set cannot be null");
        }

        this.production = production;
        this.dotPosition = dotPosition;
        this.lookaheads = new HashSet<>(lookaheads);
    }

    /**
     * Returns the production associated with this item.
     *
     * @return the production
     */
    public Production getProduction() {
        return production;
    }

    /**
     * Returns the dot position in this item.
     *
     * @return the dot position (0-based)
     */
    public int getDotPosition() {
        return dotPosition;
    }

    /**
     * Returns an unmodifiable view of the lookahead symbols.
     *
     * @return the set of lookahead terminals
     */
    public Set<Terminal> getLookaheads() {
        return Collections.unmodifiableSet(lookaheads);
    }

    /**
     * Adds a lookahead symbol to this item.
     *
     * @param lookahead the terminal to add as lookahead
     * @throws IllegalArgumentException if lookahead is null
     */
    public void addLookahead(Terminal lookahead) {
        if (lookahead == null) {
            throw new IllegalArgumentException("Lookahead terminal cannot be null");
        }
        lookaheads.add(lookahead);
    }

    /**
     * Adds multiple lookahead symbols to this item.
     *
     * @param newLookaheads the terminals to add as lookaheads
     * @throws IllegalArgumentException if newLookaheads is null
     */
    public void addLookaheads(Set<Terminal> newLookaheads) {
        if (newLookaheads == null) {
            throw new IllegalArgumentException("Lookaheads set cannot be null");
        }
        lookaheads.addAll(newLookaheads);
    }

    /**
     * Returns the symbol immediately after the dot, or null if the dot is at the end.
     *
     * @return the next symbol, or null if at the end of production
     */
    public Symbol getNextSymbol() {
        if (isComplete()) {
            return null;
        }
        return production.getRightSideSymbol(dotPosition);
    }

    /**
     * Checks if this item is complete (dot is at the end of the production).
     *
     * @return true if the item is complete, false otherwise
     */
    public boolean isComplete() {
        return dotPosition >= production.getRightSideLength();
    }

    /**
     * Checks if this item can be advanced (dot is not at the end).
     *
     * @return true if the item can be advanced, false otherwise
     */
    public boolean canAdvance() {
        return !isComplete();
    }

    /**
     * Creates a new LR item by advancing the dot position by one.
     * The lookaheads are copied to the new item.
     *
     * @return a new LR item with the dot advanced by one position
     * @throws IllegalStateException if the item cannot be advanced (dot is at the end)
     */
    public LRItem advance() {
        if (!canAdvance()) {
            throw new IllegalStateException("Cannot advance item: dot is already at the end");
        }
        return new LRItem(production, dotPosition + 1, new HashSet<>(lookaheads));
    }

    /**
     * Creates a new LR item by advancing the dot position by one, with new lookaheads.
     *
     * @param newLookaheads the lookaheads for the advanced item
     * @return a new LR item with the dot advanced by one position
     * @throws IllegalStateException if the item cannot be advanced
     * @throws IllegalArgumentException if newLookaheads is null
     */
    public LRItem advance(Set<Terminal> newLookaheads) {
        if (!canAdvance()) {
            throw new IllegalStateException("Cannot advance item: dot is already at the end");
        }
        if (newLookaheads == null) {
            throw new IllegalArgumentException("Lookaheads set cannot be null");
        }
        return new LRItem(production, dotPosition + 1, newLookaheads);
    }

    /**
     * Returns the core of this item (production and dot position without lookaheads).
     * This is useful for comparing items ignoring lookaheads.
     *
     * @return a new LR item with the same production and dot position but no lookaheads
     */
    public LRItem getCore() {
        return new LRItem(production, dotPosition);
    }

    /**
     * Checks if this item has the same core as another item (same production and dot position).
     *
     * @param other the other item to compare with
     * @return true if both items have the same core, false otherwise
     */
    public boolean hasSameCore(LRItem other) {
        if (other == null) {
            return false;
        }
        return Objects.equals(production, other.production) && dotPosition == other.dotPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LRItem lrItem = (LRItem) obj;
        return dotPosition == lrItem.dotPosition &&
               Objects.equals(production, lrItem.production) &&
               Objects.equals(lookaheads, lrItem.lookaheads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(production, dotPosition, lookaheads);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(production.getLeftSide().getName()).append(" ::= ");
        
        // Add symbols before the dot
        for (int i = 0; i < dotPosition; i++) {
            if (i > 0) sb.append(" ");
            sb.append(production.getRightSideSymbol(i).getName());
        }
        
        // Add the dot
        if (dotPosition > 0 && dotPosition < production.getRightSideLength()) {
            sb.append(" ");
        }
        sb.append("•");
        
        // Add symbols after the dot
        for (int i = dotPosition; i < production.getRightSideLength(); i++) {
            sb.append(" ").append(production.getRightSideSymbol(i).getName());
        }
        
        // Add lookaheads if present
        if (!lookaheads.isEmpty()) {
            sb.append(", {");
            boolean first = true;
            for (Terminal lookahead : lookaheads) {
                if (!first) sb.append(", ");
                sb.append(lookahead.getName());
                first = false;
            }
            sb.append("}");
        }
        
        return sb.toString();
    }

    /**
     * Returns a string representation showing only the core (without lookaheads).
     *
     * @return string representation of the core item
     */
    public String toCoreString() {
        return getCore().toString();
    }
}