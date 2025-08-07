package com.parsergenerator.analysis;

import com.parsergenerator.error.ErrorReporter;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;

import java.util.*;

/**
 * Analyzes grammar properties and validates grammar structure for LALR parsing.
 * This class performs semantic analysis including left recursion detection,
 * unreachable production detection, and comprehensive grammar validation.
 */
public class GrammarAnalyzer {
    private final Grammar grammar;
    private final ErrorReporter errorReporter;
    private final List<String> errors;
    private final List<String> warnings;
    private final List<ValidationError> validationErrors;
    
    /**
     * Constructs a GrammarAnalyzer for the given grammar.
     * 
     * @param grammar the grammar to analyze
     * @throws IllegalArgumentException if grammar is null
     */
    public GrammarAnalyzer(Grammar grammar) {
        this(grammar, new ErrorReporter());
    }
    
    /**
     * Constructs a GrammarAnalyzer with the specified error reporter.
     * 
     * @param grammar the grammar to analyze
     * @param errorReporter the error reporter to use
     * @throws IllegalArgumentException if grammar is null
     */
    public GrammarAnalyzer(Grammar grammar, ErrorReporter errorReporter) {
        if (grammar == null) {
            throw new IllegalArgumentException("Grammar cannot be null");
        }
        this.grammar = grammar;
        this.errorReporter = errorReporter != null ? errorReporter : new ErrorReporter();
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * Performs comprehensive analysis of the grammar.
     * 
     * @return AnalysisResult containing all findings
     */
    public AnalysisResult analyze() {
        errors.clear();
        warnings.clear();
        validationErrors.clear();
        int initialErrorCount = errorReporter.getErrorCount();
        int initialWarningCount = errorReporter.getWarningCount();
        
        try {
            errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, 
                                   String.format("Starting comprehensive grammar analysis - %d productions, %d terminals, %d non-terminals", 
                                               grammar.getProductions().size(), grammar.getTerminals().size(), grammar.getNonTerminals().size()));
            
            // Basic structural validation
            errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, "Validating basic grammar structure");
            validateBasicStructure();
            
            // Symbol validation
            errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, "Validating symbol definitions and usage");
            validateSymbols();
            
