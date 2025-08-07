package com.parsergenerator.parser;

import com.parsergenerator.error.ErrorReporter;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.lexer.Position;
import com.parsergenerator.lexer.Token;
import com.parsergenerator.lexer.TokenType;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parser for grammar specification files. Parses tokenized grammar input
 * and constructs a Grammar object representing the context-free grammar.
 * 
 * Grammar syntax:
 * - Terminal definitions: TerminalName = {Pattern}+
 * - Production rules: <NonTerminal> ::= <Rule1> | <Rule2> | ...
 * - Start symbol: Start = <NonTerminal>
 * - Comments: -- comment text
 */
public class GrammarParser {
    private final List<Token> tokens;
    private final ErrorReporter errorReporter;
    private int currentPosition;
    private Grammar grammar;
    private final List<ParseException> errors;
    private boolean recoveryMode;
    
    /**
     * Creates a new grammar parser for the specified tokens.
     * 
     * @param tokens the list of tokens to parse
     * @throws IllegalArgumentException if tokens is null
     */
    public GrammarParser(List<Token> tokens) {
        this(tokens, new ErrorReporter());
    }
    
    /**
     * Creates a new grammar parser with the specified error reporter.
     * 
     * @param tokens the list of tokens to parse
     * @param errorReporter the error reporter to use
     * @throws IllegalArgumentException if tokens is null
     */
    public GrammarParser(List<Token> tokens, ErrorReporter errorReporter) {
        if (tokens == null) {
            throw new IllegalArgumentException("Tokens cannot be null");
        }
        this.tokens = new ArrayList<>(tokens);
        this.errorReporter = errorReporter != null ? errorReporter : new ErrorReporter();
        this.currentPosition = 0;
        this.grammar = new Grammar();
        this.errors = new ArrayList<>();
        this.recoveryMode = false;
    }
    
    /**
     * Parses the tokens and returns a Grammar object.
     * 
     * @return the parsed grammar
     * @throws ParseException if parsing fails
     */
    public Grammar parse() throws ParseException {
        grammar = new Grammar();
        currentPosition = 0;
        errors.clear();
        recoveryMode = false;
        int initialErrorCount = errorReporter.getErrorCount();
        int initialWarningCount = errorReporter.getWarningCount();
        
        try {
            errorReporter.reportInfo(ErrorReporter.ErrorType.SYNTAX, 
                                   String.format("Starting grammar parsing - %d tokens to process", tokens.size()));
            
            while (!isAtEnd()) {
                skipCommentsAndNewlines();
                
                if (isAtEnd()) {
                    break;
                }
                
                try {
                    parseStatement();
                    recoveryMode = false; // Successfully parsed a statement
                } catch (RecoveryException e) {
                    errors.add(e);
                    currentPosition = e.getRecoveryPosition();
                    recoveryMode = true;
                    
                    // Report recovery attempt
                    errorReporter.reportInfo(ErrorReporter.ErrorType.SYNTAX, 
                                           "Attempting error recovery at position " + currentPosition);
                } catch (ParseException e) {
                    errors.add(e);
                    // Ensure we advance at least one position to avoid infinite loops
                    int positionBeforeRecovery = currentPosition;
                    recoverToNextStatement();
                    // If recovery didn't advance position, force advance to prevent infinite loop
                    if (currentPosition == positionBeforeRecovery && !isAtEnd()) {
                        advance();
                    }
                    recoveryMode = true;
                    
                    // Report recovery attempt
                    errorReporter.reportInfo(ErrorReporter.ErrorType.SYNTAX, 
                                           "Error recovery completed, continuing from position " + currentPosition);
                }
            }
            
            // Perform post-parsing validation
            try {
                validateGrammar();
            } catch (ParseException e) {
                errors.add(e);
            }
            
            // Report parsing summary
            int newErrors = errorReporter.getErrorCount() - initialErrorCount;
            int newWarnings = errorReporter.getWarningCount() - initialWarningCount;
            
            if (newErrors > 0 || newWarnings > 0) {
                errorReporter.reportInfo(ErrorReporter.ErrorType.SYNTAX, 
                    String.format("Parsing completed with %d error(s), %d warning(s) - Grammar has %d productions", 
                                newErrors, newWarnings, grammar.getProductions().size()));
            } else {
                errorReporter.reportInfo(ErrorReporter.ErrorType.SYNTAX, 
                    String.format("Parsing completed successfully - Grammar has %d productions, %d terminals, %d non-terminals", 
                                grammar.getProductions().size(), grammar.getTerminals().size(), grammar.getNonTerminals().size()));
            }
            
        } catch (Exception e) {
            errorReporter.reportError(ErrorReporter.Severity.ERROR, 
                                    ErrorReporter.ErrorType.INTERNAL, 
                                    "Unexpected error during parsing: " + e.getMessage(), 
                                    getCurrentPosition(), 
                                    "Exception: " + e.getClass().getSimpleName(), 
                                    "Check grammar file syntax and structure");
            errors.add(new ParseException("Internal parsing error: " + e.getMessage(), getCurrentPosition()));
        }
        
        // If we collected errors, throw the first one (or a combined exception)
        if (!errors.isEmpty()) {
            // Report error collection summary
            errorReporter.reportInfo(ErrorReporter.ErrorType.SYNTAX, 
                                   String.format("Collected %d parse errors - throwing combined exception", errors.size()));
            
            if (errors.size() == 1) {
                throw errors.get(0);
            } else {
                throw createCombinedErrorException();
            }
        }
        
        return grammar;
    }
    
