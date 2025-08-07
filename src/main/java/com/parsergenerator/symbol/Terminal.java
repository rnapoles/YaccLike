package com.parsergenerator.symbol;

import java.util.Objects;

/**
 * Represents a terminal symbol in a context-free grammar.
 * Terminal symbols are the basic tokens that appear in the input and cannot
 * be further expanded. They correspond to the leaves of parse trees.
 */
public class Terminal extends Symbol {
    private final String pattern;

    /**
     * Constructs a new Terminal symbol with the specified name.
     *
     * @param name the name of the terminal symbol
     * @throws IllegalArgumentException if name is null or empty
     */
    public Terminal(String name) {
        this(name, null);
    }

    /**
     * Constructs a new Terminal symbol with the specified name and pattern.
     *
     * @param name the name of the terminal symbol
     * @param pattern the pattern or regular expression that defines this terminal
     * @throws IllegalArgumentException if name is null or empty
     */
    public Terminal(String name, String pattern) {
        super(name, SymbolType.TERMINAL);
        this.pattern = pattern;
    }

    /**
     * Returns the pattern associated with this terminal symbol.
     * The pattern defines how this terminal is recognized in the input.
     *
     * @return the pattern string, or null if no pattern is defined
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Checks if this terminal has a pattern defined.
     *
     * @return true if a pattern is defined, false otherwise
     */
    public boolean hasPattern() {
        return pattern != null && !pattern.trim().isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Terminal terminal = (Terminal) obj;
        return Objects.equals(pattern, terminal.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pattern);
    }

    @Override
    public String toString() {
        if (hasPattern()) {
            return name + " = {" + pattern + "}";
        }
        return name;
    }
}