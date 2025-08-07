package com.parsergenerator.symbol;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Utility class providing common operations for working with grammar symbols.
 * This class contains static methods for symbol manipulation and analysis.
 */
public final class SymbolUtils {

    // Private constructor to prevent instantiation
    private SymbolUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Filters a collection of symbols to return only terminal symbols.
     *
     * @param symbols the collection of symbols to filter
     * @return a set containing only the terminal symbols
     */
    public static Set<Terminal> getTerminals(Collection<Symbol> symbols) {
        if (symbols == null) {
            return new HashSet<>();
        }
        return symbols.stream()
                .filter(Symbol::isTerminal)
                .map(symbol -> (Terminal) symbol)
                .collect(Collectors.toSet());
    }

    /**
     * Filters a collection of symbols to return only non-terminal symbols.
     *
     * @param symbols the collection of symbols to filter
     * @return a set containing only the non-terminal symbols
     */
    public static Set<NonTerminal> getNonTerminals(Collection<Symbol> symbols) {
        if (symbols == null) {
            return new HashSet<>();
        }
        return symbols.stream()
                .filter(Symbol::isNonTerminal)
                .map(symbol -> (NonTerminal) symbol)
                .collect(Collectors.toSet());
    }

    /**
     * Checks if a symbol name represents a terminal symbol based on naming conventions.
     * This is a heuristic method that assumes terminals are uppercase or contain
     * special characters, while non-terminals are enclosed in angle brackets.
     *
     * @param symbolName the name to check
     * @return true if the name appears to represent a terminal
     */
    public static boolean isTerminalName(String symbolName) {
        if (symbolName == null || symbolName.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = symbolName.trim();
        
        // Non-terminals are typically enclosed in angle brackets
        if (trimmed.startsWith("<") && trimmed.endsWith(">")) {
            return false;
        }
        
        // Terminals are typically uppercase or contain special characters
        return trimmed.equals(trimmed.toUpperCase()) || 
               trimmed.matches(".*[^a-zA-Z0-9_].*");
    }

    /**
     * Checks if a symbol name represents a non-terminal symbol based on naming conventions.
     *
     * @param symbolName the name to check
     * @return true if the name appears to represent a non-terminal
     */
    public static boolean isNonTerminalName(String symbolName) {
        return !isTerminalName(symbolName);
    }

    /**
     * Creates a terminal symbol with the given name.
     *
     * @param name the name of the terminal
     * @return a new Terminal instance
     * @throws IllegalArgumentException if name is null or empty
     */
    public static Terminal createTerminal(String name) {
        return new Terminal(name);
    }

    /**
     * Creates a terminal symbol with the given name and pattern.
     *
     * @param name the name of the terminal
     * @param pattern the pattern for the terminal
     * @return a new Terminal instance
     * @throws IllegalArgumentException if name is null or empty
     */
    public static Terminal createTerminal(String name, String pattern) {
        return new Terminal(name, pattern);
    }

    /**
     * Creates a non-terminal symbol with the given name.
     *
     * @param name the name of the non-terminal
     * @return a new NonTerminal instance
     * @throws IllegalArgumentException if name is null or empty
     */
    public static NonTerminal createNonTerminal(String name) {
        return new NonTerminal(name);
    }

    /**
     * Validates that a symbol name is valid according to grammar conventions.
     *
     * @param symbolName the name to validate
     * @return true if the name is valid
     */
    public static boolean isValidSymbolName(String symbolName) {
        if (symbolName == null || symbolName.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = symbolName.trim();
        
        // Check for basic validity (no whitespace in the middle, reasonable length)
        return !trimmed.contains(" ") && 
               trimmed.length() > 0 && 
               trimmed.length() <= 100 &&
               trimmed.matches("[a-zA-Z_<>][a-zA-Z0-9_<>]*");
    }
}