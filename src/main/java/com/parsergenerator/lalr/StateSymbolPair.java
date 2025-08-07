package com.parsergenerator.lalr;

import com.parsergenerator.symbol.Symbol;

import java.util.Objects;

/**
 * Represents a (state, symbol) pair used as a key in parsing tables.
 * This is used to index both ACTION and GOTO tables.
 */
public class StateSymbolPair {
    private final int state;
    private final Symbol symbol;

    /**
     * Constructs a new StateSymbolPair.
     *
     * @param state the state number
     * @param symbol the symbol
     * @throws IllegalArgumentException if symbol is null or state is negative
     */
    public StateSymbolPair(int state, Symbol symbol) {
        if (state < 0) {
            throw new IllegalArgumentException("State number cannot be negative");
        }
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }
        this.state = state;
        this.symbol = symbol;
    }

    /**
     * Returns the state number.
     *
     * @return the state number
     */
    public int getState() {
        return state;
    }

    /**
     * Returns the symbol.
     *
     * @return the symbol
     */
    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StateSymbolPair that = (StateSymbolPair) obj;
        return state == that.state && Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, symbol);
    }

    @Override
    public String toString() {
        return "(" + state + ", " + symbol.getName() + ")";
    }
}