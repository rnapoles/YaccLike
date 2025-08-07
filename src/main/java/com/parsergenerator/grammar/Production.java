package com.parsergenerator.grammar;

import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a production rule in a context-free grammar.
 * A production consists of a left-hand side non-terminal and a right-hand side
 * sequence of symbols (terminals and/or non-terminals).
 * 
 * Production format: A ::= α where A is a non-terminal and α is a sequence of symbols.
 */
public class Production {
    private final NonTerminal leftSide;
    private final List<Symbol> rightSide;
    private final int id;
    
    private static int nextId = 0;

    /**
     * Constructs a new Production with the specified left-hand side and right-hand side.
     *
     * @param leftSide the left-hand side non-terminal
     * @param rightSide the right-hand side sequence of symbols
     * @throws IllegalArgumentException if leftSide is null or rightSide is null
     */
    public Production(NonTerminal leftSide, List<Symbol> rightSide) {
        if (leftSide == null) {
            throw new IllegalArgumentException("Left-hand side cannot be null");
        }
        if (rightSide == null) {
            throw new IllegalArgumentException("Right-hand side cannot be null");
        }
        
        this.leftSide = leftSide;
        this.rightSide = new ArrayList<>(rightSide);
        this.id = nextId++;
        
        validateProduction();
    }

    /**
     * Constructs a new Production with the specified left-hand side and variable arguments for right-hand side.
     *
     * @param leftSide the left-hand side non-terminal
     * @param rightSide the right-hand side symbols
     * @throws IllegalArgumentException if leftSide is null
     */
    public Production(NonTerminal leftSide, Symbol... rightSide) {
        if (leftSide == null) {
            throw new IllegalArgumentException("Left-hand side cannot be null");
        }
        
        this.leftSide = leftSide;
        this.rightSide = new ArrayList<>();
        if (rightSide != null) {
            for (Symbol symbol : rightSide) {
                if (symbol != null) {
                    this.rightSide.add(symbol);
                }
            }
        }
        this.id = nextId++;
        
        validateProduction();
    }

    /**
     * Returns the left-hand side non-terminal of this production.
     *
     * @return the left-hand side non-terminal
     */
    public NonTerminal getLeftSide() {
        return leftSide;
    }

    /**
     * Returns an unmodifiable view of the right-hand side symbols.
     *
     * @return the right-hand side symbol sequence
     */
    public List<Symbol> getRightSide() {
        return Collections.unmodifiableList(rightSide);
    }

    /**
     * Returns the unique identifier of this production.
     *
     * @return the production ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the length of the right-hand side.
     *
     * @return the number of symbols on the right-hand side
     */
    public int getRightSideLength() {
        return rightSide.size();
    }

    /**
     * Checks if this production is an epsilon production (empty right-hand side).
     *
     * @return true if the right-hand side is empty, false otherwise
     */
    public boolean isEpsilonProduction() {
        return rightSide.isEmpty();
    }

    /**
     * Returns the symbol at the specified position on the right-hand side.
     *
     * @param index the position index (0-based)
     * @return the symbol at the specified position
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public Symbol getRightSideSymbol(int index) {
        if (index < 0 || index >= rightSide.size()) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for right-hand side of length " + rightSide.size());
        }
        return rightSide.get(index);
    }

    /**
     * Checks if this production contains the specified symbol on the right-hand side.
     *
     * @param symbol the symbol to check for
     * @return true if the symbol is present on the right-hand side, false otherwise
     */
    public boolean containsSymbol(Symbol symbol) {
        return rightSide.contains(symbol);
    }

    /**
     * Validates the production rule according to grammar constraints.
     * 
     * @throws IllegalArgumentException if the production is invalid
     */
    private void validateProduction() {
        // Check that left-hand side is a non-terminal (already enforced by type)
        if (!leftSide.isNonTerminal()) {
            throw new IllegalArgumentException("Left-hand side must be a non-terminal");
        }
        
        // Validate that all symbols in right-hand side are not null
        for (int i = 0; i < rightSide.size(); i++) {
            if (rightSide.get(i) == null) {
                throw new IllegalArgumentException("Right-hand side symbol at position " + i + " cannot be null");
            }
        }
    }

    /**
     * Checks if this production is valid according to grammar rules.
     *
     * @return true if the production is valid, false otherwise
     */
    public boolean isValid() {
        try {
            validateProduction();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Production that = (Production) obj;
        return Objects.equals(leftSide, that.leftSide) && 
               Objects.equals(rightSide, that.rightSide);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftSide, rightSide);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(leftSide.getName()).append(" ::= ");
        
        if (isEpsilonProduction()) {
            sb.append("ε");
        } else {
            for (int i = 0; i < rightSide.size(); i++) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(rightSide.get(i).getName());
            }
        }
        
        return sb.toString();
    }

    /**
     * Returns a detailed string representation including the production ID.
     *
     * @return detailed string representation
     */
    public String toDetailedString() {
        return "[" + id + "] " + toString();
    }

    /**
     * Resets the production ID counter. Used primarily for testing.
     */
    public static void resetIdCounter() {
        nextId = 0;
    }
}