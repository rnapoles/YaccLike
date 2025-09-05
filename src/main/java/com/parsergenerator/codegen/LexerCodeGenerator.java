package com.parsergenerator.codegen;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.symbol.Terminal;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Generates Java lexer code from a grammar's terminal definitions.
 * This class produces complete lexer implementations including:
 * - Main lexer class with tokenization logic
 * - Tokenizer engine with pattern matching
 * - Lexical exception handling
 * - Position tracking capabilities
 */
public class LexerCodeGenerator {
    private final TemplateManager templateManager;
    private final String packageName;
    private final String grammarName;
    
    // Template names for lexer generation
    public static final String LEXER_CLASS_TEMPLATE = "lexer_class";
    public static final String TOKENIZER_ENGINE_TEMPLATE = "tokenizer_engine";
    public static final String LEXICAL_EXCEPTION_TEMPLATE = "lexical_exception";
    
    /**
     * Creates a new LexerCodeGenerator with default package and grammar names.
     */
    public LexerCodeGenerator() {
        this("com.generated.lexer", "GeneratedGrammar");
    }
    
    /**
     * Creates a new LexerCodeGenerator with specified package and grammar names.
     *
     * @param packageName the package name for generated classes
     * @param grammarName the name of the grammar (used in class names)
     */
    public LexerCodeGenerator(String packageName, String grammarName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }
        if (grammarName == null || grammarName.trim().isEmpty()) {
            throw new IllegalArgumentException("Grammar name cannot be null or empty");
        }
        