    /**
     * Parses a single statement (terminal definition, production rule, or start symbol).
     */
    private void parseStatement() throws ParseException {
        Token current = peek();
        
        if (current.isType(TokenType.START)) {
            parseStartSymbol();
        } else if (current.isType(TokenType.TERMINAL)) {
            // Always try to parse as terminal definition - let it handle the error
            parseTerminalDefinition();
        } else if (current.isType(TokenType.NON_TERMINAL)) {
            parseProductionRule();
        } else if (current.isType(TokenType.EOF)) {
            // End of input - this is normal
            return;
        } else if (current.isType(TokenType.COMMENT) || current.isType(TokenType.NEWLINE) || 
                   current.isType(TokenType.WHITESPACE)) {
            // Skip whitespace and comments - this shouldn't happen due to skipCommentsAndNewlines
            skipCommentsAndNewlines();
            // Don't recursively call parseStatement - let the main loop handle it
        } else {
            Set<TokenType> validTokens = new HashSet<>(Arrays.asList(
                TokenType.START, TokenType.TERMINAL, TokenType.NON_TERMINAL
            ));
            reportUnexpectedToken(current, validTokens);
        }
        
        skipCommentsAndNewlines();
    }
    
    /**
     * Parses a start symbol definition: Start = <NonTerminal>
     */
    private void parseStartSymbol() throws ParseException {
        Token startToken = consume(TokenType.START, "Expected 'Start' keyword");
        
        try {
            consume(TokenType.EQUALS, "Expected '=' after 'Start'");
        } catch (ParseException e) {
            Token actual = peek();
            if (actual.isType(TokenType.PRODUCTION_ARROW)) {
                reportSyntaxError("Invalid operator for start symbol definition", 
                                TokenType.EQUALS, actual, "start symbol definition");
            } else {
                throw e;
            }
        }
        
        Token nonTerminalToken;
        try {
            nonTerminalToken = consume(TokenType.NON_TERMINAL, "Expected non-terminal after 'Start ='");
        } catch (ParseException e) {
            Token actual = peek();
            if (actual.isType(TokenType.TERMINAL)) {
                reportSyntaxError("Start symbol must be a non-terminal", 
                                TokenType.NON_TERMINAL, actual, "start symbol definition");
                return; // This line won't be reached due to exception, but satisfies compiler
            } else {
                throw e;
            }
        }
        
        String nonTerminalName = nonTerminalToken.getValue();
        
        // Check for duplicate start symbol definition
        if (grammar.getStartSymbol() != null) {
            reportSemanticError("Start symbol already defined", 
                              nonTerminalToken.getPosition(), 
                              nonTerminalName, "duplicate_start_symbol");
        }
        
        NonTerminal startSymbol = getOrCreateNonTerminal(nonTerminalName);
        grammar.setStartSymbol(startSymbol);
    }
    