            // Left recursion detection
            errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, "Detecting left recursion");
            detectLeftRecursion();
            
            // Unreachable production detection
            errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, "Detecting unreachable productions");
            detectUnreachableProductions();
            
            // Additional semantic checks
            errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, "Validating semantic properties");
            validateSemanticProperties();
            
            // Advanced validation rules
            errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, "Applying advanced validation rules");
            validateAdvancedRules();
            
            // Report analysis summary with batch error collection
            int newErrors = errorReporter.getErrorCount() - initialErrorCount;
            int newWarnings = errorReporter.getWarningCount() - initialWarningCount;
            
            if (newErrors > 0 || newWarnings > 0) {
                errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, 
                    String.format("Grammar analysis completed: %d error(s), %d warning(s) found in this analysis", 
                                newErrors, newWarnings));
                
                // Provide batch error summary with suggestions
                if (newErrors > 0) {
                    String batchSuggestion = generateBatchErrorSuggestions();
                    errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, 
                                           "Batch error analysis: " + batchSuggestion);
                }
            } else {
                errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, 
                    "Grammar analysis completed successfully - no issues found");
            }
            
        } catch (Exception e) {
            String message = "Unexpected error during grammar analysis: " + e.getMessage();
            errors.add(message);
            errorReporter.reportError(ErrorReporter.Severity.ERROR, 
                                    ErrorReporter.ErrorType.INTERNAL, 
                                    message, 
                                    null, 
                                    "Exception: " + e.getClass().getSimpleName(), 
                                    "Check grammar structure and try again");
        }
        
        return new AnalysisResult(
            new ArrayList<>(errors),
            new ArrayList<>(warnings),
            !errors.isEmpty()
        );
    }
    
    /**
     * Validates basic grammar structure.
     */
    private void validateBasicStructure() {
        // Check if grammar has a start symbol
        if (!grammar.hasStartSymbol()) {
            String message = "Grammar must have a start symbol defined";
            String suggestion = "Add a start symbol using 'Start = <NonTerminal>' in your grammar file";
            
            errors.add(message);
            errorReporter.reportGrammarValidationError(message, "grammar structure", suggestion);
            validationErrors.add(new ValidationError(
                ValidationError.Severity.ERROR,
                ValidationError.Category.STRUCTURAL,
                message,
                suggestion,
                "A start symbol is required to define the entry point for parsing"
            ));
        }
        
        // Check if grammar has productions
        if (grammar.isEmpty()) {
            errors.add("Grammar must have at least one production");
            validationErrors.add(new ValidationError(
                ValidationError.Severity.ERROR,
                ValidationError.Category.STRUCTURAL,
                "Grammar must have at least one production",
                "Add at least one production rule in the format '<NonTerminal> ::= <symbols>'",
                "Productions define the structure of the language"
            ));
        }
        
        // Check if start symbol has productions
        if (grammar.hasStartSymbol() && !grammar.getStartSymbol().hasProductions()) {
            String startName = grammar.getStartSymbol().getName();
            errors.add("Start symbol '" + startName + "' has no productions");
            validationErrors.add(new ValidationError(
                ValidationError.Severity.ERROR,
                ValidationError.Category.STRUCTURAL,
                "Start symbol '" + startName + "' has no productions",
                "Add at least one production for the start symbol: '" + startName + " ::= <symbols>'",
                "The start symbol must have productions to define valid input"
            ));
        }
    }
    
    /**
     * Validates symbol definitions and usage.
     */
    private void validateSymbols() {
        Set<String> definedSymbols = new HashSet<>();
        Set<String> usedSymbols = new HashSet<>();
        
        // Collect all defined symbols
        for (Symbol symbol : grammar.getAllSymbols()) {
            definedSymbols.add(symbol.getName());
        }
        
        // Collect all used symbols and check for undefined symbols
        for (Production production : grammar.getProductions()) {
            for (Symbol symbol : production.getRightSide()) {
                usedSymbols.add(symbol.getName());
                if (!definedSymbols.contains(symbol.getName())) {
                    errors.add("Undefined symbol '" + symbol.getName() + 
                              "' used in production: " + production);
                    validationErrors.add(new ValidationError(
                        ValidationError.Severity.ERROR,
                        ValidationError.Category.SYMBOL_DEFINITION,
                        "Undefined symbol '" + symbol.getName() + "' used in production",
                        "Define the symbol '" + symbol.getName() + "' as either a terminal or non-terminal",
                        "Production: " + production
                    ));
                }
            }
        }
        
        // Check for unused terminals (warning only)
        for (Symbol symbol : grammar.getTerminals()) {
            if (!usedSymbols.contains(symbol.getName())) {
                warnings.add("Terminal '" + symbol.getName() + "' is defined but never used");
                validationErrors.add(new ValidationError(
                    ValidationError.Severity.WARNING,
                    ValidationError.Category.SYMBOL_DEFINITION,
                    "Terminal '" + symbol.getName() + "' is defined but never used",
                    "Remove unused terminal or add it to a production rule",
                    "Unused symbols may indicate incomplete grammar design"
                ));
            }
        }
        
        // Check for symbols with invalid names
        for (Symbol symbol : grammar.getAllSymbols()) {
            validateSymbolName(symbol);
        }
    }
    
    /**
     * Detects left recursion in the grammar.
     * Uses a depth-first search approach to detect both direct and indirect left recursion.
     */
    private void detectLeftRecursion() {
        Set<NonTerminal> visited = new HashSet<>();
        Set<NonTerminal> recursionStack = new HashSet<>();
        
        for (NonTerminal nonTerminal : grammar.getNonTerminals()) {
            if (!visited.contains(nonTerminal)) {
                detectLeftRecursionDFS(nonTerminal, visited, recursionStack, new ArrayList<>());
            }
        }
    }
    
    /**
     * Depth-first search for left recursion detection.
     */
    private void detectLeftRecursionDFS(NonTerminal current, Set<NonTerminal> visited, 
                                       Set<NonTerminal> recursionStack, List<NonTerminal> path) {
        visited.add(current);
        recursionStack.add(current);
        path.add(current);
        
        for (Production production : current.getProductions()) {
            // Check if the first symbol in the production is a non-terminal
            if (!production.isEpsilonProduction()) {
                Symbol firstSymbol = production.getRightSide().get(0);
                
                if (firstSymbol.isNonTerminal()) {
                    NonTerminal firstNT = (NonTerminal) firstSymbol;
                    
                    if (recursionStack.contains(firstNT)) {
                        // Found left recursion
                        reportLeftRecursion(path, firstNT, production);
                    } else if (!visited.contains(firstNT)) {
                        detectLeftRecursionDFS(firstNT, visited, recursionStack, new ArrayList<>(path));
                    }
                }
            }
        }
        
        recursionStack.remove(current);
    }
    
    /**
     * Reports a left recursion error with detailed path information.
     */
    private void reportLeftRecursion(List<NonTerminal> path, NonTerminal target, Production production) {
        StringBuilder sb = new StringBuilder();
        sb.append("Left recursion detected: ");
        
        // Find where the cycle starts
        int cycleStart = path.indexOf(target);
        if (cycleStart >= 0) {
            for (int i = cycleStart; i < path.size(); i++) {
                if (i > cycleStart) sb.append(" -> ");
                sb.append(path.get(i).getName());
            }
            sb.append(" -> ").append(target.getName());
        } else {
            // Direct left recursion
            sb.append(target.getName()).append(" -> ").append(target.getName());
        }
        
        String message = sb.toString();
        String context = "Production: " + production;
        String suggestion = generateLeftRecursionSuggestion(target, path);
        
        errors.add(message + " (via production: " + production + ")");
        errorReporter.reportGrammarValidationError(message, context, suggestion);
    }
    
    /**
     * Generates suggestions for fixing left recursion.
     */
    private String generateLeftRecursionSuggestion(NonTerminal target, List<NonTerminal> path) {
        if (path.size() <= 1) {
            // Direct left recursion
            return String.format("Convert direct left recursion in '%s' to right recursion or use iteration", 
                               target.getName());
        } else {
            // Indirect left recursion
            return String.format("Eliminate indirect left recursion by rewriting the productions for: %s", 
                               path.stream().map(NonTerminal::getName).reduce((a, b) -> a + ", " + b).orElse(""));
        }
    }
    
    /**
     * Detects unreachable productions in the grammar.
     * A production is unreachable if its left-hand side non-terminal
     * cannot be reached from the start symbol.
     */
    private void detectUnreachableProductions() {
        if (!grammar.hasStartSymbol()) {
            return; // Already reported as error
        }
        
        Set<NonTerminal> reachable = findReachableNonTerminals();
        
        for (NonTerminal nonTerminal : grammar.getNonTerminals()) {
            if (!reachable.contains(nonTerminal)) {
                String message = "Non-terminal '" + nonTerminal.getName() + "' is unreachable from start symbol";
                String context = "Start symbol: " + grammar.getStartSymbol().getName();
                String suggestion = "Remove unreachable non-terminal or add a production that references it";
                
                errors.add(message);
                errorReporter.reportGrammarValidationError(message, context, suggestion);
                
                // Report all unreachable productions for this non-terminal
                for (Production production : nonTerminal.getProductions()) {
                    String prodWarning = "Unreachable production: " + production;
                    warnings.add(prodWarning);
                    errorReporter.reportWarning(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, 
                                              prodWarning, null, 
                                              "Remove this production or make the non-terminal reachable");
                }
            }
        }
    }
    
    /**
     * Finds all non-terminals reachable from the start symbol using BFS.
     */
    private Set<NonTerminal> findReachableNonTerminals() {
        Set<NonTerminal> reachable = new HashSet<>();
        Queue<NonTerminal> queue = new LinkedList<>();
        
        NonTerminal startSymbol = grammar.getStartSymbol();
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
     * Validates additional semantic properties of the grammar.
     */
    private void validateSemanticProperties() {
        // Check for circular dependencies in non-terminals
        detectCircularDependencies();
        
        // Check for proper start symbol definition
        validateStartSymbolDefinition();
        
        // Check for reachable non-terminals without productions
        validateReachableNonTerminals();
    }
    
    /**
     * Detects circular dependencies that don't involve left recursion
     * but could still cause issues.
     */
    private void detectCircularDependencies() {
        Set<NonTerminal> visited = new HashSet<>();
        Set<NonTerminal> recursionStack = new HashSet<>();
        
        for (NonTerminal nonTerminal : grammar.getNonTerminals()) {
            if (!visited.contains(nonTerminal)) {
                detectCircularDependenciesDFS(nonTerminal, visited, recursionStack, new ArrayList<>());
            }
        }
    }
    
    /**
     * DFS for circular dependency detection.
     */
    private void detectCircularDependenciesDFS(NonTerminal current, Set<NonTerminal> visited,
                                              Set<NonTerminal> recursionStack, List<NonTerminal> path) {
        visited.add(current);
        recursionStack.add(current);
        path.add(current);
        
        for (Production production : current.getProductions()) {
            for (Symbol symbol : production.getRightSide()) {
                if (symbol.isNonTerminal()) {
                    NonTerminal nt = (NonTerminal) symbol;
                    
                    if (recursionStack.contains(nt)) {
                        // Found circular dependency
                        reportCircularDependency(path, nt);
                    } else if (!visited.contains(nt)) {
                        detectCircularDependenciesDFS(nt, visited, recursionStack, new ArrayList<>(path));
                    }
                }
            }
        }
        
        recursionStack.remove(current);
    }
    
    /**
     * Reports a circular dependency warning.
     */
    private void reportCircularDependency(List<NonTerminal> path, NonTerminal target) {
        StringBuilder sb = new StringBuilder();
        sb.append("Circular dependency detected: ");
        
        int cycleStart = path.indexOf(target);
        if (cycleStart >= 0) {
            for (int i = cycleStart; i < path.size(); i++) {
                if (i > cycleStart) sb.append(" -> ");
                sb.append(path.get(i).getName());
            }
            sb.append(" -> ").append(target.getName());
        }
        
        warnings.add(sb.toString());
        validationErrors.add(new ValidationError(
            ValidationError.Severity.WARNING,
            ValidationError.Category.CIRCULAR_DEPENDENCY,
            sb.toString(),
            "Consider restructuring the grammar to eliminate circular dependencies",
            "Circular dependencies can make grammar analysis more complex"
        ));
    }
    
    /**
     * Validates symbol names for proper format and reserved words.
     */
    private void validateSymbolName(Symbol symbol) {
        String name = symbol.getName();
        
        // Check for empty or null names
        if (name == null || name.trim().isEmpty()) {
            errors.add("Symbol with empty or null name detected");
            validationErrors.add(new ValidationError(
                ValidationError.Severity.ERROR,
                ValidationError.Category.SYMBOL_DEFINITION,
                "Symbol with empty or null name detected",
                "Provide a valid name for all symbols",
                "Symbol type: " + symbol.getType()
            ));
            return;
        }
        
        // Check for reserved words or problematic names
        String lowerName = name.toLowerCase();
        if (lowerName.equals("epsilon") || lowerName.equals("ε") || lowerName.equals("lambda") || 
            lowerName.equals("empty") || lowerName.equals("null")) {
            warnings.add("Symbol '" + name + "' uses a name that might conflict with epsilon notation");
            validationErrors.add(new ValidationError(
                ValidationError.Severity.WARNING,
                ValidationError.Category.SYMBOL_DEFINITION,
                "Symbol '" + name + "' uses potentially problematic name",
                "Consider using a different name to avoid confusion with epsilon notation",
                "Reserved/problematic names: epsilon, ε, lambda, empty, null"
            ));
        }
        
        // Check for symbols starting with numbers (might cause parsing issues)
        if (name.matches("^\\d.*")) {
            warnings.add("Symbol '" + name + "' starts with a digit, which may cause parsing issues");
            validationErrors.add(new ValidationError(
                ValidationError.Severity.WARNING,
                ValidationError.Category.SYMBOL_DEFINITION,
                "Symbol '" + name + "' starts with a digit",
                "Consider starting symbol names with letters or underscores",
                "Names starting with digits may cause lexical analysis issues"
            ));
        }
        
        // Check for symbols with special characters that might cause issues
        if (name.matches(".*[<>\\[\\]{}()\\|\\\\].*")) {
            warnings.add("Symbol '" + name + "' contains special characters that may cause parsing issues");
            validationErrors.add(new ValidationError(
                ValidationError.Severity.WARNING,
                ValidationError.Category.SYMBOL_DEFINITION,
                "Symbol '" + name + "' contains special characters",
                "Avoid using <, >, [, ], {, }, (, ), |, \\ in symbol names",
                "Special characters may conflict with grammar notation"
            ));
        }
    }
    
    /**
     * Validates that the start symbol is properly defined.
     */
    private void validateStartSymbolDefinition() {
        if (!grammar.hasStartSymbol()) {
            return; // Already reported as error
        }
        
        NonTerminal startSymbol = grammar.getStartSymbol();
        
        // Check if start symbol is in the non-terminals set
        if (!grammar.getNonTerminals().contains(startSymbol)) {
            errors.add("Start symbol '" + startSymbol.getName() + 
                      "' is not defined as a non-terminal in the grammar");
            validationErrors.add(new ValidationError(
                ValidationError.Severity.ERROR,
                ValidationError.Category.STRUCTURAL,
                "Start symbol '" + startSymbol.getName() + "' is not defined as a non-terminal",
                "Add productions for the start symbol or choose a different start symbol",
                "The start symbol must be a non-terminal with at least one production"
            ));
        }
        
        // Check if start symbol appears on the right-hand side of any production
        boolean appearsOnRHS = false;
        Production rhsProduction = null;
        for (Production production : grammar.getProductions()) {
            if (production.containsSymbol(startSymbol)) {
                appearsOnRHS = true;
                rhsProduction = production;
                break;
            }
        }
        
        if (appearsOnRHS) {
            warnings.add("Start symbol '" + startSymbol.getName() + 
                        "' appears on the right-hand side of productions. " +
                        "This may cause ambiguity in parsing.");
            validationErrors.add(new ValidationError(
                ValidationError.Severity.WARNING,
                ValidationError.Category.STRUCTURAL,
                "Start symbol appears on right-hand side of production",
                "Consider creating a new start symbol that only appears on the left-hand side",
                "Production: " + rhsProduction
            ));
        }
        
        // Check if start symbol name follows conventions
        String startName = startSymbol.getName();
        if (!startName.matches("^[A-Z].*")) {
            warnings.add("Start symbol '" + startName + "' does not follow naming convention (should start with uppercase)");
            validationErrors.add(new ValidationError(
                ValidationError.Severity.WARNING,
                ValidationError.Category.STRUCTURAL,
                "Start symbol does not follow naming convention",
                "Consider naming the start symbol with an uppercase letter",
                "Convention: Start symbols typically begin with uppercase letters"
            ));
        }
    }
    
    /**
     * Validates that all reachable non-terminals have productions.
     */
    private void validateReachableNonTerminals() {
        if (!grammar.hasStartSymbol()) {
            return; // Already reported as error
        }
        
        Set<NonTerminal> reachable = findReachableNonTerminals();
        
        for (NonTerminal nt : reachable) {
            if (!nt.hasProductions()) {
                errors.add("Non-terminal '" + nt.getName() + 
                          "' is reachable but has no productions");
            }
        }
    }
    
    /**
     * Checks if the grammar has left recursion.
     * 
     * @return true if left recursion is detected, false otherwise
     */
    public boolean hasLeftRecursion() {
        AnalysisResult result = analyze();
        return result.getErrors().stream()
                .anyMatch(error -> error.contains("Left recursion detected"));
    }
    
    /**
     * Checks if the grammar has unreachable productions.
     * 
     * @return true if unreachable productions are detected, false otherwise
     */
    public boolean hasUnreachableProductions() {
        AnalysisResult result = analyze();
        return result.getErrors().stream()
                .anyMatch(error -> error.contains("unreachable"));
    }
    
    /**
     * Returns a list of all left-recursive non-terminals.
     * 
     * @return list of left-recursive non-terminals
     */
    public List<NonTerminal> getLeftRecursiveNonTerminals() {
        List<NonTerminal> leftRecursive = new ArrayList<>();
        Set<NonTerminal> visited = new HashSet<>();
        Set<NonTerminal> recursionStack = new HashSet<>();
        
        for (NonTerminal nonTerminal : grammar.getNonTerminals()) {
            if (!visited.contains(nonTerminal)) {
                findLeftRecursiveNonTerminals(nonTerminal, visited, recursionStack, leftRecursive);
            }
        }
        
        return leftRecursive;
    }
    
    /**
     * Helper method to find left-recursive non-terminals.
     */
    private void findLeftRecursiveNonTerminals(NonTerminal current, Set<NonTerminal> visited,
                                              Set<NonTerminal> recursionStack, List<NonTerminal> result) {
        visited.add(current);
        recursionStack.add(current);
        
        for (Production production : current.getProductions()) {
            if (!production.isEpsilonProduction()) {
                Symbol firstSymbol = production.getRightSide().get(0);
                
                if (firstSymbol.isNonTerminal()) {
                    NonTerminal firstNT = (NonTerminal) firstSymbol;
                    
                    if (recursionStack.contains(firstNT)) {
                        // Found left recursion
                        if (!result.contains(current)) {
                            result.add(current);
                        }
                        if (!result.contains(firstNT)) {
                            result.add(firstNT);
                        }
                    } else if (!visited.contains(firstNT)) {
                        findLeftRecursiveNonTerminals(firstNT, visited, recursionStack, result);
                    }
                }
            }
        }
        
        recursionStack.remove(current);
    }
    
    /**
     * Returns a list of all unreachable non-terminals.
     * 
     * @return list of unreachable non-terminals
     */
    public List<NonTerminal> getUnreachableNonTerminals() {
        if (!grammar.hasStartSymbol()) {
            return new ArrayList<>(grammar.getNonTerminals());
        }
        
        Set<NonTerminal> reachable = findReachableNonTerminals();
        List<NonTerminal> unreachable = new ArrayList<>();
        
        for (NonTerminal nt : grammar.getNonTerminals()) {
            if (!reachable.contains(nt)) {
                unreachable.add(nt);
            }
        }
        
        return unreachable;
    }
    
    /**
     * Generates batch error suggestions based on collected validation errors.
     * 
     * @return a comprehensive suggestion string for fixing multiple errors
     */
    private String generateBatchErrorSuggestions() {
        Map<ValidationError.Category, Integer> errorCounts = new HashMap<>();
        
        // Count errors by category
        for (ValidationError error : validationErrors) {
            if (error.getSeverity() == ValidationError.Severity.ERROR) {
                errorCounts.put(error.getCategory(), 
                              errorCounts.getOrDefault(error.getCategory(), 0) + 1);
            }
        }
        
        StringBuilder suggestions = new StringBuilder();
        
        if (errorCounts.containsKey(ValidationError.Category.STRUCTURAL)) {
            suggestions.append("Fix structural issues first (missing start symbol, empty grammar). ");
        }
        
        if (errorCounts.containsKey(ValidationError.Category.SYMBOL_DEFINITION)) {
            suggestions.append("Resolve symbol definition problems (undefined symbols, naming issues). ");
        }
        
        if (errorCounts.containsKey(ValidationError.Category.LEFT_RECURSION)) {
            suggestions.append("Eliminate left recursion using standard transformation techniques. ");
        }
        
        if (errorCounts.containsKey(ValidationError.Category.REACHABILITY)) {
            suggestions.append("Remove unreachable productions or add references to make them reachable. ");
        }
        
        if (errorCounts.containsKey(ValidationError.Category.CIRCULAR_DEPENDENCY)) {
            suggestions.append("Restructure grammar to eliminate circular dependencies. ");
        }
        
        if (suggestions.length() == 0) {
            suggestions.append("Review individual error messages for specific guidance.");
        }
        
        return suggestions.toString().trim();
    }
    
    /**
     * Performs advanced validation rules including comprehensive checks
     * for undefined symbols, circular dependencies, and proper grammar structure.
     */
    private void validateAdvancedRules() {
        // Check for empty productions that might cause issues
        validateEmptyProductions();
        
        // Check for duplicate productions
        validateDuplicateProductions();
        
        // Check for ambiguous grammar patterns
        validateAmbiguousPatterns();
        
        // Check for proper terminal definitions
        validateTerminalDefinitions();
        
        // Check for non-productive non-terminals
        validateProductiveNonTerminals();
        
        // Check for grammar complexity issues
        validateGrammarComplexity();
    }
    
    /**
     * Validates empty productions and their implications.
     */
    private void validateEmptyProductions() {
        for (Production production : grammar.getProductions()) {
            if (production.isEpsilonProduction()) {
                NonTerminal leftSide = production.getLeftSide();
                
                // Check if epsilon production is for start symbol
                if (leftSide.equals(grammar.getStartSymbol())) {
                    warnings.add("Start symbol '" + leftSide.getName() + 
                               "' has epsilon production, which may cause parsing ambiguity");
                    validationErrors.add(new ValidationError(
                        ValidationError.Severity.WARNING,
                        ValidationError.Category.SEMANTIC,
                        "Start symbol has epsilon production",
                        "Consider adding a new start symbol that doesn't derive epsilon",
                        "Epsilon productions in start symbols can cause parsing issues"
                    ));
                }
                
                // Check for multiple epsilon productions for same non-terminal
                long epsilonCount = leftSide.getProductions().stream()
                    .mapToLong(p -> p.isEpsilonProduction() ? 1 : 0)
                    .sum();
                
                if (epsilonCount > 1) {
                    warnings.add("Non-terminal '" + leftSide.getName() + 
                               "' has multiple epsilon productions");
                }
            }
        }
    }
    
    /**
     * Validates for duplicate productions.
     */
    private void validateDuplicateProductions() {
        Set<String> productionStrings = new HashSet<>();
        
        for (Production production : grammar.getProductions()) {
            String prodStr = production.toString();
            if (productionStrings.contains(prodStr)) {
                warnings.add("Duplicate production detected: " + prodStr);
                validationErrors.add(new ValidationError(
                    ValidationError.Severity.WARNING,
                    ValidationError.Category.SEMANTIC,
                    "Duplicate production: " + prodStr,
                    "Remove duplicate production rules",
                    "Duplicate productions are redundant and may indicate grammar design issues"
                ));
            } else {
                productionStrings.add(prodStr);
            }
        }
    }
    
    /**
     * Validates for potentially ambiguous grammar patterns.
     */
    private void validateAmbiguousPatterns() {
        // Check for productions with same left-hand side that start with same symbol
        Map<NonTerminal, List<Production>> productionsByLHS = new HashMap<>();
        
        for (Production production : grammar.getProductions()) {
            productionsByLHS.computeIfAbsent(production.getLeftSide(), k -> new ArrayList<>())
                           .add(production);
        }
        
        for (Map.Entry<NonTerminal, List<Production>> entry : productionsByLHS.entrySet()) {
            List<Production> productions = entry.getValue();
            if (productions.size() > 1) {
                checkForCommonPrefixes(entry.getKey(), productions);
            }
        }
    }
    
    /**
     * Checks for common prefixes in productions that might cause ambiguity.
     */
    private void checkForCommonPrefixes(NonTerminal nonTerminal, List<Production> productions) {
        for (int i = 0; i < productions.size(); i++) {
            for (int j = i + 1; j < productions.size(); j++) {
                Production prod1 = productions.get(i);
                Production prod2 = productions.get(j);
                
                if (!prod1.isEpsilonProduction() && !prod2.isEpsilonProduction()) {
                    Symbol first1 = prod1.getRightSide().get(0);
                    Symbol first2 = prod2.getRightSide().get(0);
                    
                    if (first1.equals(first2)) {
                        warnings.add("Potential ambiguity: Productions for '" + 
                                   nonTerminal.getName() + "' start with same symbol '" + 
                                   first1.getName() + "'");
                        validationErrors.add(new ValidationError(
                            ValidationError.Severity.WARNING,
                            ValidationError.Category.SEMANTIC,
                            "Potential ambiguity in productions for " + nonTerminal.getName(),
                            "Consider left-factoring the grammar or using different first symbols",
                            "Productions: " + prod1 + " and " + prod2
                        ));
                    }
                }
            }
        }
    }
    
    /**
     * Validates terminal definitions.
     */
    private void validateTerminalDefinitions() {
        for (Terminal terminal : grammar.getTerminals()) {
            // Check for terminals with empty names
            if (terminal.getName().trim().isEmpty()) {
                errors.add("Terminal with empty name detected");
            }
            
            // Check for terminals that might conflict with keywords
            String name = terminal.getName().toLowerCase();
            if (name.equals("epsilon") || name.equals("ε") || name.equals("lambda")) {
                warnings.add("Terminal '" + terminal.getName() + 
                           "' uses a name that might conflict with epsilon notation");
            }
        }
    }
    
    /**
     * Validates that all non-terminals are productive (can derive terminal strings).
     */
    private void validateProductiveNonTerminals() {
        Set<NonTerminal> productive = findProductiveNonTerminals();
        
        for (NonTerminal nt : grammar.getNonTerminals()) {
            if (!productive.contains(nt)) {
                errors.add("Non-terminal '" + nt.getName() + 
                          "' is not productive (cannot derive any terminal string)");
                validationErrors.add(new ValidationError(
                    ValidationError.Severity.ERROR,
                    ValidationError.Category.SEMANTIC,
                    "Non-productive non-terminal: " + nt.getName(),
                    "Ensure this non-terminal has productions that eventually lead to terminals",
                    "Non-productive non-terminals make parts of the grammar unusable"
                ));
            }
        }
    }
    
    /**
     * Finds all productive non-terminals using a fixed-point algorithm.
     */
    private Set<NonTerminal> findProductiveNonTerminals() {
        Set<NonTerminal> productive = new HashSet<>();
        boolean changed = true;
        
        while (changed) {
            changed = false;
            
            for (Production production : grammar.getProductions()) {
                NonTerminal leftSide = production.getLeftSide();
                
                if (!productive.contains(leftSide)) {
                    if (isProductionProductive(production, productive)) {
                        productive.add(leftSide);
                        changed = true;
                    }
                }
            }
        }
        
        return productive;
    }
    
    /**
     * Checks if a production is productive given the current set of productive non-terminals.
     */
    private boolean isProductionProductive(Production production, Set<NonTerminal> productive) {
        // Epsilon productions are always productive
        if (production.isEpsilonProduction()) {
            return true;
        }
        
        // All symbols in the production must be productive
        for (Symbol symbol : production.getRightSide()) {
            if (symbol.isTerminal()) {
                continue; // Terminals are always productive
            } else {
                NonTerminal nt = (NonTerminal) symbol;
                if (!productive.contains(nt)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Validates grammar complexity and provides warnings for potentially problematic patterns.
     */
    private void validateGrammarComplexity() {
        int productionCount = grammar.getProductionCount();
        int nonTerminalCount = grammar.getNonTerminalCount();
        
        // Check for overly complex grammars
        if (productionCount > 100) {
            warnings.add("Grammar has " + productionCount + 
                        " productions, which may impact parser generation performance");
        }
        
        if (nonTerminalCount > 50) {
            warnings.add("Grammar has " + nonTerminalCount + 
                        " non-terminals, consider simplifying if possible");
        }
        
        // Check for non-terminals with too many productions
        for (NonTerminal nt : grammar.getNonTerminals()) {
            int prodCount = nt.getProductionCount();
            if (prodCount > 20) {
                warnings.add("Non-terminal '" + nt.getName() + "' has " + prodCount + 
                           " productions, consider factoring");
            }
        }
    }
    
    /**
     * Returns the list of detailed validation errors.
     * 
     * @return list of ValidationError objects
     */
    public List<ValidationError> getValidationErrors() {
        return new ArrayList<>(validationErrors);
    }
    
    /**
     * Generates a detailed validation report.
     * 
     * @return formatted validation report
     */
    public String generateValidationReport() {
        StringBuilder report = new StringBuilder();
        report.append("Grammar Validation Report\n");
        report.append("========================\n\n");
        
        report.append("Grammar Statistics:\n");
        report.append("- Productions: ").append(grammar.getProductionCount()).append("\n");
        report.append("- Non-terminals: ").append(grammar.getNonTerminalCount()).append("\n");
        report.append("- Terminals: ").append(grammar.getTerminalCount()).append("\n");
        report.append("- Start symbol: ").append(
            grammar.hasStartSymbol() ? grammar.getStartSymbol().getName() : "Not defined"
        ).append("\n\n");
        
        if (!validationErrors.isEmpty()) {
            report.append("Validation Issues:\n");
            report.append("-----------------\n");
            
            for (ValidationError error : validationErrors) {
                report.append(error.toString()).append("\n");
            }
        } else {
            report.append("No validation issues found.\n");
        }
        
        return report.toString();
    }
    
    /**
     * Returns the error reporter used by this analyzer.
     * 
     * @return the error reporter
     */
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }
}