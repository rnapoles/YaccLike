package com.parsergenerator.grammar;

import com.parsergenerator.analysis.FirstFollowCalculator;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;

import java.util.*;

/**
 * Represents a complete context-free grammar with productions, symbols, and a start symbol.
 * The Grammar class manages the symbol table and provides methods for adding productions
 * and validating the grammar structure.
 */
public class Grammar {
    private NonTerminal startSymbol;
    private final Set<Production> productions;
    private final Set<Terminal> terminals;
    private final Set<NonTerminal> nonTerminals;
    private final Map<String, Symbol> symbolTable;
    
    // FIRST/FOLLOW analysis results
    private FirstFollowCalculator firstFollowCalculator;
    private boolean analysisComputed;

    /**
     * Constructs a new empty Grammar.
     */
    public Grammar() {
        this.productions = new LinkedHashSet<>();
        this.terminals = new LinkedHashSet<>();
        this.nonTerminals = new LinkedHashSet<>();
        this.symbolTable = new HashMap<>();
        this.startSymbol = null;
        this.firstFollowCalculator = null;
        this.analysisComputed = false;
    }

    /**
     * Constructs a new Grammar with the specified start symbol.
     *
     * @param startSymbol the start symbol of the grammar
     * @throws IllegalArgumentException if startSymbol is null
     */
    public Grammar(NonTerminal startSymbol) {
        this();
        setStartSymbol(startSymbol);
    }

    /**
     * Sets the start symbol of the grammar.
     *
     * @param startSymbol the start symbol to set
     * @throws IllegalArgumentException if startSymbol is null
     */
    public void setStartSymbol(NonTerminal startSymbol) {
        if (startSymbol == null) {
            throw new IllegalArgumentException("Start symbol cannot be null");
        }
        this.startSymbol = startSymbol;
        addNonTerminal(startSymbol);
        
        // Invalidate analysis since start symbol affects FOLLOW sets
        invalidateAnalysis();
    }

    /**
     * Returns the start symbol of the grammar.
     *
     * @return the start symbol, or null if not set
     */
    public NonTerminal getStartSymbol() {
        return startSymbol;
    }

    /**
     * Checks if the grammar has a start symbol defined.
     *
     * @return true if start symbol is defined, false otherwise
     */
    public boolean hasStartSymbol() {
        return startSymbol != null;
    }

    /**
     * Adds a production to the grammar and updates the symbol tables.
     *
     * @param production the production to add
     * @throws IllegalArgumentException if production is null or invalid
     */
    public void addProduction(Production production) {
        if (production == null) {
            throw new IllegalArgumentException("Production cannot be null");
        }
        if (!production.isValid()) {
            throw new IllegalArgumentException("Production is not valid: " + production);
        }

        productions.add(production);
        
        // Add left-hand side non-terminal
        NonTerminal leftSide = production.getLeftSide();
        addNonTerminal(leftSide);
        leftSide.addProduction(production);

        // Add all symbols from right-hand side
        for (Symbol symbol : production.getRightSide()) {
            addSymbol(symbol);
        }
        
        // Invalidate analysis since grammar has changed
        invalidateAnalysis();
    }

    /**
     * Adds a terminal symbol to the grammar.
     *
     * @param terminal the terminal to add
     * @throws IllegalArgumentException if terminal is null
     */
    public void addTerminal(Terminal terminal) {
        if (terminal == null) {
            throw new IllegalArgumentException("Terminal cannot be null");
        }
        terminals.add(terminal);
        symbolTable.put(terminal.getName(), terminal);
    }

    /**
     * Adds a non-terminal symbol to the grammar.
     *
     * @param nonTerminal the non-terminal to add
     * @throws IllegalArgumentException if nonTerminal is null
     */
    public void addNonTerminal(NonTerminal nonTerminal) {
        if (nonTerminal == null) {
            throw new IllegalArgumentException("Non-terminal cannot be null");
        }
        nonTerminals.add(nonTerminal);
        symbolTable.put(nonTerminal.getName(), nonTerminal);
    }

