package com.parsergenerator.symbol;

/**
 * Enumeration representing the different types of symbols in a grammar.
 * Symbols can be either terminals (leaf nodes in parse trees) or non-terminals
 * (internal nodes that can be expanded into other symbols).
 */
public enum SymbolType {
    /**
     * Terminal symbols represent tokens that appear directly in the input.
     * They cannot be further expanded and form the leaves of parse trees.
     */
    TERMINAL,
    
    /**
     * Non-terminal symbols represent syntactic categories that can be
     * expanded into sequences of other symbols according to production rules.
     */
    NON_TERMINAL
}