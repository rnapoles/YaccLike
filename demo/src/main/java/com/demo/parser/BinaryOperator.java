package com.demo.parser;

/**
 * Enumeration of binary operators supported by the expression evaluator.
 * Each operator has a symbol representation and precedence level.
 */
public enum BinaryOperator {
    PLUS("+", 1),
    MINUS("-", 1),
    MULTIPLY("*", 2),
    DIVIDE("/", 2),
    MODULO("%", 2),
    POWER("^", 3);

    private final String symbol;
    private final int precedence;

    BinaryOperator(String symbol, int precedence) {
        this.symbol = symbol;
        this.precedence = precedence;
    }

    /**
     * Gets the string symbol representation of the operator.
     * @return the operator symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Gets the precedence level of the operator.
     * Higher numbers indicate higher precedence.
     * @return the precedence level
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * Finds a binary operator by its symbol.
     * @param symbol the operator symbol to look for
     * @return the matching BinaryOperator, or null if not found
     */
    public static BinaryOperator fromSymbol(String symbol) {
        for (BinaryOperator op : values()) {
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