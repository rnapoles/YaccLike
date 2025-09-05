package com.parsergenerator.lexer;

import com.parsergenerator.error.ErrorReporter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lexer for grammar specification files. Tokenizes input text according to
 * the grammar syntax rules, handling terminals, non-terminals, operators,
 * comments, and whitespace.
 */
public class GrammarLexer {
    private final String input;
    private final String filename;
    private final ErrorReporter errorReporter;
    private int position;
    private int line;
    private int column;
    
    // Multi-line detection infrastructure
    private List<Token> tokenBuffer;
    private boolean inMultiLineProduction;
    private int multiLineStartLine;
    
    /**
     * Creates a new grammar lexer for the specified input.
     * 
     * @param input the input text to tokenize
     */
    public GrammarLexer(String input) {
        this(input, null, new ErrorReporter());
    }
    
    /**
     * Creates a new grammar lexer for the specified input and filename.
     * 
     * @param input the input text to tokenize
     * @param filename the source filename for error reporting
     */
    public GrammarLexer(String input, String filename) {
        this(input, filename, new ErrorReporter());
    }
    
    /**
     * Creates a new grammar lexer with the specified error reporter.
     * 
     * @param input the input text to tokenize
     * @param filename the source filename for error reporting
     * @param errorReporter the error reporter to use
     */
    public GrammarLexer(String input, String filename, ErrorReporter errorReporter) {
        this.input = input != null ? input : "";
        this.filename = filename;
        this.errorReporter = errorReporter != null ? errorReporter : new ErrorReporter();
        this.position = 0;
        this.line = 1;
        this.column = 1;
        
        // Initialize multi-line detection infrastructure
        this.tokenBuffer = new ArrayList<>();
        this.inMultiLineProduction = false;
        this.multiLineStartLine = -1;
    }
    
    /**
     * Tokenizes the entire input and returns a list of tokens.
     * 
     * @return list of tokens including EOF token at the end
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        int initialErrorCount = errorReporter.getErrorCount();
        
        try {
            errorReporter.reportInfo(ErrorReporter.ErrorType.LEXICAL, 
                                   "Starting lexical analysis" + (filename != null ? " of " + filename : ""));
            
            while (!isAtEnd()) {
                Token token = nextToken();
                if (token != null && token.getType() != TokenType.EOF) {
                    tokens.add(token);
                }
            }
            
            // Add EOF token
            tokens.add(new Token(TokenType.EOF, "", getCurrentPosition()));
            
            // Report summary
            int newErrors = errorReporter.getErrorCount() - initialErrorCount;
            if (newErrors > 0) {
                errorReporter.reportInfo(ErrorReporter.ErrorType.LEXICAL, 
                    String.format("Lexical analysis completed with %d error(s), %d warning(s)", 
                                newErrors, errorReporter.getWarningCount()));
            } else {
                errorReporter.reportInfo(ErrorReporter.ErrorType.LEXICAL, 
                    String.format("Lexical analysis completed successfully - %d tokens generated", tokens.size()));
            }
            
        } catch (Exception e) {
            errorReporter.reportError(ErrorReporter.Severity.ERROR, 
                                    ErrorReporter.ErrorType.INTERNAL, 
                                    "Unexpected error during tokenization: " + e.getMessage(), 
                                    getCurrentPosition(), 
                                    "Exception: " + e.getClass().getSimpleName(), 
                                    "Check input file format and encoding");
        }
        
        return tokens;
    }
    
    /**
     * Gets the next token from the input stream.
     * 
     * @return the next token, or null if whitespace was skipped
     */
    public Token nextToken() {
        skipWhitespace();
        
        if (isAtEnd()) {
            return new Token(TokenType.EOF, "", getCurrentPosition());
        }
        
        Position startPos = getCurrentPosition();
        char c = peek();
        
        // Handle comments
        if (c == '-' && peekNext() == '-') {
            return readComment(startPos);
        }
        
        // Handle newlines
        if (c == '\n') {
            advance();
            Token token = new Token(TokenType.NEWLINE, "\n", startPos);
            line++;
            column = 1;
            return token;
        }
        
        // Handle patterns first (enclosed in braces)
        if (c == '{') {
            return readPattern(startPos);
        }
        
        // Handle non-terminals (enclosed in angle brackets)
        if (c == '<') {
            return readNonTerminal(startPos);
        }
        
        // Handle production arrow
        if (c == ':' && peekNext() == ':' && peekAhead(2) == '=') {
            advance(); // :
            advance(); // :
            advance(); // =
            return new Token(TokenType.PRODUCTION_ARROW, "::=", startPos);
        }
        
        // Handle single character operators and delimiters
        switch (c) {
            case '=':
                advance();
                return new Token(TokenType.EQUALS, "=", startPos);
            case '|':
                advance();
                return new Token(TokenType.PIPE, "|", startPos);
            case '(':
                advance();
                return new Token(TokenType.LEFT_PAREN, "(", startPos);
            case ')':
                advance();
                return new Token(TokenType.RIGHT_PAREN, ")", startPos);
            case '+':
                advance();
                return new Token(TokenType.PLUS, "+", startPos);
        }
        
        // Handle identifiers (terminals and keywords)
        if (isIdentifierStart(c)) {
            return readIdentifier(startPos);
        }
        
        // Unknown character
        String unknownChar = String.valueOf(c);
        String suggestion = getSuggestionForUnknownCharacter(c);
        errorReporter.reportError(ErrorReporter.Severity.ERROR, 
                                ErrorReporter.ErrorType.LEXICAL,
                                "Unexpected character '" + unknownChar + "'", 
                                startPos,
                                "Character code: " + (int)c,
                                suggestion);
        advance();
        return new Token(TokenType.UNKNOWN, unknownChar, startPos);
    }
    
