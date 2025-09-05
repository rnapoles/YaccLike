package com.demo.parser;

/**
 * Enumeration of unary operators supported by the expression evaluator.
 */
public enum UnaryOperator {
    MINUS("-");

    private final String symbol;

    UnaryOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Gets the string symbol representation of the operator.
     * @return the operator symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Finds a unary operator by its symbol.
     * @param symbol the operator symbol to look for
     * @return the matching UnaryOperator, or null if not found
     */
    public static UnaryOperator fromSymbol(String symbol) {
        for (UnaryOperator op : values()) {
            if (op.symbol.equals(symbol)) {
                return op;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return symbol;
    }
}