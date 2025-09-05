package com.parsergenerator.lalr;

import com.parsergenerator.analysis.AnalysisResult;
import com.parsergenerator.error.ErrorReporter;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;

import java.util.*;

/**
 * Constructs LALR parsing tables from a context-free grammar.
 * This class implements the LALR table construction algorithm, which includes:
 * 1. Building the LR(0) automaton
 * 2. Merging compatible states for LALR optimization
 * 3. Constructing ACTION and GOTO tables
 */
public class LALRTableBuilder {
    private final Grammar grammar;
    private final AnalysisResult analysisResult;
    private final ErrorReporter errorReporter;
    private final boolean throwOnConflicts;
    private final List<LRItemSet> lr0States;
    private final Map<LRItemSet, Integer> stateMap;
    private final Map<Integer, Map<Symbol, Integer>> transitions;

    // Special end-of-input terminal
    private final Terminal endOfInput;

    /**
     * Constructs a new LALRTableBuilder for the given grammar and analysis result.
     *
     * @param grammar        the grammar to build tables for
     * @param analysisResult the result of grammar analysis
     * @throws IllegalArgumentException if grammar or analysisResult is null
     */
    public LALRTableBuilder(Grammar grammar, AnalysisResult analysisResult) {
        this(grammar, analysisResult, new ErrorReporter(), true);
    }

    /**
     * Constructs a new LALRTableBuilder with the specified error reporter.
     *
     * @param grammar        the grammar to build tables for
     * @param analysisResult the result of grammar analysis
     * @param errorReporter  the error reporter to use
     * @throws IllegalArgumentException if grammar or analysisResult is null
     */
    public LALRTableBuilder(Grammar grammar, AnalysisResult analysisResult, ErrorReporter errorReporter) {
        this(grammar, analysisResult, errorReporter, true);
    }

    /**
     * Constructs a new LALRTableBuilder with the specified error reporter and conflict handling behavior.
     *
     * @param grammar        the grammar to build tables for
     * @param analysisResult the result of grammar analysis
     * @param errorReporter  the error reporter to use
     * @param throwOnConflicts whether to throw exceptions when conflicts are detected
     * @throws IllegalArgumentException if grammar or analysisResult is null
     */
    public LALRTableBuilder(Grammar grammar, AnalysisResult analysisResult, ErrorReporter errorReporter, boolean throwOnConflicts) {
        if (grammar == null) {
            throw new IllegalArgumentException("Grammar cannot be null");
        }
        if (analysisResult == null) {
            throw new IllegalArgumentException("Analysis result cannot be null");
        }
        if (!grammar.hasStartSymbol()) {
            throw new IllegalArgumentException("Grammar must have a start symbol");
        }

        this.grammar = grammar;
        this.analysisResult = analysisResult;
        this.errorReporter = errorReporter != null ? errorReporter : new ErrorReporter();
        this.throwOnConflicts = throwOnConflicts;
        this.lr0States = new ArrayList<>();
        this.stateMap = new HashMap<>();
        this.transitions = new HashMap<>();
        this.endOfInput = new Terminal("$", "\\$");
    }

