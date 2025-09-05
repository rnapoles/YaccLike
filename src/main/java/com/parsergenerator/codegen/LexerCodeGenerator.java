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
        
        // Generate lexical exception class
        String exceptionCode = generateLexicalException();
        result.addFile("LexicalException.java", exceptionCode);
        
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
     * if any pattern is invalid.
     *
     * @param grammar the grammar containing terminals
     * @return list of terminal information objects
     * @throws TemplateException if any terminal pattern is invalid
     */
    private List<TerminalInfo> prepareTerminalInfo(Grammar grammar) throws TemplateException {
        List<TerminalInfo> terminalInfos = new ArrayList<>();
        
        for (Terminal terminal : grammar.getTerminals()) {
            // Always validate the pattern, even if it's empty or null
            validateTerminalPattern(terminal);
            
            // Only include terminals with valid patterns in the result
            if (terminal.hasPattern()) {
                terminalInfos.add(createTerminalInfo(terminal));
            }
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
     *
     * @param terminals list of terminal information
     * @return ordered list of terminal names for optimal matching
     */
    private List<String> generateTerminalOrder(List<TerminalInfo> terminals) {
        return terminals.stream()
                .sorted((t1, t2) -> {
                    // First, sort by pattern specificity (more specific patterns first)
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
     * Generates basic lexer class structure without template.
     * This is a fallback method when templates are not available.
     */
    private String generateBasicLexerClass(Map<String, Object> variables) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("package ").append(variables.get("packageName")).append(";\n\n");
        sb.append("import java.util.*;\n");
        sb.append("import java.util.regex.Pattern;\n");
        sb.append("import java.util.regex.Matcher;\n\n");
        
        sb.append("/**\n");
        sb.append(" * Generated lexer class for ").append(variables.get("grammarName")).append(" grammar.\n");
        sb.append(" * This class tokenizes input text according to terminal definitions.\n");
        sb.append(" */\n");
        sb.append("public class ").append(variables.get("className")).append(" {\n");
        sb.append("    private final String input;\n");
        sb.append("    private final String filename;\n");
        sb.append("    private int position;\n");
        sb.append("    private int line;\n");
        sb.append("    private int column;\n\n");
        
        // Add terminal patterns
        @SuppressWarnings("unchecked")
        List<TerminalInfo> terminals = (List<TerminalInfo>) variables.get("terminals");
        if (terminals != null) {
            for (TerminalInfo terminal : terminals) {
                sb.append("    private static final Pattern ").append(terminal.getConstantName())
                  .append(" = Pattern.compile(\"").append(escapePattern(terminal.getPattern())).append("\");\n");
            }
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
        sb.append("    }\n\n");
        
        sb.append("    // TODO: Implement tokenization methods\n");
        sb.append("    // This will be completed in subsequent tasks\n");
        
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
     */
    private String generateBasicLexicalException(Map<String, Object> variables) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("package ").append(variables.get("packageName")).append(";\n\n");
        
        sb.append("/**\n");
        sb.append(" * Exception thrown when lexical analysis fails.\n");
        sb.append(" */\n");
        sb.append("public class LexicalException extends Exception {\n");
        sb.append("    private final int line;\n");
        sb.append("    private final int column;\n");
        sb.append("    private final String unexpectedInput;\n\n");
        
        sb.append("    public LexicalException(String message, int line, int column, String unexpectedInput) {\n");
        sb.append("        super(message);\n");
        sb.append("        this.line = line;\n");
        sb.append("        this.column = column;\n");
        sb.append("        this.unexpectedInput = unexpectedInput;\n");
        sb.append("    }\n\n");
        
        sb.append("    public int getLine() { return line; }\n");
        sb.append("    public int getColumn() { return column; }\n");
        sb.append("    public String getUnexpectedInput() { return unexpectedInput; }\n");
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
}