    /**
     * Parses a terminal definition: TerminalName = {Pattern}+
     */
    private void parseTerminalDefinition() throws ParseException {
        Token terminalToken = consume(TokenType.TERMINAL, "Expected terminal name");
        
        try {
            consume(TokenType.EQUALS, "Expected '=' after terminal name");
        } catch (ParseException e) {
            Token actual = peek();
            if (actual.isType(TokenType.PRODUCTION_ARROW)) {
                reportSyntaxError("Invalid operator for terminal definition", 
                                TokenType.EQUALS, actual, "terminal definition");
            } else if (actual.isType(TokenType.TERMINAL)) {
                reportSyntaxError("Unexpected terminal symbol", 
                                TokenType.EQUALS, actual, "terminal definition");
            } else {
                throw e;
            }
        }
        
        Token patternToken;
        try {
            patternToken = consume(TokenType.PATTERN, "Expected pattern after '='");
        } catch (ParseException e) {
            Token actual = peek();
            if (actual.isType(TokenType.TERMINAL) || actual.isType(TokenType.NON_TERMINAL)) {
                reportSyntaxError("Terminal pattern must be enclosed in braces", 
                                TokenType.PATTERN, actual, "terminal definition");
                return; // This line won't be reached due to exception, but satisfies compiler
            } else {
                throw e;
            }
        }
        
        // Handle optional '+' after pattern
        if (peek().isType(TokenType.PLUS)) {
            advance(); // consume the '+'
        }
        
        String terminalName = terminalToken.getValue();
        String pattern = patternToken.getValue();
        
        // Check for duplicate terminal definition
        if (grammar.getTerminal(terminalName) != null) {
            reportSemanticError("Terminal already defined", 
                              terminalToken.getPosition(), 
                              terminalName, "duplicate_terminal");
        }
        
        Terminal terminal = new Terminal(terminalName, pattern);
        grammar.addTerminal(terminal);
    }
    
    /**
     * Parses a production rule: <NonTerminal> ::= <Rule1> | <Rule2> | ...
     */
    private void parseProductionRule() throws ParseException {
        Token nonTerminalToken = consume(TokenType.NON_TERMINAL, "Expected non-terminal");
        
        try {
            consume(TokenType.PRODUCTION_ARROW, "Expected '::=' after non-terminal");
        } catch (ParseException e) {
            Token actual = peek();
            if (actual.isType(TokenType.EQUALS)) {
                reportSyntaxError("Invalid operator for production rule", 
                                TokenType.PRODUCTION_ARROW, actual, "production rule");
            } else {
                throw e;
            }
        }
        
        String nonTerminalName = nonTerminalToken.getValue();
        NonTerminal leftSide = getOrCreateNonTerminal(nonTerminalName);
        
        // Parse the first alternative
        List<Symbol> rightSide = parseRightHandSide();
        Production production = new Production(leftSide, rightSide);
        grammar.addProduction(production);
        
        // Parse additional alternatives separated by '|'
        while (peek().isType(TokenType.PIPE)) {
            advance(); // consume '|'
            
            try {
                rightSide = parseRightHandSide();
                production = new Production(leftSide, rightSide);
                grammar.addProduction(production);
            } catch (ParseException e) {
                // Try to recover from error in alternative
                reportSyntaxError("Invalid alternative in production rule", 
                                null, peek(), "production alternative");
                // Skip to next alternative or end of production
                recoverToNextAlternativeOrStatement();
            }
        }
    }
    
    /**
     * Parses the right-hand side of a production rule.
     * 
     * @return list of symbols on the right-hand side
     */
    private List<Symbol> parseRightHandSide() throws ParseException {
        List<Symbol> symbols = new ArrayList<>();
        boolean foundSymbol = false;
        
        // Parse symbols until we hit a delimiter or end
        while (!isAtEnd() && !peek().isAnyType(TokenType.PIPE, TokenType.NEWLINE, TokenType.EOF, TokenType.COMMENT)) {
            Token token = peek();
            
            if (token.isType(TokenType.TERMINAL)) {
                String terminalName = token.getValue();
                Terminal terminal = getOrCreateTerminal(terminalName);
                symbols.add(terminal);
                advance();
                foundSymbol = true;
            } else if (token.isType(TokenType.NON_TERMINAL)) {
                String nonTerminalName = token.getValue();
                NonTerminal nonTerminal = getOrCreateNonTerminal(nonTerminalName);
                symbols.add(nonTerminal);
                advance();
                foundSymbol = true;
            } else if (token.isType(TokenType.WHITESPACE)) {
                advance(); // Skip whitespace within production
            } else {
                // Unexpected token in production rule
                Set<TokenType> validTokens = new HashSet<>(Arrays.asList(
                    TokenType.TERMINAL, TokenType.NON_TERMINAL, TokenType.PIPE, TokenType.NEWLINE
                ));
                reportUnexpectedToken(token, validTokens);
                break;
            }
        }
        
        // Check for empty production (epsilon production)
        if (!foundSymbol) {
            // This could be an epsilon production, which is valid
            // We'll represent it as an empty symbol list
        }
        
        return symbols;
    }
    