    /**
     * Builds the complete LALR parsing table.
     *
     * @return the constructed parsing table
     * @throws IllegalStateException if the grammar has analysis errors
     */
    public ParsingTable buildTable() {
        int initialErrorCount = errorReporter.getErrorCount();

        if (analysisResult.hasErrors()) {
            String message = "Cannot build parsing table: grammar has analysis errors";
            String context = String.format("Analysis found %d errors: %s",
                    analysisResult.getErrors().size(),
                    analysisResult.getErrors().toString());
            errorReporter.reportError(ErrorReporter.Severity.ERROR,
                    ErrorReporter.ErrorType.LALR_CONFLICT,
                    message,
                    null,
                    context,
                    "Fix all grammar analysis errors before attempting LALR table construction");
            throw new IllegalStateException(message);
        }

        try {
            errorReporter.reportInfo(ErrorReporter.ErrorType.LALR_CONFLICT,
                    String.format("Starting LALR table construction for grammar with %d productions",
                            grammar.getProductions().size()));

            // Step 1: Build LR(0) automaton
            errorReporter.reportInfo(ErrorReporter.ErrorType.LALR_CONFLICT, "Building LR(0) automaton");
            buildLR0Automaton();
            errorReporter.reportInfo(ErrorReporter.ErrorType.LALR_CONFLICT,
                    String.format("Built LR(0) automaton with %d states", lr0States.size()));

            // Step 2: Merge compatible states for LALR
            errorReporter.reportInfo(ErrorReporter.ErrorType.LALR_CONFLICT,
                    "Merging compatible states for LALR optimization");
            int statesBeforeMerging = lr0States.size();
            mergeLALRStates();
            int statesAfterMerging = lr0States.size();
            errorReporter.reportInfo(ErrorReporter.ErrorType.LALR_CONFLICT,
                    String.format("LALR state merging completed: %d -> %d states (saved %d states)",
                            statesBeforeMerging, statesAfterMerging, statesBeforeMerging - statesAfterMerging));

            // Step 3: Build ACTION and GOTO tables
            errorReporter.reportInfo(ErrorReporter.ErrorType.LALR_CONFLICT, "Building ACTION and GOTO tables");
            ParsingTable table = new ParsingTable(lr0States, getTerminalsWithEndMarker(), grammar.getNonTerminals());
            buildActionTable(table);
            buildGotoTable(table);

            // Check for conflicts and report them
            int conflicts = detectAndReportConflicts(table);

            int newErrors = errorReporter.getErrorCount() - initialErrorCount;
            if (newErrors > 0) {
                if (throwOnConflicts) {
                    errorReporter.reportError(ErrorReporter.Severity.ERROR,
                            ErrorReporter.ErrorType.LALR_CONFLICT,
                            String.format("LALR table construction failed with %d conflicts", conflicts),
                            null,
                            "Grammar is not LALR(1) compatible",
                            "Modify grammar to eliminate conflicts or use a more powerful parsing technique");
                    throw new IllegalStateException("Grammar has LALR conflicts - cannot generate parser");
                } else {
                    errorReporter.reportWarning(ErrorReporter.ErrorType.LALR_CONFLICT,
                            String.format("LALR table construction completed with %d conflicts", conflicts),
                            null,
                            "Grammar is not LALR(1) compatible - conflicts may affect parsing accuracy");
                    errorReporter.reportInfo(ErrorReporter.ErrorType.LALR_CONFLICT,
                            String.format(
                                    "LALR table construction completed with conflicts - %d states, %d ACTION entries, %d GOTO entries",
                                    table.getStateCount(), table.getActionEntryCount(), table.getGotoEntryCount()));
                }
            } else {
                errorReporter.reportInfo(ErrorReporter.ErrorType.LALR_CONFLICT,
                        String.format(
                                "LALR table construction completed successfully - %d states, %d ACTION entries, %d GOTO entries",
                                table.getStateCount(), table.getActionEntryCount(), table.getGotoEntryCount()));
            }

            return table;

        } catch (IllegalStateException e) {
            // Re-throw IllegalStateException as-is
            throw e;
        } catch (Exception e) {
            String message = "Unexpected error during LALR table construction: " + e.getMessage();
            errorReporter.reportError(ErrorReporter.Severity.ERROR,
                    ErrorReporter.ErrorType.INTERNAL,
                    message,
                    null,
                    "Exception: " + e.getClass().getSimpleName() + " at " + e.getStackTrace()[0],
                    "Check grammar structure and try again");
            throw new IllegalStateException(message, e);
        }
    }

    /**
     * Builds the LR(0) automaton by constructing the canonical collection of LR(0)
     * item sets.
     */
    private void buildLR0Automaton() {
        // Create augmented start production: S' -> S $
        NonTerminal augmentedStart = new NonTerminal("S'");
        Production augmentedProduction = new Production(augmentedStart, grammar.getStartSymbol(), endOfInput);

        // Create initial state with the augmented start item
        LRItem initialItem = new LRItem(augmentedProduction, 0);
        LRItemSet initialState = new LRItemSet();
        initialState.addItem(initialItem);

        // Compute closure of initial state
        initialState = initialState.closure(grammar);

        // Add initial state to the collection
        lr0States.add(initialState);
        stateMap.put(initialState, 0);
        transitions.put(0, new HashMap<>());

        // Build the automaton using a worklist algorithm
        Queue<Integer> worklist = new LinkedList<>();
        worklist.add(0);

        while (!worklist.isEmpty()) {
            int currentStateIndex = worklist.poll();
            LRItemSet currentState = lr0States.get(currentStateIndex);

            // Get all symbols that can be used with GOTO from this state
            Set<Symbol> gotoSymbols = currentState.getGotoSymbols();

            for (Symbol symbol : gotoSymbols) {
                // Compute GOTO(currentState, symbol)
                LRItemSet gotoState = currentState.gotoSet(symbol, grammar);

                if (!gotoState.isEmpty()) {
                    // Check if this state already exists
                    Integer existingStateIndex = findExistingState(gotoState);

                    if (existingStateIndex == null) {
                        // New state - add it to the collection
                        int newStateIndex = lr0States.size();
                        lr0States.add(gotoState);
                        stateMap.put(gotoState, newStateIndex);
                        transitions.put(newStateIndex, new HashMap<>());

                        // Add transition
                        transitions.get(currentStateIndex).put(symbol, newStateIndex);

                        // Add to worklist for further processing
                        worklist.add(newStateIndex);
                    } else {
                        // Existing state - just add transition
                        transitions.get(currentStateIndex).put(symbol, existingStateIndex);
                    }
                }
            }
        }
    }

