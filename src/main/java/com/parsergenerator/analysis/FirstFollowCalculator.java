package com.parsergenerator.analysis;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;

import java.util.*;

/**
 * Utility class for computing FIRST and FOLLOW sets for grammar symbols.
 * 
 * FIRST sets contain all terminals that can begin strings derived from a symbol.
 * FOLLOW sets contain all terminals that can immediately follow a non-terminal
 * in some sentential form.
 * 
 * This implementation handles epsilon productions and nullable symbols correctly.
 */
public class FirstFollowCalculator {
    private final Grammar grammar;
    private final Map<Symbol, Set<Terminal>> firstSets;
    private final Map<NonTerminal, Set<Terminal>> followSets;
    private final Set<Symbol> nullableSymbols;
    
    // Special terminal to represent end of input
    public static final Terminal END_OF_INPUT = new Terminal("$");
    
    /**
     * Constructs a FirstFollowCalculator for the given grammar.
     * 
     * @param grammar the grammar to analyze
     * @throws IllegalArgumentException if grammar is null
     */
    public FirstFollowCalculator(Grammar grammar) {
        if (grammar == null) {
            throw new IllegalArgumentException("Grammar cannot be null");
        }
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
        this.nullableSymbols = new HashSet<>();
        
        computeAllSets();
    }
    