    /**
     * Gets an existing terminal or creates a new one if it doesn't exist.
     * 
     * @param name the terminal name
     * @return the terminal symbol
     */
    private Terminal getOrCreateTerminal(String name) {
        Terminal terminal = grammar.getTerminal(name);
        if (terminal == null) {
            terminal = new Terminal(name);
            grammar.addTerminal(terminal);
        }
        return terminal;
    }
    
    /**
     * Gets an existing non-terminal or creates a new one if it doesn't exist.
     * 
     * @param name the non-terminal name
     * @return the non-terminal symbol
     */
    private NonTerminal getOrCreateNonTerminal(String name) {
        NonTerminal nonTerminal = grammar.getNonTerminal(name);
        if (nonTerminal == null) {
            nonTerminal = new NonTerminal(name);
            grammar.addNonTerminal(nonTerminal);
        }
        return nonTerminal;
    }
    
    /**
     * Skips comments and newline tokens.
     */
    private void skipCommentsAndNewlines() {
        while (!isAtEnd() && peek().isAnyType(TokenType.COMMENT, TokenType.NEWLINE, TokenType.WHITESPACE)) {
            advance();
        }
    }
    
    /**
     * Returns the current token without consuming it.
     * 
     * @return the current token
     */
    private Token peek() {
        if (currentPosition >= tokens.size()) {
            return new Token(TokenType.EOF, "", null);
        }
        return tokens.get(currentPosition);
    }
    
    /**
     * Returns the token at the specified offset from current position.
     * 
     * @param offset the offset from current position
     * @return the token at the offset position
     */
    private Token peekAhead(int offset) {
        int position = currentPosition + offset;
        if (position >= tokens.size()) {
            return new Token(TokenType.EOF, "", null);
        }
        return tokens.get(position);
    }
    
    /**
     * Consumes and returns the current token.
     * 
     * @return the consumed token
     */
    private Token advance() {
        if (!isAtEnd()) {
            currentPosition++;
        }
        return previous();
    }
    
    /**
     * Returns the previous token.
     * 
     * @return the previous token
     */
    private Token previous() {
        if (currentPosition == 0) {
            return new Token(TokenType.EOF, "", null);
        }
        return tokens.get(currentPosition - 1);
    }
    
    /**
     * Consumes a token of the expected type or throws an exception.
     * 
     * @param expectedType the expected token type
     * @param errorMessage the error message if token type doesn't match
     * @return the consumed token
     * @throws ParseException if the token type doesn't match
     */
    private Token consume(TokenType expectedType, String errorMessage) throws ParseException {
        Token current = peek();
        if (current.isType(expectedType)) {
            return advance();
        }
        
        reportSyntaxError(errorMessage, expectedType, current);
        return current; // This line won't be reached due to exception
    }
    
    /**
     * Attempts to consume a token with error recovery.
     * If the expected token is not found, tries to recover and continue parsing.
     * 
     * @param expectedType the expected token type
     * @param errorMessage the error message if token type doesn't match
     * @param canRecover whether recovery should be attempted
     * @return the consumed token, or null if recovery was attempted
     * @throws ParseException if the token type doesn't match and recovery fails
     */
    private Token consumeWithRecovery(TokenType expectedType, String errorMessage, 
                                    boolean canRecover) throws ParseException {
        Token current = peek();
        if (current.isType(expectedType)) {
            return advance();
        }
        
        if (canRecover) {
            // Try to recover by inserting the missing token conceptually
            String recoveryMessage = errorMessage + " (attempting recovery)";
            errors.add(new SyntaxErrorException(recoveryMessage, current.getPosition(), 
                                              expectedType, current.getType(), getCurrentContext()));
            return new Token(expectedType, "", current.getPosition()); // Synthetic token
        } else {
            reportSyntaxError(errorMessage, expectedType, current);
            return current; // This line won't be reached due to exception
        }
    }
    