        this.templateManager = new TemplateManager();
        this.packageName = packageName.trim();
        this.grammarName = grammarName.trim();
    }
    
    /**
     * Creates a new LexerCodeGenerator with specified template manager, package and grammar names.
     * This constructor allows sharing template manager with other code generators.
     *
     * @param templateManager the template manager to use
     * @param packageName the package name for generated classes
     * @param grammarName the name of the grammar (used in class names)
     */
    public LexerCodeGenerator(TemplateManager templateManager, String packageName, String grammarName) {
        if (templateManager == null) {
            throw new IllegalArgumentException("Template manager cannot be null");
        }
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }
        if (grammarName == null || grammarName.trim().isEmpty()) {
            throw new IllegalArgumentException("Grammar name cannot be null or empty");
        }
        
        this.templateManager = templateManager;
        this.packageName = packageName.trim();
        this.grammarName = grammarName.trim();
    }
    
    /**
     * Generates complete Java lexer code from the given grammar.
     *
     * @param grammar the grammar to generate lexer for
     * @return a GeneratedCode object containing all generated lexer files
     * @throws TemplateException if code generation fails
     */
    public GeneratedCode generateLexer(Grammar grammar) throws TemplateException {
        if (grammar == null) {
            throw new IllegalArgumentException("Grammar cannot be null");
        }
        
        GeneratedCode result = new GeneratedCode();
        
        // Generate main lexer class
        String lexerCode = generateLexerClass(grammar);
        result.addFile(grammarName + "Lexer.java", lexerCode);
        
        // Note: LexicalException already exists in com.parsergenerator.lexer package
        // and will be imported by the generated lexer class
        
        return result;
    }
    
    /**
     * Generates the main lexer class with tokenization logic.
     *
     * @param grammar the grammar containing terminal definitions
     * @return the generated lexer class code
     * @throws TemplateException if template rendering fails
     */
    public String generateLexerClass(Grammar grammar) throws TemplateException {
        if (grammar == null) {
            throw new IllegalArgumentException("Grammar cannot be null");
        }
        
        Map<String, Object> variables = new HashMap<>();
        
        variables.put("packageName", packageName);
        variables.put("grammarName", grammarName);
        variables.put("className", grammarName + "Lexer");
        
        // Prepare terminal information for template
        List<TerminalInfo> terminals = prepareTerminalInfo(grammar);
        variables.put("terminals", terminals);
        
        // Generate terminal order for longest-match tokenization
        List<String> terminalOrder = generateTerminalOrder(terminals);
        variables.put("terminalOrder", terminalOrder);
        
        // Check if template exists, if not, generate basic structure
        if (templateManager.hasTemplate(LEXER_CLASS_TEMPLATE)) {
            return templateManager.renderTemplate(LEXER_CLASS_TEMPLATE, variables);
        } else {
            // Generate basic lexer structure without template for now
            return generateBasicLexerClass(variables);
        }
    }
    
    /**
     * Generates the tokenizer engine with pattern matching logic.
     *
     * @param grammar the grammar containing terminal definitions
     * @return the generated tokenizer engine code
     * @throws TemplateException if template rendering fails
     */
    public String generateTokenizerEngine(Grammar grammar) throws TemplateException {
        if (grammar == null) {
            throw new IllegalArgumentException("Grammar cannot be null");
        }
        
        Map<String, Object> variables = new HashMap<>();
        
        variables.put("packageName", packageName);
        variables.put("grammarName", grammarName);
        
        // Prepare terminal patterns
        List<TerminalInfo> terminals = prepareTerminalInfo(grammar);
        variables.put("terminals", terminals);
        
        // Check if template exists, if not, generate basic structure
        if (templateManager.hasTemplate(TOKENIZER_ENGINE_TEMPLATE)) {
            return templateManager.renderTemplate(TOKENIZER_ENGINE_TEMPLATE, variables);
        } else {
            // Generate basic tokenizer engine structure without template for now
            return generateBasicTokenizerEngine(variables);
        }
    }
    
    /**
     * Generates the lexical exception class for error handling.
     *
     * @return the generated lexical exception code
     * @throws TemplateException if template rendering fails
     */
    public String generateLexicalException() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", packageName);
        
        // Check if template exists, if not, generate basic structure
        if (templateManager.hasTemplate(LEXICAL_EXCEPTION_TEMPLATE)) {
            return templateManager.renderTemplate(LEXICAL_EXCEPTION_TEMPLATE, variables);
        } else {
            // Generate basic exception class structure without template for now
            return generateBasicLexicalException(variables);
        }
    }
    
    /**
     * Prepares terminal information for template rendering.
     * This method validates all terminal patterns and throws an exception
     * if any pattern is invalid or if no valid patterns are found.
     *
     * @param grammar the grammar containing terminals
     * @return list of terminal information objects
     * @throws TemplateException if any terminal pattern is invalid or no valid patterns found
     */
    private List<TerminalInfo> prepareTerminalInfo(Grammar grammar) throws TemplateException {
        List<TerminalInfo> terminalInfos = new ArrayList<>();
        
        for (Terminal terminal : grammar.getTerminals()) {
            // Only validate and include terminals that have patterns
            if (terminal.hasPattern()) {
                validateTerminalPattern(terminal);
                terminalInfos.add(createTerminalInfo(terminal));
            }
        }
        
        // Throw exception if no terminals with valid patterns were found
        if (terminalInfos.isEmpty()) {
            throw new TemplateException("No terminals with valid patterns found in grammar. " +
                    "Lexer generation requires at least one terminal with a non-empty pattern.");
        }
        
        return terminalInfos;
    }
    
    /**
     * Creates terminal information object for template rendering.
     *
     * @param terminal the terminal symbol
     * @return terminal information object
     */
    private TerminalInfo createTerminalInfo(Terminal terminal) {
        return new TerminalInfo(
            terminal.getName(),
            terminal.getPattern(),
            toConstantName(terminal.getName()),
            toEnumConstant(terminal.getName())
        );
    }
    
    /**
     * Generates terminal order for longest-match tokenization strategy.
     * Orders terminals by pattern specificity and length to ensure
     * longest and most specific matches are tried first.
     * Keywords and literal patterns are prioritized over general patterns.
     *
     * @param terminals list of terminal information
     * @return ordered list of terminal names for optimal matching
     */
    private List<String> generateTerminalOrder(List<TerminalInfo> terminals) {
        return terminals.stream()
                .sorted((t1, t2) -> {
                    // First, prioritize keywords and literals over general patterns
                    boolean t1IsKeywordOrLiteral = isKeywordOrLiteral(t1);
                    boolean t2IsKeywordOrLiteral = isKeywordOrLiteral(t2);
                    
                    if (t1IsKeywordOrLiteral && !t2IsKeywordOrLiteral) {
                        return -1; // t1 comes first
                    } else if (!t1IsKeywordOrLiteral && t2IsKeywordOrLiteral) {
                        return 1; // t2 comes first
                    }
                    
                    // If both are keywords/literals or both are general patterns,
                    // sort by pattern specificity (more specific patterns first)
                    int specificityCompare = Integer.compare(t2.getSpecificity(), t1.getSpecificity());
                    if (specificityCompare != 0) {
                        return specificityCompare;
                    }
                    
                    // Then by pattern length (longer patterns first for longest-match)
                    int lengthCompare = Integer.compare(t2.getPattern().length(), t1.getPattern().length());
                    if (lengthCompare != 0) {
                        return lengthCompare;
                    }
                    
                    // Finally, alphabetical order for consistency
                    return t1.getName().compareTo(t2.getName());
                })
                .map(TerminalInfo::getName)
                .collect(Collectors.toList());
    }
    
    /**
     * Determines if a terminal represents a keyword or literal pattern.
     * Keywords and literals should be matched before general patterns like identifiers.
     *
     * @param terminal the terminal to check
     * @return true if the terminal is a keyword or literal, false otherwise
     */
    private boolean isKeywordOrLiteral(TerminalInfo terminal) {
        String pattern = terminal.getPattern();
        String name = terminal.getName();
        
        // Check if it's a keyword (name starts with KEYWORD_ or pattern is a simple literal)
        if (name.startsWith("KEYWORD_")) {
            return true;
        }
        
        // Check if pattern is a simple literal (no regex metacharacters except escapes)
        if (pattern != null && !pattern.isEmpty()) {
            // Remove escaped characters for analysis
            String unescaped = pattern.replaceAll("\\\\.", "X");
            
            // If no regex metacharacters remain, it's a literal
            boolean hasMetaChars = unescaped.matches(".*[\\[\\]\\(\\)\\{\\}\\*\\+\\?\\|\\^\\$\\.].*");
            if (!hasMetaChars) {
                return true;
            }
            
            // Check for simple escaped operators (like \+, \*, etc.)
            if (pattern.matches("\\\\[+\\-*/.=<>!&|(){}\\[\\];,]")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Generates basic lexer class structure without template.
     * This is a fallback method when templates are not available.
     */
    private String generateBasicLexerClass(Map<String, Object> variables) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("package ").append(variables.get("packageName")).append(";\n\n");
        sb.append("import java.util.*;\n");
        sb.append("import java.util.regex.Pattern;\n");
        sb.append("import java.util.regex.Matcher;\n");
        sb.append("import com.parsergenerator.lexer.Token;\n");
        sb.append("import com.parsergenerator.lexer.TokenType;\n");
        sb.append("import com.parsergenerator.lexer.Position;\n");
        sb.append("import com.parsergenerator.lexer.LexicalException;\n\n");
        
        sb.append("/**\n");
        sb.append(" * Generated lexer class for ").append(variables.get("grammarName")).append(" grammar.\n");
        sb.append(" * This class tokenizes input text according to terminal definitions with\n");
        sb.append(" * comprehensive position tracking and error reporting.\n");
        sb.append(" */\n");
        sb.append("public class ").append(variables.get("className")).append(" {\n");
        sb.append("    private final String input;\n");
        sb.append("    private final String filename;\n");
        sb.append("    private int position;\n");
        sb.append("    private int line;\n");
        sb.append("    private int column;\n");
        sb.append("    private int startPosition;\n");
        sb.append("    private int startLine;\n");
        sb.append("    private int startColumn;\n");
        sb.append("    private boolean hasErrors;\n");
        sb.append("    private final List<LexicalException> errors;\n\n");
        
        // Add terminal patterns
        @SuppressWarnings("unchecked")
        List<TerminalInfo> terminals = (List<TerminalInfo>) variables.get("terminals");
        if (terminals != null) {
            for (TerminalInfo terminal : terminals) {
                sb.append("    private static final Pattern ").append(terminal.getConstantName())
                  .append(" = Pattern.compile(\"").append(escapeJavaString(terminal.getPattern())).append("\");\n");
            }
        }
        
        // Add whitespace pattern for skipping
        sb.append("    private static final Pattern WHITESPACE_PATTERN = Pattern.compile(\"\\\\s+\");\n");
        
        // Add terminal order array
        @SuppressWarnings("unchecked")
        List<String> terminalOrder = (List<String>) variables.get("terminalOrder");
        if (terminalOrder != null && !terminalOrder.isEmpty()) {
            sb.append("\n    // Terminal matching order (longest match first)\n");
            sb.append("    private static final String[] TERMINAL_ORDER = {\n");
            for (int i = 0; i < terminalOrder.size(); i++) {
                sb.append("        \"").append(terminalOrder.get(i)).append("\"");
                if (i < terminalOrder.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("    };\n");
        }
        
        sb.append("\n    public ").append(variables.get("className")).append("(String input) {\n");
        sb.append("        this(input, null);\n");
        sb.append("    }\n\n");
        
        sb.append("    public ").append(variables.get("className")).append("(String input, String filename) {\n");
        sb.append("        this.input = input != null ? input : \"\";\n");
        sb.append("        this.filename = filename;\n");
        sb.append("        this.position = 0;\n");
        sb.append("        this.line = 1;\n");
        sb.append("        this.column = 1;\n");
        sb.append("        this.startPosition = 0;\n");
        sb.append("        this.startLine = 1;\n");
        sb.append("        this.startColumn = 1;\n");
        sb.append("        this.hasErrors = false;\n");
        sb.append("        this.errors = new ArrayList<>();\n");
        sb.append("    }\n\n");
        
        // Add position tracking methods
        sb.append("    /**\n");
        sb.append("     * Gets the current position in the input.\n");
        sb.append("     * @return the current position\n");
        sb.append("     */\n");
        sb.append("    public Position getCurrentPosition() {\n");
        sb.append("        return new Position(line, column, filename);\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Gets the position where the current token started.\n");
        sb.append("     * @return the start position of the current token\n");
        sb.append("     */\n");
        sb.append("    public Position getStartPosition() {\n");
        sb.append("        return new Position(startLine, startColumn, filename);\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Checks if the lexer has encountered any errors.\n");
        sb.append("     * @return true if errors were encountered\n");
        sb.append("     */\n");
        sb.append("    public boolean hasErrors() {\n");
        sb.append("        return hasErrors;\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Gets all lexical errors encountered during tokenization.\n");
        sb.append("     * @return list of lexical errors\n");
        sb.append("     */\n");
        sb.append("    public List<LexicalException> getErrors() {\n");
        sb.append("        return new ArrayList<>(errors);\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Checks if there are more tokens to process.\n");
        sb.append("     * @return true if more tokens are available\n");
        sb.append("     */\n");
        sb.append("    public boolean hasMoreTokens() {\n");
        sb.append("        return position < input.length();\n");
        sb.append("    }\n\n");
        
        // Add position advancement methods
        sb.append("    /**\n");
        sb.append("     * Advances position by the specified number of characters.\n");
        sb.append("     * Updates line and column tracking appropriately.\n");
        sb.append("     * @param count number of characters to advance\n");
        sb.append("     */\n");
        sb.append("    private void advancePosition(int count) {\n");
        sb.append("        for (int i = 0; i < count && position < input.length(); i++) {\n");
        sb.append("            char c = input.charAt(position);\n");
        sb.append("            position++;\n");
        sb.append("            if (c == '\\n') {\n");
        sb.append("                line++;\n");
        sb.append("                column = 1;\n");
        sb.append("            } else if (c == '\\r') {\n");
        sb.append("                // Handle \\r\\n and standalone \\r\n");
        sb.append("                if (position < input.length() && input.charAt(position) == '\\n') {\n");
        sb.append("                    position++;\n");
        sb.append("                    i++; // Count the \\n as well\n");
        sb.append("                }\n");
        sb.append("                line++;\n");
        sb.append("                column = 1;\n");
        sb.append("            } else {\n");
        sb.append("                column++;\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Marks the start of a new token at the current position.\n");
        sb.append("     */\n");
        sb.append("    private void markTokenStart() {\n");
        sb.append("        startPosition = position;\n");
        sb.append("        startLine = line;\n");
        sb.append("        startColumn = column;\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Skips whitespace characters and updates position tracking.\n");
        sb.append("     */\n");
        sb.append("    private void skipWhitespace() {\n");
        sb.append("        while (position < input.length()) {\n");
        sb.append("            Matcher matcher = WHITESPACE_PATTERN.matcher(input);\n");
        sb.append("            matcher.region(position, input.length());\n");
        sb.append("            if (matcher.lookingAt()) {\n");
        sb.append("                advancePosition(matcher.end() - matcher.start());\n");
        sb.append("            } else {\n");
        sb.append("                break;\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
        
        // Add error reporting method
        sb.append("    /**\n");
        sb.append("     * Reports a lexical error with detailed position information and suggestions.\n");
        sb.append("     * @param message the error message\n");
        sb.append("     * @param unexpectedInput the input that caused the error\n");
        sb.append("     * @param suggestion optional suggestion for fixing the error\n");
        sb.append("     */\n");
        sb.append("    private void reportError(String message, String unexpectedInput, String suggestion) {\n");
        sb.append("        Position errorPos = new Position(line, column, filename);\n");
        sb.append("        LexicalException error = new LexicalException(message, errorPos, unexpectedInput, suggestion);\n");
        sb.append("        errors.add(error);\n");
        sb.append("        hasErrors = true;\n");
        sb.append("    }\n\n");
        
        // Add next token method
        sb.append("    /**\n");
        sb.append("     * Gets the next token from the input.\n");
        sb.append("     * @return the next token, or EOF token if no more input\n");
        sb.append("     * @throws LexicalException if tokenization fails\n");
        sb.append("     */\n");
        sb.append("    public Token nextToken() throws LexicalException {\n");
        sb.append("        skipWhitespace();\n");
        sb.append("        \n");
        sb.append("        if (position >= input.length()) {\n");
        sb.append("            return new Token(TokenType.EOF, \"\", getCurrentPosition());\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        markTokenStart();\n");
        sb.append("        \n");
        if (terminals != null && !terminals.isEmpty()) {
            sb.append("        // Try to match each terminal pattern in order\n");
            sb.append("        String remainingInput = input.substring(position);\n");
            sb.append("        \n");
            for (TerminalInfo terminal : terminals) {
                sb.append("        // Try to match ").append(terminal.getName()).append("\n");
                sb.append("        Matcher ").append(terminal.getName().toLowerCase()).append("Matcher = ")
                  .append(terminal.getConstantName()).append(".matcher(remainingInput);\n");
                sb.append("        if (").append(terminal.getName().toLowerCase()).append("Matcher.lookingAt()) {\n");
                sb.append("            String matchedText = ").append(terminal.getName().toLowerCase())
                  .append("Matcher.group();\n");
                sb.append("            advancePosition(matchedText.length());\n");
                sb.append("            return new Token(TokenType.").append(terminal.getEnumConstant()).append(", matchedText, getStartPosition());\n");
                sb.append("        }\n");
                sb.append("        \n");
            }
            sb.append("        // No pattern matched - report error\n");
        } else {
            sb.append("        // No terminal patterns defined - this lexer cannot tokenize input\n");
            sb.append("        // This is likely a grammar that doesn't use pattern-based terminals\n");
        }
        
        sb.append("        char unexpectedChar = input.charAt(position);\n");
        sb.append("        String unexpectedInput = String.valueOf(unexpectedChar);\n");
        sb.append("        String suggestion = generateSuggestion(unexpectedChar);\n");
        sb.append("        \n");
        sb.append("        reportError(\"Unexpected character: '\" + unexpectedChar + \"'\", \n");
        sb.append("                   unexpectedInput, suggestion);\n");
        sb.append("        \n");
        sb.append("        // Advance past the problematic character to continue tokenization\n");
        sb.append("        advancePosition(1);\n");
        sb.append("        \n");
        sb.append("        throw new LexicalException(\"Unexpected character: '\" + unexpectedChar + \"'\", \n");
        sb.append("                                  getStartPosition(), unexpectedInput, suggestion);\n");
        sb.append("    }\n\n");
        
        // Add tokenize method
        sb.append("    /**\n");
        sb.append("     * Tokenizes the entire input and returns a list of tokens.\n");
        sb.append("     * @return list of all tokens in the input\n");
        sb.append("     * @throws LexicalException if tokenization fails\n");
        sb.append("     */\n");
        sb.append("    public List<Token> tokenize() throws LexicalException {\n");
        sb.append("        List<Token> tokens = new ArrayList<>();\n");
        sb.append("        \n");
        sb.append("        while (hasMoreTokens()) {\n");
        sb.append("            Token token = nextToken();\n");
        sb.append("            if (token.getType() != TokenType.EOF) {\n");
        sb.append("                tokens.add(token);\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        // Add EOF token\n");
        sb.append("        tokens.add(new Token(TokenType.EOF, \"\", getCurrentPosition()));\n");
        sb.append("        \n");
        sb.append("        return tokens;\n");
        sb.append("    }\n\n");
        
        // Add suggestion generation method
        sb.append("    /**\n");
        sb.append("     * Generates helpful suggestions for unexpected characters.\n");
        sb.append("     * @param unexpectedChar the character that caused the error\n");
        sb.append("     * @return a suggestion string, or null if no suggestion available\n");
        sb.append("     */\n");
        sb.append("    private String generateSuggestion(char unexpectedChar) {\n");
        sb.append("        switch (unexpectedChar) {\n");
        sb.append("            case '\"':\n");
        sb.append("                return \"Did you mean to use single quotes instead?\";\n");
        sb.append("            case '\\'':\n");
        sb.append("                return \"Did you mean to use double quotes instead?\";\n");
        sb.append("            case '{':\n");
        sb.append("            case '}':\n");
        sb.append("                return \"Curly braces might not be supported in this grammar\";\n");
        sb.append("            case '[':\n");
        sb.append("            case ']':\n");
        sb.append("                return \"Square brackets might not be supported in this grammar\";\n");
        sb.append("            case '@':\n");
        sb.append("            case '#':\n");
        sb.append("            case '$':\n");
        sb.append("            case '%':\n");
        sb.append("                return \"Special characters like '\" + unexpectedChar + \"' might not be supported\";\n");
        sb.append("            default:\n");
        sb.append("                if (Character.isLetter(unexpectedChar)) {\n");
        sb.append("                    return \"Check if this identifier is properly defined in the grammar\";\n");
        sb.append("                } else if (Character.isDigit(unexpectedChar)) {\n");
        sb.append("                    return \"Check if numeric literals are properly defined in the grammar\";\n");
        sb.append("                } else {\n");
        sb.append("                    return \"This character might not be supported by the grammar\";\n");
        sb.append("                }\n");
        sb.append("        }\n");
        sb.append("    }\n");
        
        sb.append("}\n");
        
        return sb.toString();
    }
    
    /**
     * Generates basic tokenizer engine structure without template.
     */
    private String generateBasicTokenizerEngine(Map<String, Object> variables) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("package ").append(variables.get("packageName")).append(";\n\n");
        sb.append("import java.util.*;\n");
        sb.append("import java.util.regex.Pattern;\n\n");
        
        sb.append("/**\n");
        sb.append(" * Tokenizer engine for pattern matching.\n");
        sb.append(" */\n");
        sb.append("public class TokenizerEngine {\n");
        sb.append("    // TODO: Implement tokenizer engine\n");
        sb.append("    // This will be completed in subsequent tasks\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    /**
     * Generates basic lexical exception structure without template.
     * Note: This is a fallback - the main LexicalException class already exists
     * in com.parsergenerator.lexer package and should be used instead.
     */
    private String generateBasicLexicalException(Map<String, Object> variables) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("package ").append(variables.get("packageName")).append(";\n\n");
        sb.append("import com.parsergenerator.lexer.Position;\n\n");
        
        sb.append("/**\n");
        sb.append(" * Exception thrown when lexical analysis fails.\n");
        sb.append(" * This exception provides detailed information about the location and nature\n");
        sb.append(" * of lexical errors encountered during tokenization.\n");
        sb.append(" */\n");
        sb.append("public class LexicalException extends Exception {\n");
        sb.append("    private final int line;\n");
        sb.append("    private final int column;\n");
        sb.append("    private final String unexpectedInput;\n");
        sb.append("    private final String suggestion;\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Creates a new LexicalException with the specified message and position.\n");
        sb.append("     */\n");
        sb.append("    public LexicalException(String message, int line, int column, String unexpectedInput) {\n");
        sb.append("        this(message, line, column, unexpectedInput, null);\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Creates a new LexicalException with the specified message, position, and suggestion.\n");
        sb.append("     */\n");
        sb.append("    public LexicalException(String message, int line, int column, String unexpectedInput, String suggestion) {\n");
        sb.append("        super(message);\n");
        sb.append("        this.line = line;\n");
        sb.append("        this.column = column;\n");
        sb.append("        this.unexpectedInput = unexpectedInput;\n");
        sb.append("        this.suggestion = suggestion;\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Creates a new LexicalException from a Position object.\n");
        sb.append("     */\n");
        sb.append("    public LexicalException(String message, Position position, String unexpectedInput) {\n");
        sb.append("        this(message, position.getLine(), position.getColumn(), unexpectedInput, null);\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Creates a new LexicalException from a Position object with suggestion.\n");
        sb.append("     */\n");
        sb.append("    public LexicalException(String message, Position position, String unexpectedInput, String suggestion) {\n");
        sb.append("        this(message, position.getLine(), position.getColumn(), unexpectedInput, suggestion);\n");
        sb.append("    }\n\n");
        
        sb.append("    public int getLine() { return line; }\n");
        sb.append("    public int getColumn() { return column; }\n");
        sb.append("    public String getUnexpectedInput() { return unexpectedInput; }\n");
        sb.append("    public String getSuggestion() { return suggestion; }\n\n");
        
        sb.append("    public boolean hasSuggestion() {\n");
        sb.append("        return suggestion != null && !suggestion.trim().isEmpty();\n");
        sb.append("    }\n\n");
        
        sb.append("    public Position getPosition() {\n");
        sb.append("        return new Position(line, column);\n");
        sb.append("    }\n\n");
        
        sb.append("    @Override\n");
        sb.append("    public String toString() {\n");
        sb.append("        StringBuilder sb = new StringBuilder();\n");
        sb.append("        sb.append(\"LexicalException at \").append(line).append(\":\").append(column);\n");
        sb.append("        sb.append(\": \").append(getMessage());\n");
        sb.append("        \n");
        sb.append("        if (unexpectedInput != null && !unexpectedInput.isEmpty()) {\n");
        sb.append("            sb.append(\" (unexpected input: '\").append(unexpectedInput).append(\"')\");\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        if (hasSuggestion()) {\n");
        sb.append("            sb.append(\" - Suggestion: \").append(suggestion);\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        return sb.toString();\n");
        sb.append("    }\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    /**
     * Validates a terminal pattern for regex syntax and compilation.
     *
     * @param terminal the terminal to validate
     * @throws TemplateException if the pattern is invalid
     */
    private void validateTerminalPattern(Terminal terminal) throws TemplateException {
        String pattern = terminal.getPattern();
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new TemplateException("Terminal '" + terminal.getName() + "' has empty pattern");
        }
        
        try {
            // Try to compile the pattern to validate syntax
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new TemplateException("Invalid regex pattern for terminal '" + terminal.getName() + 
                    "': " + e.getMessage() + " at position " + e.getIndex());
        }
        
        // Additional validation for common issues
        validatePatternFeatures(terminal.getName(), pattern);
    }
    
    /**
     * Validates specific pattern features and provides warnings for potential issues.
     *
     * @param terminalName the name of the terminal
     * @param pattern the pattern to validate
     * @throws TemplateException if the pattern has unsupported features
     */
    private void validatePatternFeatures(String terminalName, String pattern) throws TemplateException {
        // Check for potentially problematic patterns
        if (pattern.contains(".*") || pattern.contains(".+")) {
            // Warn about greedy quantifiers that might cause issues
            System.out.println("Warning: Terminal '" + terminalName + "' uses greedy quantifiers (.*/.+) " +
                    "which may cause tokenization issues. Consider using non-greedy alternatives.");
        }
        
        if (pattern.matches(".*\\\\[0-9]+.*")) {
            // Check for backreferences which might not work as expected in tokenization
            System.out.println("Warning: Terminal '" + terminalName + "' appears to use backreferences " +
                    "which may not work correctly in lexical analysis.");
        }
        
        // Check for anchors that don't make sense in tokenization context
        if (pattern.startsWith("^") || pattern.endsWith("$")) {
            System.out.println("Warning: Terminal '" + terminalName + "' uses line anchors (^/$) " +
                    "which may not behave as expected in tokenization. Consider removing them.");
        }
    }
    
    /**
     * Compares two patterns by specificity for ordering in longest-match tokenization.
     * More specific patterns should be tried before less specific ones.
     *
     * @param pattern1 the first pattern
     * @param pattern2 the second pattern
     * @return negative if pattern1 is more specific, positive if pattern2 is more specific, 0 if equal
     */
    private int comparePatternSpecificity(String pattern1, String pattern2) {
        int specificity1 = calculatePatternSpecificity(pattern1);
        int specificity2 = calculatePatternSpecificity(pattern2);
        return Integer.compare(specificity1, specificity2);
    }
    
    /**
     * Calculates a specificity score for a regex pattern.
     * Higher scores indicate more specific patterns that should be tried first.
     *
     * @param pattern the pattern to analyze
     * @return specificity score (higher = more specific)
     */
    private int calculatePatternSpecificity(String pattern) {
        // Create a temporary TerminalInfo to use its specificity calculation
        try {
            TerminalInfo temp = new TerminalInfo("temp", pattern, "TEMP", "TEMP");
            return temp.getSpecificity();
        } catch (IllegalArgumentException e) {
            // If pattern is invalid, return lowest specificity
            return Integer.MIN_VALUE;
        }
    }
    
    /**
     * Converts a terminal name to a valid Java constant name.
     */
    private String toConstantName(String name) {
        if (name == null || name.isEmpty()) {
            return "UNKNOWN_PATTERN";
        }
        
        String cleaned = name.replaceAll("[^a-zA-Z0-9_]", "_")
                             .replaceAll("_+", "_")
                             .replaceAll("^_|_$", "")
                             .toUpperCase();
        
        if (cleaned.isEmpty() || Character.isDigit(cleaned.charAt(0))) {
            cleaned = "PATTERN_" + cleaned;
        }
        
        return cleaned.isEmpty() ? "UNKNOWN_PATTERN" : cleaned + "_PATTERN";
    }
    
    /**
     * Converts a terminal name to a valid Java enum constant.
     */
    private String toEnumConstant(String name) {
        if (name == null || name.isEmpty()) {
            return "UNKNOWN";
        }
        
        // Handle special cases for common symbols
        switch (name) {
            case "+": return "PLUS";
            case "-": return "MINUS";
            case "*": return "MULTIPLY";
            case "/": return "DIVIDE";
            case "=": return "EQUALS";
            case "(": return "LPAREN";
            case ")": return "RPAREN";
            case "{": return "LBRACE";
            case "}": return "RBRACE";
            case "[": return "LBRACKET";
            case "]": return "RBRACKET";
            case ";": return "SEMICOLON";
            case ",": return "COMMA";
            case ".": return "DOT";
            case ":": return "COLON";
            default:
                String cleaned = name.replaceAll("[^a-zA-Z0-9_]", "_")
                                    .replaceAll("_+", "_")
                                    .replaceAll("^_|_$", "")
                                    .toUpperCase();
                
                if (cleaned.isEmpty() || Character.isDigit(cleaned.charAt(0))) {
                    cleaned = "TOKEN_" + cleaned;
                }
                
                return cleaned.isEmpty() ? "UNKNOWN" : cleaned;
        }
    }
    
    /**
     * Escapes special characters in regex patterns for Java string literals.
     */
    private String escapePattern(String pattern) {
        if (pattern == null) {
            return "";
        }
        
        return pattern.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
    }
    
    /**
     * Returns the template manager used by this generator.
     *
     * @return the template manager
     */
    public TemplateManager getTemplateManager() {
        return templateManager;
    }
    
    /**
     * Returns the package name for generated classes.
     *
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * Returns the grammar name used in generated classes.
     *
     * @return the grammar name
     */
    public String getGrammarName() {
        return grammarName;
    }
    
    /**
     * Data class to hold terminal information for template rendering.
     * Includes compiled pattern for efficient matching during generation.
     */
    public static class TerminalInfo {
        private final String name;
        private final String pattern;
        private final String constantName;
        private final String enumConstant;
        private final Pattern compiledPattern;
        private final int specificity;
        
        public TerminalInfo(String name, String pattern, String constantName, String enumConstant) {
            this.name = name;
            this.pattern = pattern;
            this.constantName = constantName;
            this.enumConstant = enumConstant;
            
            // Compile pattern for validation and efficiency
            try {
                this.compiledPattern = Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid pattern for terminal '" + name + "': " + e.getMessage(), e);
            }
            
            // Calculate specificity for ordering
            this.specificity = calculateSpecificity(pattern);
        }
        
        public String getName() { return name; }
        public String getPattern() { return pattern; }
        public String getConstantName() { return constantName; }
        public String getEnumConstant() { return enumConstant; }
        public Pattern getCompiledPattern() { return compiledPattern; }
        public int getSpecificity() { return specificity; }
        
        /**
         * Calculates specificity score for this terminal's pattern.
         */
        private int calculateSpecificity(String pattern) {
            if (pattern == null || pattern.isEmpty()) {
                return 0;
            }
            
            int specificity = 0;
            
            // Literal characters increase specificity
            for (char c : pattern.toCharArray()) {
                if (Character.isLetterOrDigit(c)) {
                    specificity += 10;
                }
            }
            
            // Character classes are less specific than literals
            specificity -= pattern.split("\\[").length - 1;
            
            // Quantifiers reduce specificity
            specificity -= pattern.split("\\*").length - 1;
            specificity -= pattern.split("\\+").length - 1;
            specificity -= pattern.split("\\?").length - 1;
            
            // Alternation reduces specificity
            specificity -= pattern.split("\\|").length - 1;
            
            // Dot wildcard significantly reduces specificity
            specificity -= (pattern.split("\\.").length - 1) * 5;
            
            return specificity;
        }
        
        @Override
        public String toString() {
            return String.format("TerminalInfo{name='%s', pattern='%s', specificity=%d}", 
                    name, pattern, specificity);
        }
    }
    
    /**
     * Escapes a string for use in Java string literals.
     * This method properly escapes backslashes, quotes, and other special characters.
     *
     * @param input the string to escape
     * @return the escaped string suitable for Java string literals
     */
    private String escapeJavaString(String input) {
        if (input == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c < 32 || c > 126) {
                        // Escape non-printable characters
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }
}