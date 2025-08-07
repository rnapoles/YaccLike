package com.parsergenerator.lalr;

import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;

import java.util.*;

/**
 * Represents the complete LALR parsing tables (ACTION and GOTO tables).
 * The ACTION table determines what action to take for each (state, terminal) pair,
 * while the GOTO table determines the next state for each (state, non-terminal) pair.
 */
public class ParsingTable {
    private final Map<StateSymbolPair, Action> actionTable;
    private final Map<StateSymbolPair, Integer> gotoTable;
    private final List<LRItemSet> states;
    private final Set<Terminal> terminals;
    private final Set<NonTerminal> nonTerminals;

    /**
     * Constructs a new empty ParsingTable.
     */
    public ParsingTable() {
        this.actionTable = new HashMap<>();
        this.gotoTable = new HashMap<>();
        this.states = new ArrayList<>();
        this.terminals = new LinkedHashSet<>();
        this.nonTerminals = new LinkedHashSet<>();
    }

    /**
     * Constructs a new ParsingTable with the specified states and symbols.
     *
     * @param states the list of parser states
     * @param terminals the set of terminal symbols
     * @param nonTerminals the set of non-terminal symbols
     */
    public ParsingTable(List<LRItemSet> states, Set<Terminal> terminals, Set<NonTerminal> nonTerminals) {
        this.actionTable = new HashMap<>();
        this.gotoTable = new HashMap<>();
        this.states = new ArrayList<>(states);
        this.terminals = new LinkedHashSet<>(terminals);
        this.nonTerminals = new LinkedHashSet<>(nonTerminals);
    }