    /**
     * Checks if we've reached the end of the token stream.
     * 
     * @return true if at end, false otherwise
     */
    private boolean isAtEnd() {
        return currentPosition >= tokens.size() || 
               (currentPosition < tokens.size() && tokens.get(currentPosition).isType(TokenType.EOF));
    }
    
    /**
     * Returns the list of errors encountered during parsing.
     * 
     * @return the list of parse errors
     */
    public List<ParseException> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * Checks if any errors were encountered during parsing.
     * 
     * @return true if errors were found, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Attempts to recover from a parse error by skipping to the next statement.
     * Recovery points are: newlines, start of new definitions, or EOF.
     */
    private void recoverToNextStatement() {
        Set<TokenType> recoveryTokens = new HashSet<>(Arrays.asList(
            TokenType.START, TokenType.TERMINAL, TokenType.NON_TERMINAL, 
            TokenType.NEWLINE, TokenType.EOF
        ));
        
        // Skip tokens until we find a recovery point
        while (!isAtEnd() && !recoveryTokens.contains(peek().getType())) {
            advance();
        }
        
        // Skip any newlines to get to the actual next statement
        skipCommentsAndNewlines();
    }
    
    /**
     * Attempts to recover from an error in a production alternative by skipping
     * to the next alternative (|) or the end of the production rule.
     */
    private void recoverToNextAlternativeOrStatement() {
        Set<TokenType> recoveryTokens = new HashSet<>(Arrays.asList(
            TokenType.PIPE, TokenType.START, TokenType.TERMINAL, 
            TokenType.NON_TERMINAL, TokenType.NEWLINE, TokenType.EOF
        ));
        
        // Skip tokens until we find a recovery point
        while (!isAtEnd() && !recoveryTokens.contains(peek().getType())) {
            advance();
        }
        
        // If we found a pipe, we can continue with the next alternative
        // Otherwise, skip newlines to get to the next statement
        if (!peek().isType(TokenType.PIPE)) {
            skipCommentsAndNewlines();
        }
    }
    
    /**
     * Creates a combined exception when multiple errors are encountered.
     * 
     * @return a ParseException summarizing all errors
     */
    private SyntaxErrorException createCombinedErrorException() {
        StringBuilder message = new StringBuilder("Multiple parse errors encountered:\n");
        
        for (int i = 0; i < errors.size(); i++) {
            ParseException error = errors.get(i);
            message.append(String.format("  %d. %s\n", i + 1, error.getMessage()));
        }
        
        return new SyntaxErrorException(message.toString(), errors.get(0).getPosition());
    }
    
    /**
     * Reports a syntax error with detailed context information.
     * 
     * @param message the error message
     * @param expectedType the expected token type
     * @param actualToken the actual token encountered
     * @param context additional context information
     * @throws SyntaxErrorException always
     */
    private void reportSyntaxError(String message, TokenType expectedType, 
                                 Token actualToken, String context) throws SyntaxErrorException {
        String enhancedMessage = enhanceErrorMessage(message, actualToken.getPosition(), 
                                                   context != null ? context : getCurrentContext());
        String suggestion = getSuggestion(expectedType, actualToken.getType());
        
        // Report to ErrorReporter
        errorReporter.reportSyntaxError(enhancedMessage, actualToken.getPosition(), suggestion);
        
        if (!suggestion.equals("Check grammar syntax and token placement")) {
            enhancedMessage += ". Suggestion: " + suggestion;
        }
        
        throw new SyntaxErrorException(enhancedMessage, actualToken.getPosition(), 
                                     expectedType, actualToken.getType(), context);
    }
    
    /**
     * Reports a syntax error with automatic context detection.
     * 
     * @param message the error message
     * @param expectedType the expected token type
     * @param actualToken the actual token encountered
     * @throws SyntaxErrorException always
     */
    private void reportSyntaxError(String message, TokenType expectedType, 
                                 Token actualToken) throws SyntaxErrorException {
        reportSyntaxError(message, expectedType, actualToken, getCurrentContext());
    }
    
