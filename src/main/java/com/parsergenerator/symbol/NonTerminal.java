package com.parsergenerator.symbol;

import com.parsergenerator.grammar.Production;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

/**
 * Represents a non-terminal symbol in a context-free grammar.
 * Non-terminal symbols can be expanded into sequences of other symbols
 * according to production rules. They correspond to internal nodes in parse trees.
 */
public class NonTerminal extends Symbol {
    private final Set<Production> productions;

    /**
     * Constructs a new NonTerminal symbol with the specified name.
     *
     * @param name the name of the non-terminal symbol
     * @throws IllegalArgumentException if name is null or empty
     */
    public NonTerminal(String name) {
        super(name, SymbolType.NON_TERMINAL);
        this.productions = new HashSet<>();
    }

    /**
     * Adds a production rule to this non-terminal.
     *
     * @param production the production rule to add
     */
    public void addProduction(Production production) {
        if (production != null) {
            productions.add(production);
        }
    }

    /**
     * Returns an unmodifiable view of the productions for this non-terminal.
     *
     * @return the set of productions
     */
    public Set<Production> getProductions() {
        return Collections.unmodifiableSet(productions);
    }

    /**
     * Returns the number of productions for this non-terminal.
     *
     * @return the production count
     */
    public int getProductionCount() {
        return productions.size();
    }

    /**
     * Checks if this non-terminal has any productions defined.
     *
     * @return true if productions exist, false otherwise
     */
    public boolean hasProductions() {
        return !productions.isEmpty();
    }

    /**
     * Removes all productions from this non-terminal.
     */
    public void clearProductions() {
        productions.clear();
    }

    @Override
    public String toString() {
        return "<" + name + ">";
    }
}