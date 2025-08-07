package com.parsergenerator.symbol;

import java.util.Objects;

/**
 * Abstract base class representing a symbol in a context-free grammar.
 * Symbols can be either terminals or non-terminals and serve as the
 * fundamental building blocks of grammar productions.
 */
public abstract class Symbol {
    protected final String name;
    protected final SymbolType type;

    /**
     * Constructs a new Symbol with the specified name and type.
     *
     * @param name the name of the symbol, must not be null or empty
     * @param type the type of the symbol (terminal or non-terminal)
     * @throws IllegalArgumentException if name is null or empty
     */
    protected Symbol(String name, SymbolType type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Symbol type cannot be null");
        }
        this.name = name.trim();
        this.type = type;
    }

    /**
     * Returns the name of this symbol.
     *
     * @return the symbol name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this symbol.
     *
     * @return the symbol type
     */
    public SymbolType getType() {
        return type;
    }

    /**
     * Checks if this symbol is a terminal.
     *
     * @return true if this symbol is a terminal, false otherwise
     */
    public boolean isTerminal() {
        return type == SymbolType.TERMINAL;
    }

    /**
     * Checks if this symbol is a non-terminal.
     *
     * @return true if this symbol is a non-terminal, false otherwise
     */
    public boolean isNonTerminal() {
        return type == SymbolType.NON_TERMINAL;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Symbol symbol = (Symbol) obj;
        return Objects.equals(name, symbol.name) && type == symbol.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return name;
    }
}