    /**
     * Reports a semantic error with symbol information.
     * 
     * @param message the error message
     * @param position the error position
     * @param symbolName the symbol involved in the error
     * @param errorType the type of semantic error
     * @throws SemanticErrorException always
     */
    private void reportSemanticError(String message, Position position, 
                                   String symbolName, String errorType) throws SemanticErrorException {
        String enhancedMessage = enhanceErrorMessage(message, position, getCurrentContext());
        
        // Report to ErrorReporter
        String context = "Symbol: " + symbolName + ", Error type: " + errorType;
        errorReporter.reportSemanticError(enhancedMessage, context, null);
        
        throw new SemanticErrorException(enhancedMessage, position, symbolName, errorType);
    }
    
    /**
     * Reports an unexpected token error with recovery suggestions.
     * 
     * @param actualToken the unexpected token
     * @param validTokens set of valid token types at this position
     * @throws SyntaxErrorException always
     */
    private void reportUnexpectedToken(Token actualToken, Set<TokenType> validTokens) throws SyntaxErrorException {
        StringBuilder message = new StringBuilder("Unexpected token '")
                .append(actualToken.getValue()).append("'");
        
        if (!validTokens.isEmpty()) {
            message.append(". Expected one of: ");
            validTokens.forEach(type -> message.append(type).append(" "));
        }
        
        throw new SyntaxErrorException(message.toString(), actualToken.getPosition(), 
                                     null, actualToken.getType(), getCurrentContext());
    }
    
    /**
     * Provides suggestions for fixing common syntax errors.
     * 
     * @param expectedType the expected token type
     * @param actualType the actual token type
     * @return a helpful suggestion string
     */
    private String getSuggestion(TokenType expectedType, TokenType actualType) {
        if (expectedType == TokenType.EQUALS && actualType == TokenType.PRODUCTION_ARROW) {
            return "Use '=' for terminal definitions, '::=' for production rules";
        } else if (expectedType == TokenType.PRODUCTION_ARROW && actualType == TokenType.EQUALS) {
            return "Use '::=' for production rules, '=' for terminal definitions";
        } else if (expectedType == TokenType.NON_TERMINAL && actualType == TokenType.TERMINAL) {
            return "Non-terminals should be enclosed in angle brackets: <symbol>";
        } else if (expectedType == TokenType.PATTERN && actualType == TokenType.TERMINAL) {
            return "Terminal patterns should be enclosed in braces: {pattern}";
        } else if (expectedType == TokenType.EOF) {
            return "Unexpected end of input - check for missing tokens";
        } else if (actualType == TokenType.EOF) {
            return "Unexpected end of input - expected " + expectedType;
        } else if (expectedType == TokenType.START && actualType != null) {
            return "Expected 'Start' keyword to define grammar start symbol";
        } else if (expectedType == TokenType.PIPE && actualType != null) {
            return "Use '|' to separate production alternatives";
        }
        
        return "Check grammar syntax and token placement";
    }
    
    /**
     * Provides context-aware error messages with line context.
     * 
     * @param message the base error message
     * @param position the error position
     * @param context additional context information
     * @return enhanced error message with context
     */
    private String enhanceErrorMessage(String message, Position position, String context) {
        StringBuilder enhanced = new StringBuilder(message);
        
        if (position != null) {
            enhanced.append(" at line ").append(position.getLine())
                    .append(", column ").append(position.getColumn());
        }
        
        if (context != null && !context.trim().isEmpty()) {
            enhanced.append(" while parsing ").append(context);
        }
        
        return enhanced.toString();
    }
    
    /**
     * Gets the current parsing context for better error messages.
     * 
     * @return a string describing the current parsing context
     */
    private String getCurrentContext() {
        if (recoveryMode) {
            return "recovery mode";
        }
        
        // Look at recent tokens to determine context
        if (currentPosition > 0) {
            Token previous = tokens.get(currentPosition - 1);
            if (previous.isType(TokenType.START)) {
                return "start symbol definition";
            } else if (previous.isType(TokenType.EQUALS)) {
                return "terminal definition";
            } else if (previous.isType(TokenType.PRODUCTION_ARROW)) {
                return "production rule";
            } else if (previous.isType(TokenType.PIPE)) {
                return "production alternative";
            }
        }
        
        return "grammar statement";
    }
    