    private Token readComment(Position startPos) {
        StringBuilder comment = new StringBuilder();
        advance(); // first -
        advance(); // second -
        
        while (!isAtEnd() && peek() != '\n') {
            comment.append(peek());
            advance();
        }
        
        return new Token(TokenType.COMMENT, comment.toString(), startPos);
    }
    
    private Token readPattern(Position startPos) {
        StringBuilder pattern = new StringBuilder();
        advance(); // opening {
        
        while (!isAtEnd()) {
            char c = peek();
            
            if (c == '}') {
                // Found closing brace, end pattern
                break;
            } else if (c == '\\' && position + 1 < input.length()) {
                // Handle escaped character
                pattern.append(c); // Add the backslash
                advance();
                if (!isAtEnd()) {
                    pattern.append(peek()); // Add the escaped character
                    advance();
                }
            } else {
                pattern.append(c);
                advance();
            }
        }
        
        if (isAtEnd()) {
            errorReporter.reportError(ErrorReporter.Severity.ERROR, 
                                    ErrorReporter.ErrorType.LEXICAL,
                                    "Unterminated pattern - missing closing '}'", 
                                    startPos,
                                    "Pattern started at " + startPos,
                                    "Add closing '}' to complete the pattern definition");
        } else {
            advance(); // closing }
        }
        
        return new Token(TokenType.PATTERN, pattern.toString(), startPos);
    }
    
    private Token readNonTerminal(Position startPos) {
        StringBuilder nonTerminal = new StringBuilder();
        advance(); // opening <
        
        while (!isAtEnd() && peek() != '>') {
            nonTerminal.append(peek());
            advance();
        }
        
        if (isAtEnd()) {
            errorReporter.reportError(ErrorReporter.Severity.ERROR, 
                                    ErrorReporter.ErrorType.LEXICAL,
                                    "Unterminated non-terminal - missing closing '>'", 
                                    startPos,
                                    "Non-terminal started at " + startPos,
                                    "Add closing '>' to complete the non-terminal definition");
        } else {
            advance(); // closing >
        }
        
        String value = nonTerminal.toString();
        if (value.trim().isEmpty()) {
            errorReporter.reportError(ErrorReporter.Severity.ERROR, 
                                    ErrorReporter.ErrorType.LEXICAL,
                                    "Empty non-terminal name", 
                                    startPos,
                                    "Non-terminal brackets found but no name provided",
                                    "Provide a valid name between the angle brackets: <name>");
        }
        
        return new Token(TokenType.NON_TERMINAL, value, startPos);
    }
    
