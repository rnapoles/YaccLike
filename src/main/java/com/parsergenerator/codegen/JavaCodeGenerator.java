package com.parsergenerator.codegen;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.lalr.Action;
import com.parsergenerator.lalr.ParsingTable;
import com.parsergenerator.lalr.StateSymbolPair;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Symbol;
import com.parsergenerator.symbol.Terminal;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates Java parser code from a grammar and LALR parsing table.
 * This class produces complete parser implementations including:
 * - Main parser class with embedded parsing tables
 * - Lexer class for tokenization
 * - Token classes for all terminal symbols
 * - AST node classes for all non-terminals
 * - Error handling and recovery mechanisms
 */
public class JavaCodeGenerator {
    private final TemplateManager templateManager;
    private final String packageName;
    private final String grammarName;
    private final LexerCodeGenerator lexerGenerator;
    
    /**
     * Creates a new JavaCodeGenerator with default package and grammar names.
     */
    public JavaCodeGenerator() {
        this("com.generated.parser", "GeneratedGrammar");
    }
    
    /**
     * Creates a new JavaCodeGenerator with specified package and grammar names.
     *
     * @param packageName the package name for generated classes
     * @param grammarName the name of the grammar (used in class names)
     */
    public JavaCodeGenerator(String packageName, String grammarName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }
        if (grammarName == null || grammarName.trim().isEmpty()) {
            throw new IllegalArgumentException("Grammar name cannot be null or empty");
        }
        