    /**
     * Gets the current position for error reporting.
     * 
     * @return the current position
     */
    private Position getCurrentPosition() {
        Token current = peek();
        return current.getPosition();
    }
    
    /**
     * Validates the parsed grammar for common issues.
     * 
     * @throws ParseException if validation fails
     */
    private void validateGrammar() throws ParseException {
        validateStartSymbol();
        validateSymbolDefinitions();
        validateProductionRules();
    }
    
    /**
     * Validates that a start symbol is defined and valid.
     * 
     * @throws SemanticErrorException if start symbol validation fails
     */
    private void validateStartSymbol() throws SemanticErrorException {
        if (grammar.getStartSymbol() == null) {
            throw new SemanticErrorException("No start symbol defined. Use 'Start = <NonTerminal>' to define one.", 
                                           null, null, "missing_start_symbol");
        }
        
        // Check if start symbol has productions
        NonTerminal startSymbol = grammar.getStartSymbol();
        boolean hasProductions = grammar.getProductions().stream()
                .anyMatch(p -> p.getLeftSide().equals(startSymbol));
        
        if (!hasProductions) {
            throw new SemanticErrorException("Start symbol has no production rules", 
                                           null, startSymbol.getName(), "undefined_start_symbol");
        }
    }
    
    /**
     * Validates symbol definitions and usage.
     * 
     * @throws SemanticErrorException if symbol validation fails
     */
    private void validateSymbolDefinitions() throws SemanticErrorException {
        // Check for undefined non-terminals (used but not defined)
        Set<String> definedNonTerminals = new HashSet<>();
        Set<String> usedNonTerminals = new HashSet<>();
        
        // Collect defined non-terminals (left-hand sides of productions)
        for (Production production : grammar.getProductions()) {
            definedNonTerminals.add(production.getLeftSide().getName());
        }
        
        // Collect used non-terminals (right-hand sides of productions)
        for (Production production : grammar.getProductions()) {
            for (Symbol symbol : production.getRightSide()) {
                if (symbol instanceof NonTerminal) {
                    usedNonTerminals.add(symbol.getName());
                }
            }
        }
        
        // Check for undefined non-terminals
        for (String used : usedNonTerminals) {
            if (!definedNonTerminals.contains(used)) {
                throw new SemanticErrorException("Non-terminal used but not defined", 
                                               null, used, "undefined_nonterminal");
            }
        }
        
        // Check for unused non-terminals (defined but not used)
        for (String defined : definedNonTerminals) {
            if (!usedNonTerminals.contains(defined) && 
                !defined.equals(grammar.getStartSymbol().getName())) {
                // This is a warning, not an error - we'll just note it
                // Could be enhanced to collect warnings separately
            }
        }
        
        // Check for terminals used but not defined (if they have patterns)
        Set<String> definedTerminals = new HashSet<>();
        Set<String> usedTerminals = new HashSet<>();
        
        for (Terminal terminal : grammar.getTerminals()) {
            if (terminal.getPattern() != null && !terminal.getPattern().isEmpty()) {
                definedTerminals.add(terminal.getName());
            }
        }
        
        for (Production production : grammar.getProductions()) {
            for (Symbol symbol : production.getRightSide()) {
                if (symbol instanceof Terminal) {
                    usedTerminals.add(symbol.getName());
                }
            }
        }
        
        // Note: We don't require all terminals to have patterns as they might be literals
    }
    
    /**
     * Validates production rules for common issues.
     * 
     * @throws SemanticErrorException if production validation fails
     */
    private void validateProductionRules() throws SemanticErrorException {
        if (grammar.getProductions().isEmpty()) {
            throw new SemanticErrorException("Grammar has no production rules", 
                                           null, null, "empty_grammar");
        }
        
        // Check for duplicate productions
        Set<String> productionStrings = new HashSet<>();
        for (Production production : grammar.getProductions()) {
            String productionString = production.toString();
            if (productionStrings.contains(productionString)) {
                throw new SemanticErrorException("Duplicate production rule", 
                                               null, productionString, "duplicate_production");
            }
            productionStrings.add(productionString);
        }
    }
    
    /**
     * Returns the error reporter used by this parser.
     * 
     * @return the error reporter
     */
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }
}