    private Token readIdentifier(Position startPos) {
        StringBuilder identifier = new StringBuilder();
        
        while (!isAtEnd() && isIdentifierPart(peek())) {
            identifier.append(peek());
            advance();
        }
        
        String value = identifier.toString();
        
        // Check for keywords
        if ("Start".equals(value)) {
            return new Token(TokenType.START, value, startPos);
        }
        
        // Default to terminal
        return new Token(TokenType.TERMINAL, value, startPos);
    }
    
    private void skipWhitespace() {
        while (!isAtEnd() && isWhitespace(peek()) && peek() != '\n') {
            advance();
        }
    }
    
    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r';
    }
    
    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }
    
    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
    
    /**
     * Returns the current character without advancing the position.
     * 
     * @return the current character, or '\0' if at end of input
     */
    public char peek() {
        if (isAtEnd()) return '\0';
        return input.charAt(position);
    }
    
    private char peekNext() {
        if (position + 1 >= input.length()) return '\0';
        return input.charAt(position + 1);
    }
    
    private char peekAhead(int offset) {
        if (position + offset >= input.length()) return '\0';
        return input.charAt(position + offset);
    }
    
    private char advance() {
        if (isAtEnd()) return '\0';
        char c = input.charAt(position);
        position++;
        column++;
        return c;
    }
    
    /**
     * Returns whether the lexer has reached the end of input.
     * 
     * @return true if at end of input
     */
    public boolean isAtEnd() {
        return position >= input.length();
    }
    
    /**
     * Returns the current position in the input stream.
     * 
     * @return the current position
     */
    public Position getCurrentPosition() {
        return new Position(line, column, filename);
    }
    
    /**
     * Returns the error reporter used by this lexer.
     * 
     * @return the error reporter
     */
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }
    
    /**
     * Returns whether any lexical errors were encountered.
     * 
     * @return true if errors were reported
     */
    public boolean hasErrors() {
        return errorReporter.hasErrors();
    }
    
    /**
     * Provides suggestions for unknown characters based on common mistakes.
     * 
     * @param c the unknown character
     * @return a helpful suggestion
     */
    private String getSuggestionForUnknownCharacter(char c) {
        switch (c) {
            case '[':
            case ']':
                return "Use angle brackets < > for non-terminals, not square brackets";
            case '"':
            case '\'':
                return "Use braces { } for terminal patterns, not quotes";
            case ';':
                return "Grammar statements are separated by newlines, not semicolons";
            case ',':
                return "Use spaces to separate symbols in productions, not commas";
            case '.':
                return "Check if this should be part of a terminal pattern enclosed in braces";
            case '&':
                return "Use '::=' for production rules, not '&'";
            case '-':
                if (peekNext() != '-') {
                    return "Use '--' for comments, single '-' is not valid";
                }
                break;
            default:
                if (Character.isLetter(c) || Character.isDigit(c)) {
                    return "This character might belong to an identifier or pattern";
                } else if (Character.isWhitespace(c)) {
                    return "Unexpected whitespace character - check for non-standard spaces";
                } else {
                    return "Remove this character or enclose it in a terminal pattern { }";
                }
        }
        return "Check grammar syntax and remove invalid characters";
    }
    
    /**
     * Checks if the current line continues a production rule from the previous line.
     * This method detects multi-line production patterns by looking for:
     * 1. Lines that start with a pipe (|) character (after optional whitespace)
     * 2. Previous lines that end with a pipe character
     * 
     * @return true if the current position indicates a production continuation
     */
    private boolean isProductionContinuation() {
        if (isAtEnd()) {
            return false;
        }
        
        // Save current position to restore later
        int savedPosition = position;
        int savedLine = line;
        int savedColumn = column;
        
        try {
            // Skip any leading whitespace on the current line
            while (!isAtEnd() && isWhitespace(peek()) && peek() != '\n') {
                advance();
            }
            
            // Check if we find a pipe character at the start of the line (after whitespace)
            if (!isAtEnd() && peek() == '|') {
                return true;
            }
            
            return false;
        } finally {
            // Restore position
            position = savedPosition;
            line = savedLine;
            column = savedColumn;
        }
    }
    
    /**
     * Checks if the current position is at the beginning of a line.
     * This considers the position to be at line start if:
     * 1. We're at the very beginning of input (position 0)
     * 2. The previous character was a newline (\n)
     * 
     * @return true if the current position is at the start of a line
     */
    private boolean isAtLineStart() {
        if (position == 0) {
            return true;
        }
        
        if (position > 0 && position <= input.length()) {
            char prevChar = input.charAt(position - 1);
            return prevChar == '\n';
        }
        
        return false;
    }
    
    /**
     * Detects if the current line or previous line contains patterns that indicate
     * a multi-line production rule continuation. This method identifies:
     * 1. Lines ending with a pipe (|) character (trailing pipe pattern)
     * 2. Lines that appear to have incomplete production syntax
     * 3. Current line starting with a pipe character (leading pipe pattern)
     * 
     * @return true if a continuation pattern is detected
     */
    private boolean detectContinuationPattern() {
        if (isAtEnd()) {
            return false;
        }
        
        // Save current position to restore later
        int savedPosition = position;
        int savedLine = line;
        int savedColumn = column;
        
        try {
            // First check if current line starts with a pipe (after optional whitespace)
            if (isProductionContinuation()) {
                return true;
            }
            
            // Check for trailing pipe pattern on previous line
            // We need to look backwards to find the previous line
            if (position > 0) {
                // Find the start of current line
                int currentLineStart = position;
                while (currentLineStart > 0 && input.charAt(currentLineStart - 1) != '\n') {
                    currentLineStart--;
                }
                
                // If we're not at the first line, check the previous line
                if (currentLineStart > 0) {
                    // Find the start of previous line
                    int prevLineStart = currentLineStart - 1; // Skip the newline
                    while (prevLineStart > 0 && input.charAt(prevLineStart - 1) != '\n') {
                        prevLineStart--;
                    }
                    
                    // Extract the previous line content
                    String prevLine = input.substring(prevLineStart, currentLineStart - 1);
                    
                    // Check if previous line ends with pipe (after trimming whitespace)
                    String trimmedPrevLine = prevLine.trim();
                    if (trimmedPrevLine.endsWith("|")) {
                        return true;
                    }
                    
                    // Check for incomplete production patterns
                    if (isIncompleteProduction(trimmedPrevLine)) {
                        return true;
                    }
                }
            }
            
            return false;
        } finally {
            // Restore position
            position = savedPosition;
            line = savedLine;
            column = savedColumn;
        }
    }
    
    /**
     * Checks if a line contains an incomplete production rule that suggests
     * continuation on the next line. This includes:
     * 1. Lines ending with production arrow (::=) but no alternatives
     * 2. Lines with production arrow but incomplete right-hand side
     * 3. Lines ending with operators that suggest more content follows
     * 
     * @param line the line content to check (trimmed)
     * @return true if the line appears to be an incomplete production
     */
    private boolean isIncompleteProduction(String line) {
        if (line.isEmpty()) {
            return false;
        }
        
        // Check if line ends with production arrow (suggests alternatives follow)
        if (line.endsWith("::=")) {
            return true;
        }
        
        // Check if line contains production arrow but seems incomplete
        if (line.contains("::=")) {
            String rightSide = line.substring(line.indexOf("::=") + 3).trim();
            // If right side is empty or ends with certain operators, it might be incomplete
            if (rightSide.isEmpty() || rightSide.endsWith("|")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a character is any type of whitespace including newlines.
     * This method recognizes all whitespace characters: space (\s), tab (\t), 
     * carriage return (\r), and newline (\n).
     * 
     * @param c the character to check
     * @return true if the character is any type of whitespace
     */
    private boolean isAnyWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }
    
    /**
     * Skips leading whitespace on a continuation line while preserving
     * the lexer's position tracking. This method is specifically designed
     * for handling whitespace normalization when processing multi-line
     * production rules.
     * 
     * This method skips spaces, tabs, and carriage returns but stops at:
     * - Newline characters (\n)
     * - End of input
     * - Non-whitespace characters
     */
    private void skipLeadingWhitespaceOnContinuationLine() {
        while (!isAtEnd() && isWhitespace(peek()) && peek() != '\n') {
            advance();
        }
    }
}