    /**
     * Sets an action in the ACTION table.
     *
     * @param state the state number
     * @param terminal the terminal symbol
     * @param action the action to set
     * @throws IllegalArgumentException if terminal is null or action is null
     */
    public void setAction(int state, Terminal terminal, Action action) {
        if (terminal == null) {
            throw new IllegalArgumentException("Terminal cannot be null");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        actionTable.put(new StateSymbolPair(state, terminal), action);
    }

    /**
     * Gets an action from the ACTION table.
     *
     * @param state the state number
     * @param terminal the terminal symbol
     * @return the action, or null if not found
     */
    public Action getAction(int state, Terminal terminal) {
        if (terminal == null) {
            return null;
        }
        return actionTable.get(new StateSymbolPair(state, terminal));
    }

    /**
     * Sets a GOTO entry in the GOTO table.
     *
     * @param state the current state number
     * @param nonTerminal the non-terminal symbol
     * @param nextState the next state number
     * @throws IllegalArgumentException if nonTerminal is null
     */
    public void setGoto(int state, NonTerminal nonTerminal, int nextState) {
        if (nonTerminal == null) {
            throw new IllegalArgumentException("Non-terminal cannot be null");
        }
        gotoTable.put(new StateSymbolPair(state, nonTerminal), nextState);
    }

    /**
     * Gets a GOTO entry from the GOTO table.
     *
     * @param state the current state number
     * @param nonTerminal the non-terminal symbol
     * @return the next state number, or null if not found
     */
    public Integer getGoto(int state, NonTerminal nonTerminal) {
        if (nonTerminal == null) {
            return null;
        }
        return gotoTable.get(new StateSymbolPair(state, nonTerminal));
    }

    /**
     * Checks if there is an action defined for the given state and terminal.
     *
     * @param state the state number
     * @param terminal the terminal symbol
     * @return true if an action is defined, false otherwise
     */
    public boolean hasAction(int state, Terminal terminal) {
        return getAction(state, terminal) != null;
    }

    /**
     * Checks if there is a GOTO entry defined for the given state and non-terminal.
     *
     * @param state the state number
     * @param nonTerminal the non-terminal symbol
     * @return true if a GOTO entry is defined, false otherwise
     */
    public boolean hasGoto(int state, NonTerminal nonTerminal) {
        return getGoto(state, nonTerminal) != null;
    }

    /**
     * Returns an unmodifiable view of the ACTION table.
     *
     * @return the ACTION table
     */
    public Map<StateSymbolPair, Action> getActionTable() {
        return Collections.unmodifiableMap(actionTable);
    }

    /**
     * Returns an unmodifiable view of the GOTO table.
     *
     * @return the GOTO table
     */
    public Map<StateSymbolPair, Integer> getGotoTable() {
        return Collections.unmodifiableMap(gotoTable);
    }

    /**
     * Returns an unmodifiable view of the parser states.
     *
     * @return the list of states
     */
    public List<LRItemSet> getStates() {
        return Collections.unmodifiableList(states);
    }

    /**
     * Returns the state at the specified index.
     *
     * @param index the state index
     * @return the state at the index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public LRItemSet getState(int index) {
        return states.get(index);
    }

    /**
     * Returns the number of states in the parsing table.
     *
     * @return the number of states
     */
    public int getStateCount() {
        return states.size();
    }

    /**
     * Returns an unmodifiable view of the terminal symbols.
     *
     * @return the set of terminals
     */
    public Set<Terminal> getTerminals() {
        return Collections.unmodifiableSet(terminals);
    }

    /**
     * Returns an unmodifiable view of the non-terminal symbols.
     *
     * @return the set of non-terminals
     */
    public Set<NonTerminal> getNonTerminals() {
        return Collections.unmodifiableSet(nonTerminals);
    }

    /**
     * Adds a state to the parsing table.
     *
     * @param state the state to add
     * @return the index of the added state
     */
    public int addState(LRItemSet state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        states.add(state);
        return states.size() - 1;
    }

    /**
     * Adds a terminal symbol to the table.
     *
     * @param terminal the terminal to add
     */
    public void addTerminal(Terminal terminal) {
        if (terminal != null) {
            terminals.add(terminal);
        }
    }

    /**
     * Adds a non-terminal symbol to the table.
     *
     * @param nonTerminal the non-terminal to add
     */
    public void addNonTerminal(NonTerminal nonTerminal) {
        if (nonTerminal != null) {
            nonTerminals.add(nonTerminal);
        }
    }

    /**
     * Detects conflicts in the parsing table.
     * This method analyzes the ACTION table to identify shift/reduce and reduce/reduce conflicts.
     * Note: Since our implementation overwrites conflicting actions, this method detects
     * potential conflicts by analyzing the structure, but actual conflicts would have been
     * resolved during table construction.
     *
     * @return a list of conflict descriptions
     */
    public List<String> detectConflicts() {
        List<String> conflicts = new ArrayList<>();
        
        // Since our current implementation overwrites conflicts, we need to detect them
        // during table construction. This method serves as a validation check.
        
        // Check for potential conflicts by analyzing the grammar structure
        // and the resulting ACTION table entries
        
        // Group actions by state and terminal to look for inconsistencies
        Map<Integer, Map<Terminal, List<String>>> stateActions = new HashMap<>();
        
        for (Map.Entry<StateSymbolPair, Action> entry : actionTable.entrySet()) {
            StateSymbolPair key = entry.getKey();
            Action action = entry.getValue();
            
            if (key.getSymbol().isTerminal()) {
                Terminal terminal = (Terminal) key.getSymbol();
                int state = key.getState();
                
                stateActions.computeIfAbsent(state, k -> new HashMap<>())
                           .computeIfAbsent(terminal, k -> new ArrayList<>())
                           .add(action.toDetailedString());
            }
        }
        
        // Look for states where multiple types of actions could apply
        for (int stateIndex = 0; stateIndex < states.size(); stateIndex++) {
            LRItemSet state = states.get(stateIndex);
            
            // Analyze items in this state for potential conflicts
            Set<Terminal> shiftTerminals = new HashSet<>();
            Map<Terminal, List<Production>> reduceTerminals = new HashMap<>();
            
            for (LRItem item : state.getItems()) {
                if (item.canAdvance() && item.getNextSymbol().isTerminal()) {
                    // This item can shift on a terminal
                    shiftTerminals.add((Terminal) item.getNextSymbol());
                } else if (item.isComplete()) {
                    // This item can reduce
                    Set<Terminal> lookaheads = item.getLookaheads();
                    if (lookaheads.isEmpty()) {
                        // Use FOLLOW set if no lookaheads
                        // This would require access to grammar, which we don't have here
                        // So we'll skip this case for now
                        continue;
                    }
                    
                    for (Terminal lookahead : lookaheads) {
                        reduceTerminals.computeIfAbsent(lookahead, k -> new ArrayList<>())
                                     .add(item.getProduction());
                    }
                }
            }
            
            // Check for shift/reduce conflicts
            for (Terminal terminal : shiftTerminals) {
                if (reduceTerminals.containsKey(terminal)) {
                    List<Production> reductions = reduceTerminals.get(terminal);
                    for (Production production : reductions) {
                        conflicts.add(String.format(
                            "Potential shift/reduce conflict in state %d on terminal '%s': " +
                            "can shift or reduce by production %s",
                            stateIndex, terminal.getName(), production.toString()
                        ));
                    }
                }
            }
            
            // Check for reduce/reduce conflicts
            for (Map.Entry<Terminal, List<Production>> entry : reduceTerminals.entrySet()) {
                List<Production> productions = entry.getValue();
                if (productions.size() > 1) {
                    Terminal terminal = entry.getKey();
                    StringBuilder conflictMsg = new StringBuilder();
                    conflictMsg.append(String.format(
                        "Reduce/reduce conflict in state %d on terminal '%s': can reduce by ",
                        stateIndex, terminal.getName()
                    ));
                    
                    for (int i = 0; i < productions.size(); i++) {
                        if (i > 0) conflictMsg.append(" or ");
                        conflictMsg.append(productions.get(i).toString());
                    }
                    
                    conflicts.add(conflictMsg.toString());
                }
            }
        }
        
        return conflicts;
    }

    /**
     * Checks if the parsing table has any conflicts.
     *
     * @return true if there are conflicts, false otherwise
     */
    public boolean hasConflicts() {
        return !detectConflicts().isEmpty();
    }

    /**
     * Returns a string representation of the ACTION table.
     *
     * @return ACTION table as string
     */
    public String actionTableToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ACTION Table:\n");
        sb.append("State\t");
        
        // Header with terminals
        for (Terminal terminal : terminals) {
            sb.append(terminal.getName()).append("\t");
        }
        sb.append("\n");
        
        // Rows for each state
        for (int i = 0; i < states.size(); i++) {
            sb.append(i).append("\t");
            for (Terminal terminal : terminals) {
                Action action = getAction(i, terminal);
                if (action != null) {
                    sb.append(action.toString());
                } else {
                    sb.append("-");
                }
                sb.append("\t");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }

    /**
     * Returns a string representation of the GOTO table.
     *
     * @return GOTO table as string
     */
    public String gotoTableToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GOTO Table:\n");
        sb.append("State\t");
        
        // Header with non-terminals
        for (NonTerminal nonTerminal : nonTerminals) {
            sb.append(nonTerminal.getName()).append("\t");
        }
        sb.append("\n");
        
        // Rows for each state
        for (int i = 0; i < states.size(); i++) {
            sb.append(i).append("\t");
            for (NonTerminal nonTerminal : nonTerminals) {
                Integer nextState = getGoto(i, nonTerminal);
                if (nextState != null) {
                    sb.append(nextState);
                } else {
                    sb.append("-");
                }
                sb.append("\t");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }

    /**
     * Compresses the parsing table by removing redundant entries.
     * This method identifies and removes entries that are not necessary for parsing,
     * such as error entries that can be inferred from missing entries.
     * 
     * @return the number of entries removed during compression
     */
    public int compressTable() {
        int removedEntries = 0;
        
        // Remove explicit error actions since they can be inferred from missing entries
        Iterator<Map.Entry<StateSymbolPair, Action>> actionIter = actionTable.entrySet().iterator();
        while (actionIter.hasNext()) {
            Map.Entry<StateSymbolPair, Action> entry = actionIter.next();
            if (entry.getValue().isError()) {
                actionIter.remove();
                removedEntries++;
            }
        }
        
        // Remove redundant GOTO entries for states that don't need them
        // (This is more complex and depends on the specific grammar structure)
        removedEntries += removeRedundantGotoEntries();
        
        return removedEntries;
    }
    
    /**
     * Removes redundant GOTO entries that are not reachable or necessary.
     * This is a conservative implementation that only removes entries that are
     * clearly unreachable based on the state structure.
     * 
     * @return the number of GOTO entries removed
     */
    private int removeRedundantGotoEntries() {
        int removedEntries = 0;
        
        // For now, we'll be very conservative and not remove GOTO entries
        // unless we have more sophisticated analysis of the grammar structure.
        // This prevents removing entries that might be needed for parsing.
        
        // In a more complete implementation, we would:
        // 1. Analyze the LR items in each state to determine which GOTO entries are actually needed
        // 2. Check for states that are unreachable from the initial state
        // 3. Remove GOTO entries that point to unreachable states
        
        // For this implementation, we'll only remove GOTO entries that point to invalid states
        Iterator<Map.Entry<StateSymbolPair, Integer>> gotoIter = gotoTable.entrySet().iterator();
        while (gotoIter.hasNext()) {
            Map.Entry<StateSymbolPair, Integer> entry = gotoIter.next();
            Integer targetState = entry.getValue();
            
            // Remove entries that point to invalid states
            if (targetState < 0 || targetState >= states.size()) {
                gotoIter.remove();
                removedEntries++;
            }
        }
        
        return removedEntries;
    }
    
    /**
     * Validates the completeness and correctness of the parsing table.
     * This method performs comprehensive validation including:
     * - Checking that all necessary ACTION entries are present
     * - Verifying GOTO table completeness
     * - Validating state transitions
     * - Checking for unreachable states
     * 
     * @return a validation result containing any issues found
     */
    public ValidationResult validateTable() {
        ValidationResult result = new ValidationResult();
        
        // Validate ACTION table completeness
        validateActionTableCompleteness(result);
        
        // Validate GOTO table completeness
        validateGotoTableCompleteness(result);
        
        // Validate state transitions
        validateStateTransitions(result);
        
        // Check for unreachable states
        validateStateReachability(result);
        
        // Validate table consistency
        validateTableConsistency(result);
        
        return result;
    }
    
    /**
     * Validates that the ACTION table has all necessary entries.
     */
    private void validateActionTableCompleteness(ValidationResult result) {
        for (int stateIndex = 0; stateIndex < states.size(); stateIndex++) {
            LRItemSet state = states.get(stateIndex);
            
            // Check that all terminals that can appear in this state have actions
            Set<Terminal> expectedTerminals = getExpectedTerminalsForState(state);
            
            for (Terminal terminal : expectedTerminals) {
                Action action = getAction(stateIndex, terminal);
                if (action == null) {
                    result.addError(String.format(
                        "Missing ACTION entry for state %d, terminal '%s'",
                        stateIndex, terminal.getName()
                    ));
                }
            }
        }
    }
    
    /**
     * Gets the set of terminals that should have actions defined for a given state.
     */
    private Set<Terminal> getExpectedTerminalsForState(LRItemSet state) {
        Set<Terminal> expectedTerminals = new HashSet<>();
        
        for (LRItem item : state.getItems()) {
            if (item.canAdvance() && item.getNextSymbol().isTerminal()) {
                // This terminal should have a shift action
                expectedTerminals.add((Terminal) item.getNextSymbol());
            } else if (item.isComplete()) {
                // This item can reduce - add its lookahead terminals
                expectedTerminals.addAll(item.getLookaheads());
            }
        }
        
        return expectedTerminals;
    }
    
    /**
     * Validates that the GOTO table has all necessary entries.
     */
    private void validateGotoTableCompleteness(ValidationResult result) {
        for (int stateIndex = 0; stateIndex < states.size(); stateIndex++) {
            LRItemSet state = states.get(stateIndex);
            
            for (LRItem item : state.getItems()) {
                if (item.canAdvance() && item.getNextSymbol().isNonTerminal()) {
                    NonTerminal nonTerminal = (NonTerminal) item.getNextSymbol();
                    Integer gotoState = getGoto(stateIndex, nonTerminal);
                    
                    if (gotoState == null) {
                        result.addError(String.format(
                            "Missing GOTO entry for state %d, non-terminal '%s'",
                            stateIndex, nonTerminal.getName()
                        ));
                    } else if (gotoState < 0 || gotoState >= states.size()) {
                        result.addError(String.format(
                            "Invalid GOTO entry for state %d, non-terminal '%s': points to invalid state %d",
                            stateIndex, nonTerminal.getName(), gotoState
                        ));
                    }
                }
            }
        }
    }
    
    /**
     * Validates that state transitions are consistent.
     */
    private void validateStateTransitions(ValidationResult result) {
        for (Map.Entry<StateSymbolPair, Action> entry : actionTable.entrySet()) {
            StateSymbolPair key = entry.getKey();
            Action action = entry.getValue();
            
            if (action.isShift()) {
                int targetState = action.getParameter();
                if (targetState < 0 || targetState >= states.size()) {
                    result.addError(String.format(
                        "SHIFT action in state %d for terminal '%s' points to invalid state %d",
                        key.getState(), key.getSymbol().getName(), targetState
                    ));
                }
            }
        }
        
        for (Map.Entry<StateSymbolPair, Integer> entry : gotoTable.entrySet()) {
            StateSymbolPair key = entry.getKey();
            Integer targetState = entry.getValue();
            
            if (targetState < 0 || targetState >= states.size()) {
                result.addError(String.format(
                    "GOTO entry in state %d for non-terminal '%s' points to invalid state %d",
                    key.getState(), key.getSymbol().getName(), targetState
                ));
            }
        }
    }
    
    /**
     * Validates that all states are reachable from the initial state.
     */
    private void validateStateReachability(ValidationResult result) {
        if (states.isEmpty()) {
            result.addError("Parsing table has no states");
            return;
        }
        
        Set<Integer> reachableStates = new HashSet<>();
        Queue<Integer> worklist = new LinkedList<>();
        
        // Start from state 0 (initial state)
        reachableStates.add(0);
        worklist.add(0);
        
        while (!worklist.isEmpty()) {
            int currentState = worklist.poll();
            
            // Check ACTION table for reachable states
            for (Map.Entry<StateSymbolPair, Action> entry : actionTable.entrySet()) {
                if (entry.getKey().getState() == currentState && entry.getValue().isShift()) {
                    int targetState = entry.getValue().getParameter();
                    if (!reachableStates.contains(targetState)) {
                        reachableStates.add(targetState);
                        worklist.add(targetState);
                    }
                }
            }
            
            // Check GOTO table for reachable states
            for (Map.Entry<StateSymbolPair, Integer> entry : gotoTable.entrySet()) {
                if (entry.getKey().getState() == currentState) {
                    int targetState = entry.getValue();
                    if (!reachableStates.contains(targetState)) {
                        reachableStates.add(targetState);
                        worklist.add(targetState);
                    }
                }
            }
        }
        
        // Report unreachable states
        for (int i = 0; i < states.size(); i++) {
            if (!reachableStates.contains(i)) {
                result.addWarning(String.format("State %d is unreachable", i));
            }
        }
    }
    
    /**
     * Validates internal consistency of the parsing table.
     */
    private void validateTableConsistency(ValidationResult result) {
        // Check that all referenced productions exist
        for (Action action : actionTable.values()) {
            if (action.isReduce()) {
                Production production = action.getProduction();
                if (production == null) {
                    result.addError(String.format(
                        "REDUCE action references null production (ID: %d)",
                        action.getParameter()
                    ));
                }
            }
        }
        
        // Check that state indices are consistent
        for (StateSymbolPair key : actionTable.keySet()) {
            if (key.getState() >= states.size()) {
                result.addError(String.format(
                    "ACTION table references invalid state %d (max: %d)",
                    key.getState(), states.size() - 1
                ));
            }
        }
        
        for (StateSymbolPair key : gotoTable.keySet()) {
            if (key.getState() >= states.size()) {
                result.addError(String.format(
                    "GOTO table references invalid state %d (max: %d)",
                    key.getState(), states.size() - 1
                ));
            }
        }
    }
    
    /**
     * Serializes the parsing table to a compact string representation.
     * This format can be used for debugging or storing the table.
     * 
     * @return a serialized representation of the table
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        
        // Header with metadata
        sb.append("LALR_TABLE_V1\n");
        sb.append("STATES=").append(states.size()).append("\n");
        sb.append("TERMINALS=").append(terminals.size()).append("\n");
        sb.append("NONTERMINALS=").append(nonTerminals.size()).append("\n");
        sb.append("ACTION_ENTRIES=").append(actionTable.size()).append("\n");
        sb.append("GOTO_ENTRIES=").append(gotoTable.size()).append("\n");
        sb.append("\n");
        
        // Terminal symbols
        sb.append("TERMINALS:\n");
        for (Terminal terminal : terminals) {
            sb.append(terminal.getName()).append("\n");
        }
        sb.append("\n");
        
        // Non-terminal symbols
        sb.append("NONTERMINALS:\n");
        for (NonTerminal nonTerminal : nonTerminals) {
            sb.append(nonTerminal.getName()).append("\n");
        }
        sb.append("\n");
        
        // ACTION table
        sb.append("ACTION:\n");
        for (Map.Entry<StateSymbolPair, Action> entry : actionTable.entrySet()) {
            StateSymbolPair key = entry.getKey();
            Action action = entry.getValue();
            sb.append(key.getState()).append(",")
              .append(key.getSymbol().getName()).append(",")
              .append(action.getType().name()).append(",")
              .append(action.getParameter()).append("\n");
        }
        sb.append("\n");
        
        // GOTO table
        sb.append("GOTO:\n");
        for (Map.Entry<StateSymbolPair, Integer> entry : gotoTable.entrySet()) {
            StateSymbolPair key = entry.getKey();
            Integer targetState = entry.getValue();
            sb.append(key.getState()).append(",")
              .append(key.getSymbol().getName()).append(",")
              .append(targetState).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Creates a detailed debugging representation of the parsing table.
     * This includes state information, item sets, and transition details.
     * 
     * @return a detailed debugging string
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== LALR PARSING TABLE DEBUG INFO ===\n\n");
        
        // Table statistics
        sb.append("Table Statistics:\n");
        sb.append("  States: ").append(states.size()).append("\n");
        sb.append("  Terminals: ").append(terminals.size()).append("\n");
        sb.append("  Non-terminals: ").append(nonTerminals.size()).append("\n");
        sb.append("  ACTION entries: ").append(actionTable.size()).append("\n");
        sb.append("  GOTO entries: ").append(gotoTable.size()).append("\n");
        
        // Validation results
        ValidationResult validation = validateTable();
        sb.append("  Validation errors: ").append(validation.getErrors().size()).append("\n");
        sb.append("  Validation warnings: ").append(validation.getWarnings().size()).append("\n");
        sb.append("\n");
        
        // Conflicts
        List<String> conflicts = detectConflicts();
        if (!conflicts.isEmpty()) {
            sb.append("Conflicts:\n");
            for (String conflict : conflicts) {
                sb.append("  ").append(conflict).append("\n");
            }
            sb.append("\n");
        }
        
        // Validation issues
        if (!validation.getErrors().isEmpty()) {
            sb.append("Validation Errors:\n");
            for (String error : validation.getErrors()) {
                sb.append("  ERROR: ").append(error).append("\n");
            }
            sb.append("\n");
        }
        
        if (!validation.getWarnings().isEmpty()) {
            sb.append("Validation Warnings:\n");
            for (String warning : validation.getWarnings()) {
                sb.append("  WARNING: ").append(warning).append("\n");
            }
            sb.append("\n");
        }
        
        // Performance metrics
        Map<String, Object> metrics = getPerformanceMetrics();
        sb.append("Performance Metrics:\n");
        sb.append("  ACTION table density: ").append(String.format("%.2f%%", (Double) metrics.get("actionTableDensity") * 100)).append("\n");
        sb.append("  GOTO table density: ").append(String.format("%.2f%%", (Double) metrics.get("gotoTableDensity") * 100)).append("\n");
        sb.append("  Overall density: ").append(String.format("%.2f%%", (Double) metrics.get("overallDensity") * 100)).append("\n");
        sb.append("  Estimated memory: ").append(metrics.get("estimatedMemoryBytes")).append(" bytes\n");
        sb.append("\n");
        
        // State details
        sb.append("State Details:\n");
        for (int i = 0; i < Math.min(states.size(), 10); i++) { // Show first 10 states
            LRItemSet state = states.get(i);
            sb.append("  State ").append(i).append(":\n");
            
            // Show items in this state
            for (LRItem item : state.getItems()) {
                sb.append("    ").append(item.toString()).append("\n");
            }
            
            // Show actions for this state
            boolean hasActions = false;
            for (Terminal terminal : terminals) {
                Action action = getAction(i, terminal);
                if (action != null) {
                    if (!hasActions) {
                        sb.append("    Actions: ");
                        hasActions = true;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(terminal.getName()).append("->").append(action.toString());
                }
            }
            if (hasActions) {
                sb.append("\n");
            }
            
            // Show gotos for this state
            boolean hasGotos = false;
            for (NonTerminal nonTerminal : nonTerminals) {
                Integer gotoState = getGoto(i, nonTerminal);
                if (gotoState != null) {
                    if (!hasGotos) {
                        sb.append("    Gotos: ");
                        hasGotos = true;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(nonTerminal.getName()).append("->").append(gotoState);
                }
            }
            if (hasGotos) {
                sb.append("\n");
            }
            
            sb.append("\n");
        }
        
        if (states.size() > 10) {
            sb.append("  ... (").append(states.size() - 10).append(" more states)\n\n");
        }
        
        // Table summaries
        sb.append("ACTION Table Summary:\n");
        sb.append(actionTableToString()).append("\n");
        
        sb.append("GOTO Table Summary:\n");
        sb.append(gotoTableToString()).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Computes performance metrics for the parsing table.
     * This includes density metrics, memory usage estimates, and efficiency measures.
     * 
     * @return a map of performance metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Basic size metrics
        metrics.put("stateCount", states.size());
        metrics.put("terminalCount", terminals.size());
        metrics.put("nonTerminalCount", nonTerminals.size());
        metrics.put("actionEntries", actionTable.size());
        metrics.put("gotoEntries", gotoTable.size());
        
        // Density metrics
        int maxActionEntries = states.size() * terminals.size();
        int maxGotoEntries = states.size() * nonTerminals.size();
        double actionDensity = maxActionEntries > 0 ? (double) actionTable.size() / maxActionEntries : 0.0;
        double gotoDensity = maxGotoEntries > 0 ? (double) gotoTable.size() / maxGotoEntries : 0.0;
        
        metrics.put("actionTableDensity", actionDensity);
        metrics.put("gotoTableDensity", gotoDensity);
        metrics.put("overallDensity", 
                   (double) (actionTable.size() + gotoTable.size()) / (maxActionEntries + maxGotoEntries));
        
        // Conflict metrics
        List<String> conflicts = detectConflicts();
        metrics.put("conflictCount", conflicts.size());
        
        // Validation metrics
        ValidationResult validation = validateTable();
        metrics.put("validationErrors", validation.getErrors().size());
        metrics.put("validationWarnings", validation.getWarnings().size());
        
        // Memory usage estimation (rough)
        long estimatedMemory = 0;
        estimatedMemory += actionTable.size() * 64; // Rough estimate for StateSymbolPair + Action
        estimatedMemory += gotoTable.size() * 48;   // Rough estimate for StateSymbolPair + Integer
        estimatedMemory += states.size() * 256;     // Rough estimate for LRItemSet
        metrics.put("estimatedMemoryBytes", estimatedMemory);
        
        return metrics;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ParsingTable {\n");
        sb.append("  States: ").append(states.size()).append("\n");
        sb.append("  Terminals: ").append(terminals.size()).append("\n");
        sb.append("  Non-terminals: ").append(nonTerminals.size()).append("\n");
        sb.append("  ACTION entries: ").append(actionTable.size()).append("\n");
        sb.append("  GOTO entries: ").append(gotoTable.size()).append("\n");
        
        List<String> conflicts = detectConflicts();
        if (!conflicts.isEmpty()) {
            sb.append("  Conflicts: ").append(conflicts.size()).append("\n");
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Returns the number of entries in the ACTION table.
     * 
     * @return the number of ACTION table entries
     */
    public int getActionEntryCount() {
        return actionTable.size();
    }
    
    /**
     * Returns the number of entries in the GOTO table.
     * 
     * @return the number of GOTO table entries
     */
    public int getGotoEntryCount() {
        return gotoTable.size();
    }
}