    /**
     * Returns the FIRST set for the given symbol.
     * 
     * @param symbol the symbol to get FIRST set for
     * @return the FIRST set, or empty set if symbol is null
     */
    public Set<Terminal> getFirstSet(Symbol symbol) {
        if (symbol == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(firstSets.getOrDefault(symbol, Collections.emptySet()));
    }
    
    /**
     * Returns the FOLLOW set for the given non-terminal.
     * 
     * @param nonTerminal the non-terminal to get FOLLOW set for
     * @return the FOLLOW set, or empty set if non-terminal is null
     */
    public Set<Terminal> getFollowSet(NonTerminal nonTerminal) {
        if (nonTerminal == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(followSets.getOrDefault(nonTerminal, Collections.emptySet()));
    }
    
    /**
     * Checks if the given symbol is nullable (can derive epsilon).
     * 
     * @param symbol the symbol to check
     * @return true if the symbol is nullable, false otherwise
     */
    public boolean isNullable(Symbol symbol) {
        return nullableSymbols.contains(symbol);
    }
    
    /**
     * Returns the set of all nullable symbols.
     * 
     * @return unmodifiable set of nullable symbols
     */
    public Set<Symbol> getNullableSymbols() {
        return Collections.unmodifiableSet(nullableSymbols);
    }
    
    /**
     * Computes the FIRST set for a sequence of symbols.
     * This is useful for computing FIRST sets of production right-hand sides.
     * 
     * @param symbols the sequence of symbols
     * @return the FIRST set of the sequence
     */
    public Set<Terminal> getFirstSetOfSequence(List<Symbol> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<Terminal> result = new HashSet<>();
        
        for (Symbol symbol : symbols) {
            Set<Terminal> symbolFirst = getFirstSet(symbol);
            result.addAll(symbolFirst);
            
            // If this symbol is not nullable, we stop here
            if (!isNullable(symbol)) {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Computes all FIRST, FOLLOW, and nullable sets for the grammar.
     */
    private void computeAllSets() {
        computeNullableSymbols();
        computeFirstSets();
        computeFollowSets();
    }
    
    /**
     * Computes which symbols are nullable (can derive epsilon).
     * Uses a fixed-point algorithm that iterates until no more changes occur.
     */
    private void computeNullableSymbols() {
        boolean changed = true;
        
        while (changed) {
            changed = false;
            
            for (Production production : grammar.getProductions()) {
                NonTerminal leftSide = production.getLeftSide();
                
                // If left side is already nullable, skip
                if (nullableSymbols.contains(leftSide)) {
                    continue;
                }
                
                // Check if this production can derive epsilon
                if (canDeriveEpsilon(production)) {
                    nullableSymbols.add(leftSide);
                    changed = true;
                }
            }
        }
    }
    
    /**
     * Checks if a production can derive epsilon.
     * A production can derive epsilon if it's an epsilon production or
     * all symbols on the right-hand side are nullable.
     */
    private boolean canDeriveEpsilon(Production production) {
        // Epsilon production
        if (production.isEpsilonProduction()) {
            return true;
        }
        
        // All symbols must be nullable
        for (Symbol symbol : production.getRightSide()) {
            if (!nullableSymbols.contains(symbol)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Computes FIRST sets for all symbols using a fixed-point algorithm.
     */
    private void computeFirstSets() {
        // Initialize FIRST sets for terminals
        for (Terminal terminal : grammar.getTerminals()) {
            Set<Terminal> firstSet = new HashSet<>();
            firstSet.add(terminal);
            firstSets.put(terminal, firstSet);
        }
        
        // Initialize empty FIRST sets for non-terminals
        for (NonTerminal nonTerminal : grammar.getNonTerminals()) {
            firstSets.put(nonTerminal, new HashSet<>());
        }
        
        boolean changed = true;
        
        while (changed) {
            changed = false;
            
            for (Production production : grammar.getProductions()) {
                NonTerminal leftSide = production.getLeftSide();
                Set<Terminal> leftFirstSet = firstSets.get(leftSide);
                int oldSize = leftFirstSet.size();
                
                // Add FIRST set of the production's right-hand side
                Set<Terminal> rhsFirst = computeFirstOfRightHandSide(production.getRightSide());
                leftFirstSet.addAll(rhsFirst);
                
                if (leftFirstSet.size() > oldSize) {
                    changed = true;
                }
            }
        }
    }
    
    /**
     * Computes the FIRST set of a production's right-hand side.
     */
    private Set<Terminal> computeFirstOfRightHandSide(List<Symbol> rightSide) {
        Set<Terminal> result = new HashSet<>();
        
        if (rightSide.isEmpty()) {
            // Epsilon production - FIRST set is empty for this computation
            return result;
        }
        
        for (Symbol symbol : rightSide) {
            Set<Terminal> symbolFirst = firstSets.get(symbol);
            if (symbolFirst != null) {
                result.addAll(symbolFirst);
            }
            
            // If this symbol is not nullable, stop here
            if (!isNullable(symbol)) {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Computes FOLLOW sets for all non-terminals using a fixed-point algorithm.
     */
    private void computeFollowSets() {
        // Initialize empty FOLLOW sets for all non-terminals
        for (NonTerminal nonTerminal : grammar.getNonTerminals()) {
            followSets.put(nonTerminal, new HashSet<>());
        }
        
        // Add $ to FOLLOW set of start symbol
        if (grammar.hasStartSymbol()) {
            followSets.get(grammar.getStartSymbol()).add(END_OF_INPUT);
        }
        
        boolean changed = true;
        
        while (changed) {
            changed = false;
            
            for (Production production : grammar.getProductions()) {
                changed |= updateFollowSetsForProduction(production);
            }
        }
    }
    
    /**
     * Updates FOLLOW sets based on a single production.
     * Returns true if any FOLLOW set was modified.
     */
    private boolean updateFollowSetsForProduction(Production production) {
        boolean changed = false;
        List<Symbol> rightSide = production.getRightSide();
        NonTerminal leftSide = production.getLeftSide();
        
        for (int i = 0; i < rightSide.size(); i++) {
            Symbol currentSymbol = rightSide.get(i);
            
            // Only compute FOLLOW sets for non-terminals
            if (!currentSymbol.isNonTerminal()) {
                continue;
            }
            
            NonTerminal currentNT = (NonTerminal) currentSymbol;
            Set<Terminal> currentFollow = followSets.get(currentNT);
            int oldSize = currentFollow.size();
            
            // Get the symbols that follow this non-terminal
            List<Symbol> beta = rightSide.subList(i + 1, rightSide.size());
            
            if (beta.isEmpty()) {
                // A ::= αB (B is at the end)
                // FOLLOW(B) includes FOLLOW(A)
                currentFollow.addAll(followSets.get(leftSide));
            } else {
                // A ::= αBβ
                // FOLLOW(B) includes FIRST(β)
                Set<Terminal> betaFirst = computeFirstOfRightHandSide(beta);
                currentFollow.addAll(betaFirst);
                
                // If β is nullable, FOLLOW(B) also includes FOLLOW(A)
                if (isSequenceNullable(beta)) {
                    currentFollow.addAll(followSets.get(leftSide));
                }
            }
            
            if (currentFollow.size() > oldSize) {
                changed = true;
            }
        }
        
        return changed;
    }
    
    /**
     * Checks if a sequence of symbols is nullable (all symbols are nullable).
     */
    private boolean isSequenceNullable(List<Symbol> symbols) {
        for (Symbol symbol : symbols) {
            if (!isNullable(symbol)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns a string representation of all computed sets for debugging.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FirstFollowCalculator Results:\n");
        
        sb.append("\nNullable symbols:\n");
        for (Symbol symbol : nullableSymbols) {
            sb.append("  ").append(symbol.getName()).append("\n");
        }
        
        sb.append("\nFIRST sets:\n");
        for (Map.Entry<Symbol, Set<Terminal>> entry : firstSets.entrySet()) {
            sb.append("  FIRST(").append(entry.getKey().getName()).append(") = {");
            boolean first = true;
            for (Terminal terminal : entry.getValue()) {
                if (!first) sb.append(", ");
                sb.append(terminal.getName());
                first = false;
            }
            sb.append("}\n");
        }
        
        sb.append("\nFOLLOW sets:\n");
        for (Map.Entry<NonTerminal, Set<Terminal>> entry : followSets.entrySet()) {
            sb.append("  FOLLOW(").append(entry.getKey().getName()).append(") = {");
            boolean first = true;
            for (Terminal terminal : entry.getValue()) {
                if (!first) sb.append(", ");
                sb.append(terminal.getName());
                first = false;
            }
            sb.append("}\n");
        }
        
        return sb.toString();
    }
}