        this.templateManager = new TemplateManager();
        this.packageName = packageName.trim();
        this.grammarName = grammarName.trim();
        this.lexerGenerator = new LexerCodeGenerator(templateManager, packageName, grammarName);
    }
    
    /**
     * Generates complete Java parser code from the given grammar and parsing table.
     *
     * @param grammar the grammar to generate parser for
     * @param parsingTable the LALR parsing table
     * @return a GeneratedCode object containing all generated files
     * @throws TemplateException if code generation fails
     */
    public GeneratedCode generate(Grammar grammar, ParsingTable parsingTable) throws TemplateException {
        if (grammar == null) {
            throw new IllegalArgumentException("Grammar cannot be null");
        }
        if (parsingTable == null) {
            throw new IllegalArgumentException("Parsing table cannot be null");
        }
        
        GeneratedCode result = new GeneratedCode();
        
        // Generate main parser class
        String parserCode = generateParserClass(grammar, parsingTable);
        result.addFile(grammarName + "Parser.java", parserCode);
        
        // Generate lexer class only if there are terminals with patterns
        boolean hasTerminalsWithPatterns = grammar.getTerminals().stream()
                .anyMatch(Terminal::hasPattern);
        
        if (hasTerminalsWithPatterns) {
            GeneratedCode lexerCode = lexerGenerator.generateLexer(grammar);
            result.merge(lexerCode);
        } else {
            // Generate a minimal lexer for grammars without pattern-based terminals
            String minimalLexerCode = generateMinimalLexer(grammar);
            result.addFile(grammarName + "Lexer.java", minimalLexerCode);
        }
        
        // Generate token type enum (enhanced for lexer compatibility)
        String tokenTypeCode = generateTokenTypeEnum(grammar);
        result.addFile("TokenType.java", tokenTypeCode);
        
        // Generate token class
        String tokenCode = generateTokenClass();
        result.addFile("Token.java", tokenCode);
        
        // Generate AST node base class
        String astBaseCode = generateASTNodeBase();
        result.addFile("ASTNode.java", astBaseCode);
        
        // Generate terminal AST node class
        String terminalNodeCode = generateTerminalASTNode();
        result.addFile("TerminalNode.java", terminalNodeCode);
        
        // Generate AST visitor interface
        String visitorCode = generateASTVisitor(grammar);
        result.addFile("ASTVisitor.java", visitorCode);
        
        // Generate non-terminal AST node classes
        for (NonTerminal nonTerminal : grammar.getNonTerminals()) {
            String nodeCode = generateNonTerminalASTNode(nonTerminal);
            String className = toClassName(nonTerminal.getName()) + "Node";
            result.addFile(className + ".java", nodeCode);
        }
        
        // Generate parse exception class
        String exceptionCode = generateParseException();
        result.addFile("ParseException.java", exceptionCode);
        
        // Generate position class
        String positionCode = generatePositionClass();
        result.addFile("Position.java", positionCode);
        
        return result;
    }
    
    /**
     * Generates complete Java parser and lexer code from the given grammar and parsing table.
     * This method ensures that the generated lexer tokens match the parser's expected token types.
     *
     * @param grammar the grammar to generate parser and lexer for
     * @param parsingTable the LALR parsing table
     * @return a GeneratedCode object containing all generated files
     * @throws TemplateException if code generation fails
     */
    public GeneratedCode generateWithLexer(Grammar grammar, ParsingTable parsingTable) throws TemplateException {
        // This method is equivalent to generate() since lexer generation is now integrated
        return generate(grammar, parsingTable);
    }
    
    /**
     * Generates the main parser class with embedded parsing tables.
     */
    private String generateParserClass(Grammar grammar, ParsingTable parsingTable) throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        
        variables.put("packageName", packageName);
        variables.put("grammarName", grammarName);
        variables.put("className", grammarName + "Parser");
        variables.put("lexerClassName", grammarName + "Lexer");
        variables.put("astNodeBaseClass", "ASTNode");
        variables.put("terminalNodeClass", "TerminalNode");
        
        // Generate parsing tables
        variables.put("actionTable", generateActionTableArray(grammar, parsingTable));
        variables.put("gotoTable", generateGotoTableArray(grammar, parsingTable));
        variables.put("eofToken", getEOFTokenIndex(grammar));
        
        // Generate production information
        variables.put("productionLengths", generateProductionLengthsArray(grammar));
        variables.put("productionLeftSides", generateProductionLeftSidesArray(grammar));
        
        // Generate node creation switch statement
        variables.put("createNodeSwitch", generateNodeCreationSwitch(grammar));
        
        return templateManager.renderTemplate(TemplateManager.PARSER_CLASS_TEMPLATE, variables);
    }
    
    /**
     * Generates the TokenType enum for all terminal symbols.
     * Enhanced to ensure compatibility between parser and lexer token types.
     */
    private String generateTokenTypeEnum(Grammar grammar) throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        
        variables.put("packageName", packageName);
        variables.put("grammarName", grammarName);
        
        // Generate token types for all terminals (both with and without patterns)
        // The parser needs all terminals, while the lexer will only use those with patterns
        List<String> tokenTypes = grammar.getTerminals().stream()
                .map(terminal -> toEnumConstant(terminal.getName()))
                .collect(Collectors.toList());
        
        // Add standard token types that lexer and parser both need
        // Note: EOF is added by the template, so we don't add it here
        if (!tokenTypes.contains("TERMINAL")) {
            tokenTypes.add("TERMINAL");
        }
        if (!tokenTypes.contains("UNKNOWN")) {
            tokenTypes.add("UNKNOWN");
        }
        
        variables.put("tokenTypes", tokenTypes);
        
        return templateManager.renderTemplate(TemplateManager.TOKEN_TYPE_ENUM_TEMPLATE, variables);
    }
    
    /**
     * Generates the Token class.
     */
    private String generateTokenClass() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", packageName);
        
        return templateManager.renderTemplate(TemplateManager.TOKEN_CLASS_TEMPLATE, variables);
    }
    
    /**
     * Generates the base AST node class.
     */
    private String generateASTNodeBase() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", packageName);
        variables.put("className", "ASTNode");
        
        return templateManager.renderTemplate(TemplateManager.AST_NODE_BASE_TEMPLATE, variables);
    }
    
    /**
     * Generates the terminal AST node class.
     */
    private String generateTerminalASTNode() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", packageName);
        variables.put("className", "TerminalNode");
        variables.put("baseClassName", "ASTNode");
        
        return templateManager.renderTemplate(TemplateManager.AST_NODE_TERMINAL_TEMPLATE, variables);
    }
    
    /**
     * Generates an AST node class for a specific non-terminal.
     */
    private String generateNonTerminalASTNode(NonTerminal nonTerminal) throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        
        variables.put("packageName", packageName);
        variables.put("className", toClassName(nonTerminal.getName()) + "Node");
        variables.put("baseClassName", "ASTNode");
        variables.put("symbolName", toClassName(nonTerminal.getName()));
        
        return templateManager.renderTemplate(TemplateManager.AST_NODE_NONTERMINAL_TEMPLATE, variables);
    }
    
    /**
     * Generates the ASTVisitor interface.
     */
    private String generateASTVisitor(Grammar grammar) throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        
        variables.put("packageName", packageName);
        variables.put("grammarName", grammarName);
        
        List<String> nonTerminalNames = grammar.getNonTerminals().stream()
                .map(nt -> toClassName(nt.getName()))
                .collect(Collectors.toList());
        
        variables.put("nonTerminals", nonTerminalNames);
        
        return templateManager.renderTemplate(TemplateManager.AST_VISITOR_TEMPLATE, variables);
    }
    
    /**
     * Generates the ParseException class.
     */
    private String generateParseException() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", packageName);
        
        return templateManager.renderTemplate(TemplateManager.PARSE_EXCEPTION_TEMPLATE, variables);
    }
    
    /**
     * Generates the Position class.
     */
    private String generatePositionClass() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", packageName);
        
        return templateManager.renderTemplate(TemplateManager.POSITION_CLASS_TEMPLATE, variables);
    }
    
    /**
     * Generates the ACTION table as a Java 2D array string.
     */
    private String generateActionTableArray(Grammar grammar, ParsingTable parsingTable) {
        List<Terminal> terminals = new ArrayList<>(grammar.getTerminals());
        terminals.add(new Terminal("EOF")); // Add EOF token
        
        int stateCount = parsingTable.getStateCount();
        int terminalCount = terminals.size();
        
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        
        for (int state = 0; state < stateCount; state++) {
            sb.append("        {");
            for (int termIndex = 0; termIndex < terminalCount; termIndex++) {
                Terminal terminal = terminals.get(termIndex);
                Action action = parsingTable.getAction(state, terminal);
                
                int actionValue = 0; // Default to error
                if (action != null) {
                    if (action.isShift()) {
                        actionValue = action.getParameter() + 1; // Shift actions are positive
                    } else if (action.isReduce()) {
                        actionValue = -(action.getParameter() + 1); // Reduce actions are negative
                    } else if (action.isAccept()) {
                        actionValue = 0; // Accept action
                    }
                }
                
                sb.append(actionValue);
                if (termIndex < terminalCount - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");
            if (state < stateCount - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        
        sb.append("    }");
        return sb.toString();
    }
    
    /**
     * Generates the GOTO table as a Java 2D array string.
     */
    private String generateGotoTableArray(Grammar grammar, ParsingTable parsingTable) {
        List<NonTerminal> nonTerminals = new ArrayList<>(grammar.getNonTerminals());
        
        int stateCount = parsingTable.getStateCount();
        int nonTerminalCount = nonTerminals.size();
        
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        
        for (int state = 0; state < stateCount; state++) {
            sb.append("        {");
            for (int ntIndex = 0; ntIndex < nonTerminalCount; ntIndex++) {
                NonTerminal nonTerminal = nonTerminals.get(ntIndex);
                Integer gotoState = parsingTable.getGoto(state, nonTerminal);
                
                int gotoValue = (gotoState != null) ? gotoState : -1;
                sb.append(gotoValue);
                
                if (ntIndex < nonTerminalCount - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");
            if (state < stateCount - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        
        sb.append("    }");
        return sb.toString();
    }
    
    /**
     * Gets the index of the EOF token.
     */
    private int getEOFTokenIndex(Grammar grammar) {
        return grammar.getTerminals().size(); // EOF is added after all terminals
    }
    
    /**
     * Generates the production lengths array.
     */
    private String generateProductionLengthsArray(Grammar grammar) {
        List<Production> productions = new ArrayList<>(grammar.getProductions());
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        for (int i = 0; i < productions.size(); i++) {
            Production production = productions.get(i);
            sb.append(production.getRightSideLength());
            
            if (i < productions.size() - 1) {
                sb.append(", ");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Generates the production left sides array (indices of non-terminals).
     */
    private String generateProductionLeftSidesArray(Grammar grammar) {
        List<Production> productions = new ArrayList<>(grammar.getProductions());
        List<NonTerminal> nonTerminals = new ArrayList<>(grammar.getNonTerminals());
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        for (int i = 0; i < productions.size(); i++) {
            Production production = productions.get(i);
            int leftSideIndex = nonTerminals.indexOf(production.getLeftSide());
            sb.append(leftSideIndex);
            
            if (i < productions.size() - 1) {
                sb.append(", ");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Generates the switch statement for creating AST nodes.
     */
    private String generateNodeCreationSwitch(Grammar grammar) {
        List<Production> productions = new ArrayList<>(grammar.getProductions());
        
        StringBuilder sb = new StringBuilder();
        sb.append("switch (productionNumber) {\n");
        
        for (int i = 0; i < productions.size(); i++) {
            Production production = productions.get(i);
            String className = toClassName(production.getLeftSide().getName()) + "Node";
            
            sb.append("            case ").append(i).append(":\n");
            sb.append("                return new ").append(className).append("(").append(i).append(", children);\n");
        }
        
        sb.append("            default:\n");
        sb.append("                throw new ParseException(\"Unknown production number: \" + productionNumber);\n");
        sb.append("        }");
        
        return sb.toString();
    }
    
    /**
     * Converts a symbol name to a valid Java class name.
     */
    private String toClassName(String name) {
        if (name == null || name.isEmpty()) {
            return "Unknown";
        }
        
        // Remove angle brackets for non-terminals
        String cleaned = name.replaceAll("[<>]", "");
        
        // Convert to PascalCase
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : cleaned.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            } else {
                capitalizeNext = true;
            }
        }
        
        return result.length() > 0 ? result.toString() : "Unknown";
    }
    
    /**
     * Converts a symbol name to a valid Java enum constant.
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
            case "::": return "DOUBLE_COLON";
            case "::=": return "PRODUCTION";
            case "|": return "PIPE";
            case "<": return "LT";
            case ">": return "GT";
            case "<=": return "LE";
            case ">=": return "GE";
            case "==": return "EQ";
            case "!=": return "NE";
            case "&&": return "AND";
            case "||": return "OR";
            case "!": return "NOT";
            default:
                // For other names, clean them up
                String cleaned = name.replaceAll("[^a-zA-Z0-9_]", "_")
                                    .replaceAll("_+", "_")
                                    .replaceAll("^_|_$", "") // Remove leading/trailing underscores
                                    .toUpperCase();
                
                // Ensure it starts with a letter or underscore
                if (cleaned.isEmpty() || Character.isDigit(cleaned.charAt(0))) {
                    cleaned = "TOKEN_" + cleaned;
                }
                
                return cleaned.isEmpty() ? "UNKNOWN" : cleaned;
        }
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
     * Generates a minimal lexer for grammars that don't use pattern-based terminals.
     * This lexer provides basic functionality but doesn't perform actual tokenization.
     */
    private String generateMinimalLexer(Grammar grammar) throws TemplateException {
        StringBuilder sb = new StringBuilder();
        
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import java.util.*;\n");
        sb.append("import com.parsergenerator.lexer.Token;\n");
        sb.append("import com.parsergenerator.lexer.TokenType;\n");
        sb.append("import com.parsergenerator.lexer.Position;\n");
        sb.append("import com.parsergenerator.lexer.LexicalException;\n\n");
        
        sb.append("/**\n");
        sb.append(" * Minimal lexer class for ").append(grammarName).append(" grammar.\n");
        sb.append(" * This lexer is generated for grammars that don't use pattern-based terminals.\n");
        sb.append(" * It provides basic lexer interface but doesn't perform actual tokenization.\n");
        sb.append(" */\n");
        sb.append("public class ").append(grammarName).append("Lexer {\n");
        sb.append("    private final String input;\n");
        sb.append("    private final String filename;\n");
        sb.append("    private int position;\n");
        sb.append("    private int line;\n");
        sb.append("    private int column;\n\n");
        
        sb.append("    public ").append(grammarName).append("Lexer(String input) {\n");
        sb.append("        this(input, null);\n");
        sb.append("    }\n\n");
        
        sb.append("    public ").append(grammarName).append("Lexer(String input, String filename) {\n");
        sb.append("        this.input = input != null ? input : \"\";\n");
        sb.append("        this.filename = filename;\n");
        sb.append("        this.position = 0;\n");
        sb.append("        this.line = 1;\n");
        sb.append("        this.column = 1;\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Gets the current position in the input.\n");
        sb.append("     * @return the current position\n");
        sb.append("     */\n");
        sb.append("    public Position getCurrentPosition() {\n");
        sb.append("        return new Position(line, column, filename);\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Checks if there are more tokens to process.\n");
        sb.append("     * @return false for minimal lexer\n");
        sb.append("     */\n");
        sb.append("    public boolean hasMoreTokens() {\n");
        sb.append("        return false;\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Checks if the lexer has encountered any errors.\n");
        sb.append("     * @return false for minimal lexer\n");
        sb.append("     */\n");
        sb.append("    public boolean hasErrors() {\n");
        sb.append("        return false;\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Gets the next token from the input.\n");
        sb.append("     * @return EOF token for minimal lexer\n");
        sb.append("     */\n");
        sb.append("    public Token nextToken() {\n");
        sb.append("        return new Token(TokenType.EOF, \"\", getCurrentPosition());\n");
        sb.append("    }\n\n");
        
        sb.append("    /**\n");
        sb.append("     * Tokenizes the entire input and returns a list of tokens.\n");
        sb.append("     * @return list containing only EOF token for minimal lexer\n");
        sb.append("     */\n");
        sb.append("    public List<Token> tokenize() {\n");
        sb.append("        List<Token> tokens = new ArrayList<>();\n");
        sb.append("        tokens.add(new Token(TokenType.EOF, \"\", getCurrentPosition()));\n");
        sb.append("        return tokens;\n");
        sb.append("    }\n");
        
        sb.append("}\n");
        
        return sb.toString();
    }
    
    /**
     * Returns the lexer code generator used by this generator.
     *
     * @return the lexer code generator
     */
    public LexerCodeGenerator getLexerGenerator() {
        return lexerGenerator;
    }
}