    /**
     * Finds an existing state that matches the given state (same core).
     *
     * @param state the state to find
     * @return the index of the existing state, or null if not found
     */
    private Integer findExistingState(LRItemSet state) {
        for (Map.Entry<LRItemSet, Integer> entry : stateMap.entrySet()) {
            if (entry.getKey().hasSameCore(state)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Merges compatible states for LALR optimization.
     * States with the same core (same items ignoring lookaheads) are merged.
     */
    private void mergeLALRStates() {
        // Group states by their cores
        Map<LRItemSet, List<Integer>> coreGroups = new HashMap<>();

        for (int i = 0; i < lr0States.size(); i++) {
            LRItemSet state = lr0States.get(i);
            LRItemSet core = state.getCore();

            coreGroups.computeIfAbsent(core, k -> new ArrayList<>()).add(i);
        }

        // Merge states with the same core
        for (List<Integer> stateIndices : coreGroups.values()) {
            if (stateIndices.size() > 1) {
                // Merge all states in this group into the first one
                int primaryStateIndex = stateIndices.get(0);
                LRItemSet primaryState = lr0States.get(primaryStateIndex);

                for (int i = 1; i < stateIndices.size(); i++) {
                    int secondaryStateIndex = stateIndices.get(i);
                    LRItemSet secondaryState = lr0States.get(secondaryStateIndex);

                    // Merge lookaheads
                    primaryState.mergeLookaheads(secondaryState);

                    // Update transitions to point to the primary state
                    updateTransitionsAfterMerge(secondaryStateIndex, primaryStateIndex);

                    // Mark secondary state for removal
                    lr0States.set(secondaryStateIndex, null);
                }
            }
        }

        // Remove null states and update indices
        compactStates();
    }

    /**
     * Updates all transitions that point to the old state to point to the new
     * state.
     *
     * @param oldStateIndex the old state index
     * @param newStateIndex the new state index
     */
    private void updateTransitionsAfterMerge(int oldStateIndex, int newStateIndex) {
        for (Map<Symbol, Integer> stateTransitions : transitions.values()) {
            for (Map.Entry<Symbol, Integer> transition : stateTransitions.entrySet()) {
                if (transition.getValue().equals(oldStateIndex)) {
                    transition.setValue(newStateIndex);
                }
            }
        }
    }

    /**
     * Removes null states from the state list and updates all indices accordingly.
     */
    private void compactStates() {
        List<LRItemSet> compactedStates = new ArrayList<>();
        Map<Integer, Integer> indexMapping = new HashMap<>();

        // Build the compacted state list and index mapping
        for (int i = 0; i < lr0States.size(); i++) {
            LRItemSet state = lr0States.get(i);
            if (state != null) {
                indexMapping.put(i, compactedStates.size());
                compactedStates.add(state);
            }
        }

        // Update the state list
        lr0States.clear();
        lr0States.addAll(compactedStates);

        // Update state map
        stateMap.clear();
        for (int i = 0; i < lr0States.size(); i++) {
            stateMap.put(lr0States.get(i), i);
        }

        // Update transitions with new indices
        Map<Integer, Map<Symbol, Integer>> newTransitions = new HashMap<>();
        for (Map.Entry<Integer, Map<Symbol, Integer>> entry : transitions.entrySet()) {
            Integer oldFromState = entry.getKey();
            Integer newFromState = indexMapping.get(oldFromState);

            if (newFromState != null) {
                Map<Symbol, Integer> newStateTransitions = new HashMap<>();
                for (Map.Entry<Symbol, Integer> transition : entry.getValue().entrySet()) {
                    Symbol symbol = transition.getKey();
                    Integer oldToState = transition.getValue();
                    Integer newToState = indexMapping.get(oldToState);

                    if (newToState != null) {
                        newStateTransitions.put(symbol, newToState);
                    }
                }
                newTransitions.put(newFromState, newStateTransitions);
            }
        }

        transitions.clear();
        transitions.putAll(newTransitions);
    }

    /**
     * Builds the ACTION table for the parsing table.
     * This method constructs the ACTION table by:
     * 1. Adding SHIFT actions for items that can advance on terminals
     * 2. Adding REDUCE actions for complete items based on their lookaheads
     * 3. Adding the ACCEPT action for the augmented start production
     * 4. Detecting and reporting conflicts (shift/reduce and reduce/reduce)
     *
     * @param table the parsing table to populate
     */
    private void buildActionTable(ParsingTable table) {
        List<String> conflicts = new ArrayList<>();

        for (int stateIndex = 0; stateIndex < lr0States.size(); stateIndex++) {
            LRItemSet state = lr0States.get(stateIndex);

            // Add actions for shift items
            for (LRItem item : state.getItems()) {
                if (item.canAdvance()) {
                    Symbol nextSymbol = item.getNextSymbol();
                    if (nextSymbol.isTerminal()) {
                        Terminal terminal = (Terminal) nextSymbol;
                        Integer nextState = transitions.get(stateIndex).get(terminal);

                        if (nextState != null) {
                            Action newAction = Action.shift(nextState);
                            addActionWithConflictDetection(table, stateIndex, terminal, newAction, conflicts);
                        }
                    }
                }
            }

            // Add actions for reduce items
            for (LRItem item : state.getCompleteItems()) {
                Production production = item.getProduction();

                // Check if this is the accept item (S' -> S $ •)
                if (isAcceptItem(item)) {
                    Action acceptAction = Action.accept();
                    addActionWithConflictDetection(table, stateIndex, endOfInput, acceptAction, conflicts);
                } else {
                    // Add reduce actions for all lookahead symbols
                    Set<Terminal> lookaheads = item.getLookaheads();
                    if (lookaheads.isEmpty()) {
                        // For LR(0) items without lookaheads, use FOLLOW set
                        lookaheads = grammar.getFollowSet(production.getLeftSide());
                    }

                    for (Terminal lookahead : lookaheads) {
                        Action newAction = Action.reduce(production);
                        addActionWithConflictDetection(table, stateIndex, lookahead, newAction, conflicts);
                    }
                }
            }
        }

        // Store conflicts in the table for later retrieval
        if (!conflicts.isEmpty()) {
            // Report conflicts through ErrorReporter
            for (String conflict : conflicts) {
                String suggestion = generateConflictSuggestion(conflict);
                errorReporter.reportLALRConflict(conflict, "LALR table construction", suggestion);
            }

            errorReporter.reportError(ErrorReporter.Severity.ERROR,
                    ErrorReporter.ErrorType.LALR_CONFLICT,
                    String.format("LALR table construction failed with %d conflict(s)", conflicts.size()),
                    null,
                    "Conflicts detected during ACTION table construction",
                    "Resolve grammar conflicts or consider using a more powerful parsing algorithm");
        }
    }

    /**
     * Adds an action to the ACTION table with conflict detection and reporting.
     * This method checks for existing actions and reports conflicts when found.
     *
     * @param table     the parsing table to update
     * @param state     the state number
     * @param terminal  the terminal symbol
     * @param newAction the action to add
     * @param conflicts the list to add conflict descriptions to
     */
    private void addActionWithConflictDetection(ParsingTable table, int state, Terminal terminal,
            Action newAction, List<String> conflicts) {
        Action existingAction = table.getAction(state, terminal);

        if (existingAction == null) {
            // No conflict - add the action
            table.setAction(state, terminal, newAction);
        } else if (!existingAction.equals(newAction)) {
            // Conflict detected - determine the type and report it
            String conflictType = determineConflictType(existingAction, newAction);
            String conflictDescription = String.format(
                    "State %d, symbol '%s': %s conflict between %s and %s",
                    state, terminal.getName(), conflictType,
                    existingAction.toDetailedString(), newAction.toDetailedString());
            conflicts.add(conflictDescription);

            // Apply conflict resolution strategy
            Action resolvedAction = resolveConflict(existingAction, newAction, terminal);
            table.setAction(state, terminal, resolvedAction);
        }
        // If actions are equal, no conflict - action is already set
    }

    /**
     * Determines the type of conflict between two actions.
     *
     * @param action1 the first action
     * @param action2 the second action
     * @return a string describing the conflict type
     */
    private String determineConflictType(Action action1, Action action2) {
        boolean hasShift = action1.isShift() || action2.isShift();
        boolean hasReduce = action1.isReduce() || action2.isReduce();

        if (hasShift && hasReduce) {
            return "shift/reduce";
        } else if (hasReduce && (action1.isReduce() && action2.isReduce())) {
            return "reduce/reduce";
        } else {
            return "action";
        }
    }

    /**
     * Resolves conflicts between two actions using standard precedence rules.
     * This is a basic conflict resolution strategy:
     * - Shift/reduce conflicts: prefer shift (shift-reduce conflict resolution)
     * - Reduce/reduce conflicts: prefer the production with lower ID (first
     * defined)
     *
     * @param existingAction the existing action
     * @param newAction      the new action
     * @param terminal       the terminal symbol (for potential precedence-based
     *                       resolution)
     * @return the action to use after conflict resolution
     */
    private Action resolveConflict(Action existingAction, Action newAction, Terminal terminal) {
        // Shift/reduce conflict: prefer shift
        if (existingAction.isShift() && newAction.isReduce()) {
            return existingAction; // Keep shift
        } else if (existingAction.isReduce() && newAction.isShift()) {
            return newAction; // Use shift
        }

        // Reduce/reduce conflict: prefer production with lower ID (earlier definition)
        if (existingAction.isReduce() && newAction.isReduce()) {
            if (existingAction.getParameter() <= newAction.getParameter()) {
                return existingAction;
            } else {
                return newAction;
            }
        }

        // For other conflicts, keep the existing action
        return existingAction;
    }

    /**
     * Builds the GOTO table for the parsing table.
     * This method constructs the GOTO table by adding entries for non-terminal
     * transitions.
     * The GOTO table determines the next state when a non-terminal is recognized
     * during parsing.
     *
     * @param table the parsing table to populate
     */
    private void buildGotoTable(ParsingTable table) {
        for (int stateIndex = 0; stateIndex < lr0States.size(); stateIndex++) {
            Map<Symbol, Integer> stateTransitions = transitions.get(stateIndex);

            if (stateTransitions != null) {
                for (Map.Entry<Symbol, Integer> transition : stateTransitions.entrySet()) {
                    Symbol symbol = transition.getKey();
                    Integer nextState = transition.getValue();

                    if (symbol.isNonTerminal()) {
                        NonTerminal nonTerminal = (NonTerminal) symbol;

                        // Validate the transition
                        if (nextState < 0 || nextState >= lr0States.size()) {
                            throw new IllegalStateException(
                                    String.format("Invalid GOTO transition: state %d -> %d on symbol %s",
                                            stateIndex, nextState, symbol.getName()));
                        }

                        // Check for duplicate GOTO entries (should not happen in correct
                        // implementation)
                        Integer existingGoto = table.getGoto(stateIndex, nonTerminal);
                        if (existingGoto != null && !existingGoto.equals(nextState)) {
                            throw new IllegalStateException(
                                    String.format("Duplicate GOTO entry: state %d, symbol %s has both %d and %d",
                                            stateIndex, symbol.getName(), existingGoto, nextState));
                        }

                        table.setGoto(stateIndex, nonTerminal, nextState);
                    }
                }
            }
        }

        // Validate GOTO table completeness
        validateGotoTableCompleteness(table);
    }

    /**
     * Validates that the GOTO table is complete for all necessary
     * state/non-terminal pairs.
     * This method checks that every state that can transition on a non-terminal has
     * the appropriate GOTO entry.
     *
     * @param table the parsing table to validate
     */
    private void validateGotoTableCompleteness(ParsingTable table) {
        for (int stateIndex = 0; stateIndex < lr0States.size(); stateIndex++) {
            LRItemSet state = lr0States.get(stateIndex);

            // Check each item in the state
            for (LRItem item : state.getItems()) {
                if (item.canAdvance()) {
                    Symbol nextSymbol = item.getNextSymbol();
                    if (nextSymbol.isNonTerminal()) {
                        NonTerminal nonTerminal = (NonTerminal) nextSymbol;

                        // Verify that there's a GOTO entry for this state/non-terminal pair
                        Integer gotoState = table.getGoto(stateIndex, nonTerminal);
                        if (gotoState == null) {
                            // This might indicate an error in automaton construction
                            System.err.println(String.format(
                                    "Warning: Missing GOTO entry for state %d, non-terminal %s",
                                    stateIndex, nonTerminal.getName()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the given item is the accept item (S' -> S $ •).
     *
     * @param item the item to check
     * @return true if this is the accept item
     */
    private boolean isAcceptItem(LRItem item) {
        Production production = item.getProduction();
        return item.isComplete() &&
                production.getLeftSide().getName().equals("S'") &&
                production.getRightSideLength() == 2 &&
                production.getRightSideSymbol(0).equals(grammar.getStartSymbol()) &&
                production.getRightSideSymbol(1).equals(endOfInput);
    }

    /**
     * Returns the set of terminals including the end-of-input marker.
     *
     * @return the set of terminals with end marker
     */
    private Set<Terminal> getTerminalsWithEndMarker() {
        Set<Terminal> terminals = new LinkedHashSet<>(grammar.getTerminals());
        //Set<Terminal> terminals = new HashSet<>(grammar.getTerminals());
        terminals.add(endOfInput);
        return terminals;
    }

    /**
     * Returns the list of LR(0) states constructed by this builder.
     *
     * @return the list of LR(0) states
     */
    public List<LRItemSet> getLR0States() {
        return Collections.unmodifiableList(lr0States);
    }

    /**
     * Returns the transitions between states.
     *
     * @return the transition map
     */
    public Map<Integer, Map<Symbol, Integer>> getTransitions() {
        return Collections.unmodifiableMap(transitions);
    }

    /**
     * Returns the end-of-input terminal used by this builder.
     *
     * @return the end-of-input terminal
     */
    public Terminal getEndOfInputTerminal() {
        return endOfInput;
    }

    /**
     * Returns a string representation of the LR(0) automaton.
     *
     * @return string representation of the automaton
     */
    public String automatonToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LR(0) Automaton:\n");

        for (int i = 0; i < lr0States.size(); i++) {
            LRItemSet state = lr0States.get(i);
            sb.append("State ").append(i).append(":\n");
            sb.append(state.toString()).append("\n");

            Map<Symbol, Integer> stateTransitions = transitions.get(i);
            if (stateTransitions != null && !stateTransitions.isEmpty()) {
                sb.append("  Transitions:\n");
                for (Map.Entry<Symbol, Integer> transition : stateTransitions.entrySet()) {
                    sb.append("    ").append(transition.getKey().getName())
                            .append(" -> ").append(transition.getValue()).append("\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Detects and reports conflicts in the parsing table.
     * 
     * @param table the parsing table to check for conflicts
     * @return the number of conflicts detected
     */
    private int detectAndReportConflicts(ParsingTable table) {
        int conflictCount = 0;

        try {
            // Use the existing conflict detection from the table
            List<String> conflicts = table.detectConflicts();
            conflictCount = conflicts.size();

            if (conflictCount > 0) {
                errorReporter.reportError(ErrorReporter.Severity.ERROR,
                        ErrorReporter.ErrorType.LALR_CONFLICT,
                        String.format("Detected %d LALR conflicts in parsing table", conflictCount),
                        null,
                        "Conflicts: " + conflicts.toString(),
                        "Resolve grammar ambiguities to eliminate conflicts");

                // Report individual conflicts
                for (String conflict : conflicts) {
                    errorReporter.reportLALRConflict(conflict, "LALR table construction",
                            generateConflictSuggestion(conflict));
                }
            }

        } catch (Exception e) {
            // Fallback: report that conflict detection failed
            errorReporter.reportError(ErrorReporter.Severity.WARNING,
                    ErrorReporter.ErrorType.LALR_CONFLICT,
                    "Could not perform detailed conflict detection: " + e.getMessage(),
                    null,
                    "Exception during conflict detection",
                    "Manual review of grammar may be needed");
        }

        return conflictCount;
    }

    /**
     * Generates suggestions for resolving conflicts based on conflict description.
     * 
     * @param conflictDescription the description of the conflict
     * @return a suggestion for resolving the conflict
     */
    private String generateConflictSuggestion(String conflictDescription) {
        if (conflictDescription.toLowerCase().contains("shift/reduce")) {
            return "Consider adding precedence rules or restructuring the grammar to eliminate shift/reduce ambiguity";
        } else if (conflictDescription.toLowerCase().contains("reduce/reduce")) {
            return "Restructure the grammar to make productions distinguishable or factor out common prefixes";
        } else if (conflictDescription.toLowerCase().contains("conflict")) {
            return "Review grammar structure and eliminate ambiguous constructs";
        } else {
            return "Analyze the grammar for ambiguities and apply appropriate disambiguation techniques";
        }
    }

}