    /**
     * Adds a symbol to the appropriate symbol set based on its type.
     *
     * @param symbol the symbol to add
     * @throws IllegalArgumentException if symbol is null
     */
    public void addSymbol(Symbol symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }
        
        if (symbol.isTerminal()) {
            addTerminal((Terminal) symbol);
        } else {
            addNonTerminal((NonTerminal) symbol);
        }
    }

    /**
     * Looks up a symbol by name in the symbol table.
     *
     * @param name the name of the symbol to find
     * @return the symbol with the given name, or null if not found
     */
    public Symbol getSymbol(String name) {
        if (name == null) {
            return null;
        }
        return symbolTable.get(name);
    }

    /**
     * Checks if a symbol with the given name exists in the grammar.
     *
     * @param name the name to check
     * @return true if a symbol with the name exists, false otherwise
     */
    public boolean hasSymbol(String name) {
        return getSymbol(name) != null;
    }

    /**
     * Gets a terminal symbol by name.
     *
     * @param name the name of the terminal
     * @return the terminal symbol, or null if not found or not a terminal
     */
    public Terminal getTerminal(String name) {
        Symbol symbol = getSymbol(name);
        return (symbol instanceof Terminal) ? (Terminal) symbol : null;
    }

    /**
     * Gets a non-terminal symbol by name.
     *
     * @param name the name of the non-terminal
     * @return the non-terminal symbol, or null if not found or not a non-terminal
     */
    public NonTerminal getNonTerminal(String name) {
        Symbol symbol = getSymbol(name);
        return (symbol instanceof NonTerminal) ? (NonTerminal) symbol : null;
    }

    /**
     * Returns an unmodifiable view of all productions in the grammar.
     *
     * @return the set of productions
     */
    public Set<Production> getProductions() {
        return Collections.unmodifiableSet(productions);
    }

    /**
     * Returns an unmodifiable view of all terminal symbols in the grammar.
     *
     * @return the set of terminals
     */
    public Set<Terminal> getTerminals() {
        return Collections.unmodifiableSet(terminals);
    }

    /**
     * Returns an unmodifiable view of all non-terminal symbols in the grammar.
     *
     * @return the set of non-terminals
     */
    public Set<NonTerminal> getNonTerminals() {
        return Collections.unmodifiableSet(nonTerminals);
    }

    /**
     * Returns an unmodifiable view of all symbols in the grammar.
     *
     * @return all symbols (terminals and non-terminals)
     */
    public Set<Symbol> getAllSymbols() {
        Set<Symbol> allSymbols = new LinkedHashSet<>();
        allSymbols.addAll(terminals);
        allSymbols.addAll(nonTerminals);
        return Collections.unmodifiableSet(allSymbols);
    }

    /**
     * Returns the productions for a specific non-terminal.
     *
     * @param nonTerminal the non-terminal to get productions for
     * @return the set of productions for the non-terminal
     */
    public Set<Production> getProductionsFor(NonTerminal nonTerminal) {
        if (nonTerminal == null) {
            return Collections.emptySet();
        }
        return nonTerminal.getProductions();
    }

    /**
     * Returns the productions for a non-terminal by name.
     *
     * @param nonTerminalName the name of the non-terminal
     * @return the set of productions for the non-terminal, or empty set if not found
     */
    public Set<Production> getProductionsFor(String nonTerminalName) {
        NonTerminal nt = getNonTerminal(nonTerminalName);
        return getProductionsFor(nt);
    }

    /**
     * Returns the number of productions in the grammar.
     *
     * @return the production count
     */
    public int getProductionCount() {
        return productions.size();
    }

    /**
     * Returns the number of terminal symbols in the grammar.
     *
     * @return the terminal count
     */
    public int getTerminalCount() {
        return terminals.size();
    }

    /**
     * Returns the number of non-terminal symbols in the grammar.
     *
     * @return the non-terminal count
     */
    public int getNonTerminalCount() {
        return nonTerminals.size();
    }

    /**
     * Checks if the grammar is empty (has no productions).
     *
     * @return true if the grammar has no productions, false otherwise
     */
    public boolean isEmpty() {
        return productions.isEmpty();
    }

    /**
     * Validates the grammar structure and returns a list of validation errors.
     *
     * @return a list of validation error messages, empty if grammar is valid
     */
    public List<String> validateGrammar() {
        List<String> errors = new ArrayList<>();

        // Check if start symbol is defined
        if (startSymbol == null) {
            errors.add("Grammar must have a start symbol defined");
        }

        // Check if grammar has productions
        if (productions.isEmpty()) {
            errors.add("Grammar must have at least one production");
        }

        // Check if start symbol has productions
        if (startSymbol != null && !startSymbol.hasProductions()) {
            errors.add("Start symbol '" + startSymbol.getName() + "' has no productions");
        }

        // Check for undefined symbols (symbols used in productions but not defined)
        Set<String> definedSymbols = new HashSet<>();
        for (Symbol symbol : getAllSymbols()) {
            definedSymbols.add(symbol.getName());
        }

        for (Production production : productions) {
            for (Symbol symbol : production.getRightSide()) {
                if (!definedSymbols.contains(symbol.getName())) {
                    errors.add("Undefined symbol '" + symbol.getName() + "' used in production: " + production);
                }
            }
        }

        // Check for unreachable non-terminals
        Set<NonTerminal> reachable = findReachableNonTerminals();
        for (NonTerminal nt : nonTerminals) {
            if (!reachable.contains(nt)) {
                errors.add("Non-terminal '" + nt.getName() + "' is unreachable from start symbol");
            }
        }

        // Check for reachable non-terminals without productions
        for (NonTerminal nt : reachable) {
            if (!nt.hasProductions()) {
                errors.add("Non-terminal '" + nt.getName() + "' is reachable but has no productions");
            }
        }

        return errors;
    }

    /**
     * Checks if the grammar is valid (has no validation errors).
     *
     * @return true if the grammar is valid, false otherwise
     */
    public boolean isValid() {
        return validateGrammar().isEmpty();
    }

    /**
     * Finds all non-terminals reachable from the start symbol.
     *
     * @return the set of reachable non-terminals
     */
    private Set<NonTerminal> findReachableNonTerminals() {
        if (startSymbol == null) {
            return Collections.emptySet();
        }

        Set<NonTerminal> reachable = new HashSet<>();
        Queue<NonTerminal> queue = new LinkedList<>();
        
        queue.add(startSymbol);
        reachable.add(startSymbol);

        while (!queue.isEmpty()) {
            NonTerminal current = queue.poll();
            
            for (Production production : current.getProductions()) {
                for (Symbol symbol : production.getRightSide()) {
                    if (symbol.isNonTerminal()) {
                        NonTerminal nt = (NonTerminal) symbol;
                        if (!reachable.contains(nt)) {
                            reachable.add(nt);
                            queue.add(nt);
                        }
                    }
                }
            }
        }

        return reachable;
    }

    /**
     * Computes FIRST and FOLLOW sets for all symbols in the grammar.
     * This method should be called after all productions have been added.
     * The analysis results are cached and will not be recomputed unless
     * invalidateAnalysis() is called.
     */
    public void computeFirstFollowSets() {
        if (!analysisComputed) {
            firstFollowCalculator = new FirstFollowCalculator(this);
            analysisComputed = true;
        }
    }

    /**
     * Invalidates the cached FIRST/FOLLOW analysis results.
     * This should be called whenever the grammar is modified
     * (productions added/removed, symbols changed, etc.).
     */
    public void invalidateAnalysis() {
        firstFollowCalculator = null;
        analysisComputed = false;
    }

    /**
     * Returns the FIRST set for the given symbol.
     * Computes the analysis if not already done.
     *
     * @param symbol the symbol to get FIRST set for
     * @return the FIRST set, or empty set if symbol is null
     */
    public Set<Terminal> getFirstSet(Symbol symbol) {
        ensureAnalysisComputed();
        return firstFollowCalculator.getFirstSet(symbol);
    }

    /**
     * Returns the FOLLOW set for the given non-terminal.
     * Computes the analysis if not already done.
     *
     * @param nonTerminal the non-terminal to get FOLLOW set for
     * @return the FOLLOW set, or empty set if non-terminal is null
     */
    public Set<Terminal> getFollowSet(NonTerminal nonTerminal) {
        ensureAnalysisComputed();
        return firstFollowCalculator.getFollowSet(nonTerminal);
    }

    /**
     * Checks if the given symbol is nullable (can derive epsilon).
     * Computes the analysis if not already done.
     *
     * @param symbol the symbol to check
     * @return true if the symbol is nullable, false otherwise
     */
    public boolean isNullable(Symbol symbol) {
        ensureAnalysisComputed();
        return firstFollowCalculator.isNullable(symbol);
    }

    /**
     * Returns the set of all nullable symbols.
     * Computes the analysis if not already done.
     *
     * @return unmodifiable set of nullable symbols
     */
    public Set<Symbol> getNullableSymbols() {
        ensureAnalysisComputed();
        return firstFollowCalculator.getNullableSymbols();
    }

    /**
     * Computes the FIRST set for a sequence of symbols.
     * This is useful for computing FIRST sets of production right-hand sides.
     * Computes the analysis if not already done.
     *
     * @param symbols the sequence of symbols
     * @return the FIRST set of the sequence
     */
    public Set<Terminal> getFirstSetOfSequence(List<Symbol> symbols) {
        ensureAnalysisComputed();
        return firstFollowCalculator.getFirstSetOfSequence(symbols);
    }

    /**
     * Checks if FIRST/FOLLOW analysis has been computed.
     *
     * @return true if analysis is computed, false otherwise
     */
    public boolean isAnalysisComputed() {
        return analysisComputed;
    }

    /**
     * Returns the FirstFollowCalculator instance used for analysis.
     * Computes the analysis if not already done.
     *
     * @return the FirstFollowCalculator instance
     */
    public FirstFollowCalculator getFirstFollowCalculator() {
        ensureAnalysisComputed();
        return firstFollowCalculator;
    }

    /**
     * Ensures that FIRST/FOLLOW analysis has been computed.
     * If not computed, computes it automatically.
     */
    private void ensureAnalysisComputed() {
        if (!analysisComputed) {
            computeFirstFollowSets();
        }
    }

    /**
     * Clears all productions and symbols from the grammar.
     */
    public void clear() {
        productions.clear();
        terminals.clear();
        nonTerminals.clear();
        symbolTable.clear();
        startSymbol = null;
        invalidateAnalysis();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Grammar {\n");
        
        if (startSymbol != null) {
            sb.append("  Start: ").append(startSymbol.getName()).append("\n");
        }
        
        sb.append("  Productions:\n");
        for (Production production : productions) {
            sb.append("    ").append(production.toString()).append("\n");
        }
        
        sb.append("  Terminals: ").append(terminals.size()).append("\n");
        sb.append("  Non-terminals: ").append(nonTerminals.size()).append("\n");
        sb.append("}");
        
        return sb.toString();
    }

    /**
     * Returns a detailed string representation of the grammar.
     *
     * @return detailed string representation
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Grammar {\n");
        
        if (startSymbol != null) {
            sb.append("  Start Symbol: ").append(startSymbol.getName()).append("\n");
        }
        
        sb.append("  Productions (").append(productions.size()).append("):\n");
        for (Production production : productions) {
            sb.append("    ").append(production.toDetailedString()).append("\n");
        }
        
        sb.append("  Terminals (").append(terminals.size()).append("):\n");
        for (Terminal terminal : terminals) {
            sb.append("    ").append(terminal.toString()).append("\n");
        }
        
        sb.append("  Non-terminals (").append(nonTerminals.size()).append("):\n");
        for (NonTerminal nonTerminal : nonTerminals) {
            sb.append("    ").append(nonTerminal.toString()).append("\n");
        }
        
        sb.append("}");
        
        return sb.toString();
    }
}