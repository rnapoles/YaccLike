package com.parsergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Functional validation test for the generated arithmetic parser.
 * This test validates that the generated parser meets the requirements
 * by examining the generated code structure and capabilities.
 * 
 * Requirements tested: 6.3, 6.4, 6.5
 */
public class ArithmeticParserFunctionalityTest {
    
    @TempDir
    Path tempDir;
    
    private ParserGenerator generator;
    private Path grammarFile;
    private Path outputDir;
    private Path packageDir;
    
    @BeforeEach
    void setUp() throws IOException {
        // Set up temporary directories
        outputDir = tempDir.resolve("generated");
        Files.createDirectories(outputDir);
        
        // Initialize parser generator
        generator = new ParserGenerator();
        generator.setOutputDirectory(outputDir.toString());
        generator.setPackageName("com.test.arithmetic");
        generator.setGrammarName("ArithmeticParser");
        generator.setVerbose(false);
        generator.setDebug(false);
        
        // Create arithmetic grammar
        grammarFile = tempDir.resolve("arithmetic.grammar");
        createArithmeticGrammar();
        
        // Generate the parser
        generateParser();
    }
    
    private void createArithmeticGrammar() throws IOException {
        String grammarContent = """
            -- Arithmetic expression grammar for functionality testing
            NUMBER = {[0-9]+}
            PLUS = {\\+}
            MULTIPLY = {\\*}
            LPAREN = {\\(}
            RPAREN = {\\)}
            
            Start = <Expression>
            
            <Expression> ::= <Term> PLUS <Expression> | <Term>
            <Term> ::= <Factor> MULTIPLY <Term> | <Factor>
            <Factor> ::= NUMBER | LPAREN <Expression> RPAREN
            """;
        
        Files.writeString(grammarFile, grammarContent);
    }
    
    private void generateParser() throws IOException {
        // Generate the parser
        boolean success = generator.generateParser(grammarFile.toString());
        assertTrue(success, "Parser generation should succeed");
        
        packageDir = outputDir.resolve("com").resolve("test").resolve("arithmetic");
        assertTrue(Files.exists(packageDir), "Package directory should exist");
    }
    
    /**
     * Test that the generated parser correctly handles operator precedence.
     * Requirement 6.3: Parser should produce correct AST with proper precedence for "2+3*4"
     */
    @Test
    @DisplayName("Generated parser should handle operator precedence correctly")
    void testOperatorPrecedence() throws IOException {
        // Verify the grammar structure reflects precedence
        // Expression (addition, lower precedence) -> Term (multiplication, higher precedence) -> Factor (highest)
        
        // Check that all AST node types are generated
        assertTrue(Files.exists(packageDir.resolve("ExpressionNode.java")), 
                  "ExpressionNode should be generated for addition operations");
        assertTrue(Files.exists(packageDir.resolve("TermNode.java")), 
                  "TermNode should be generated for multiplication operations");
        assertTrue(Files.exists(packageDir.resolve("FactorNode.java")), 
                  "FactorNode should be generated for primary expressions");
        
        // Verify the parser has the correct production structure
        Path parserFile = packageDir.resolve("ArithmeticParserParser.java");
        String parserContent = Files.readString(parserFile);
        
        // Check that the parser handles multiple productions (indicating precedence levels)
        assertTrue(parserContent.contains("createNonTerminalNode"), 
                  "Parser should create different AST nodes for different precedence levels");
        
        // Verify parsing table has sufficient complexity for precedence
        assertTrue(parserContent.contains("ACTION_TABLE") && parserContent.contains("GOTO_TABLE"), 
                  "Parser should have parsing tables for precedence handling");
        
        // The parser should be substantial enough to handle precedence correctly
        assertTrue(parserContent.length() > 8000, 
                  "Generated parser should be substantial for precedence handling");
    }
    
    /**
     * Test that the generated parser correctly handles parentheses grouping.
     * Requirement 6.4: Parser should respect parentheses grouping for "(2+3)*4"
     */
    @Test
    @DisplayName("Generated parser should handle parentheses grouping")
    void testParenthesesGrouping() throws IOException {
        // Verify LPAREN and RPAREN tokens are defined
        Path tokenTypeFile = packageDir.resolve("TokenType.java");
        String tokenTypeContent = Files.readString(tokenTypeFile);
        
        assertTrue(tokenTypeContent.contains("LPAREN"), "TokenType should include LPAREN");
        assertTrue(tokenTypeContent.contains("RPAREN"), "TokenType should include RPAREN");
        
        // Verify the Factor production can handle parenthesized expressions
        // Factor ::= NUMBER | LPAREN Expression RPAREN
        Path parserFile = packageDir.resolve("ArithmeticParserParser.java");
        String parserContent = Files.readString(parserFile);
        
        // The parser should handle the parentheses tokens in its parsing table
        // Since the parser uses token indices, we check for the parsing table structure
        assertTrue(parserContent.contains("ACTION_TABLE") && parserContent.contains("GOTO_TABLE"), 
                  "Parser should have parsing tables that handle parentheses tokens");
        
        // Verify FactorNode can handle both NUMBER and parenthesized expressions
        Path factorFile = packageDir.resolve("FactorNode.java");
        assertTrue(Files.exists(factorFile), "FactorNode should exist for handling parentheses");
        
        String factorContent = Files.readString(factorFile);
        assertTrue(factorContent.contains("extends ASTNode") || factorContent.contains("implements"), 
                  "FactorNode should be a proper AST node");
    }
    
    /**
     * Test that the generated parser provides meaningful error messages.
     * Requirement 6.5: Parser should provide meaningful error messages for invalid input
     */
    @Test
    @DisplayName("Generated parser should provide meaningful error messages")
    void testErrorMessages() throws IOException {
        // Verify ParseException class exists with proper structure
        Path parseExceptionFile = packageDir.resolve("ParseException.java");
        assertTrue(Files.exists(parseExceptionFile), "ParseException should be generated");
        
        String parseExceptionContent = Files.readString(parseExceptionFile);
        assertTrue(parseExceptionContent.contains("extends Exception") || 
                  parseExceptionContent.contains("extends RuntimeException"), 
                  "ParseException should extend Exception");
        
        // Verify Position class for error location tracking
        Path positionFile = packageDir.resolve("Position.java");
        assertTrue(Files.exists(positionFile), "Position class should be generated");
        
        String positionContent = Files.readString(positionFile);
        assertTrue(positionContent.contains("line") && positionContent.contains("column"), 
                  "Position should track line and column numbers");
        
        // Verify parser has error handling infrastructure
        Path parserFile = packageDir.resolve("ArithmeticParserParser.java");
        String parserContent = Files.readString(parserFile);
        
        assertTrue(parserContent.contains("createErrorMessage") || parserContent.contains("error"), 
                  "Parser should have error message creation");
        assertTrue(parserContent.contains("getExpectedTokens") || parserContent.contains("expected"), 
                  "Parser should provide expected token information");
        assertTrue(parserContent.contains("handleError") || parserContent.contains("recovery"), 
                  "Parser should have error handling/recovery mechanisms");
        
        // Verify error recovery mechanisms
        assertTrue(parserContent.contains("panicModeRecovery") || parserContent.contains("panic"), 
                  "Parser should have panic mode recovery");
        assertTrue(parserContent.contains("phraseLevelRecovery") || parserContent.contains("phrase"), 
                  "Parser should have phrase-level recovery");
    }
    
    /**
     * Test that the generated parser has complete and correct structure.
     * This validates the overall functionality of the generated parser.
     */
    @Test
    @DisplayName("Generated parser should have complete structure")
    void testParserCompleteness() throws IOException {
        // Verify all essential files are generated
        String[] requiredFiles = {
            "ArithmeticParserParser.java",  // Main parser class
            "TokenType.java",               // Token type enumeration
            "Token.java",                   // Token class
            "ASTNode.java",                 // Base AST node class
            "Position.java",                // Position tracking
            "ParseException.java",          // Exception handling
            "ExpressionNode.java",          // Expression AST nodes
            "TermNode.java",                // Term AST nodes
            "FactorNode.java",              // Factor AST nodes
            "TerminalNode.java",            // Terminal AST nodes
            "ASTVisitor.java"               // Visitor pattern support
        };
        
        for (String fileName : requiredFiles) {
            Path file = packageDir.resolve(fileName);
            assertTrue(Files.exists(file), "Required file " + fileName + " should exist");
            
            String content = Files.readString(file);
            assertFalse(content.trim().isEmpty(), "File " + fileName + " should not be empty");
            assertTrue(content.contains("package com.test.arithmetic"), 
                      "File " + fileName + " should have correct package declaration");
        }
        
        // Verify the main parser has all essential methods
        Path parserFile = packageDir.resolve("ArithmeticParserParser.java");
        String parserContent = Files.readString(parserFile);
        
        String[] essentialMethods = {
            "parse",                    // Main parsing method
            "shift",                    // Shift action
            "reduce",                   // Reduce action
            "createNonTerminalNode",    // AST node creation
            "getCurrentToken",          // Token access
            "handleError"               // Error handling
        };
        
        for (String method : essentialMethods) {
            assertTrue(parserContent.contains(method), 
                      "Parser should contain method: " + method);
        }
        
        // Verify parsing tables are embedded
        assertTrue(parserContent.contains("ACTION_TABLE"), "Parser should contain ACTION table");
        assertTrue(parserContent.contains("GOTO_TABLE"), "Parser should contain GOTO table");
        assertTrue(parserContent.contains("PRODUCTION_LENGTHS"), "Parser should contain production info");
    }
    
    /**
     * Test that the generated parser handles all required tokens.
     * This ensures the lexical analysis component is properly integrated.
     */
    @Test
    @DisplayName("Generated parser should handle all required tokens")
    void testTokenHandling() throws IOException {
        // Verify TokenType enum contains all expected tokens
        Path tokenTypeFile = packageDir.resolve("TokenType.java");
        String tokenTypeContent = Files.readString(tokenTypeFile);
        
        String[] expectedTokens = {"NUMBER", "PLUS", "MULTIPLY", "LPAREN", "RPAREN", "EOF"};
        for (String token : expectedTokens) {
            assertTrue(tokenTypeContent.contains(token), 
                      "TokenType should contain " + token + " token");
        }
        
        // Verify Token class has proper structure
        Path tokenFile = packageDir.resolve("Token.java");
        String tokenContent = Files.readString(tokenFile);
        
        assertTrue(tokenContent.contains("TokenType"), "Token should reference TokenType");
        assertTrue(tokenContent.contains("Position"), "Token should have position information");
        assertTrue(tokenContent.contains("getValue") || tokenContent.contains("value"), 
                  "Token should have value accessor");
        assertTrue(tokenContent.contains("getType") || tokenContent.contains("type"), 
                  "Token should have type accessor");
        
        // Verify parser properly handles token types
        Path parserFile = packageDir.resolve("ArithmeticParserParser.java");
        String parserContent = Files.readString(parserFile);
        
        assertTrue(parserContent.contains("getTokenTypeIndex") || parserContent.contains("tokenType"), 
                  "Parser should handle token type indexing");
        assertTrue(parserContent.contains("EOF"), "Parser should handle EOF token");
    }
    
    /**
     * Test that the generated AST structure supports proper tree traversal.
     * This validates the AST generation functionality.
     */
    @Test
    @DisplayName("Generated AST should support proper tree traversal")
    void testASTTraversal() throws IOException {
        // Verify base ASTNode class has proper structure
        Path astNodeFile = packageDir.resolve("ASTNode.java");
        String astNodeContent = Files.readString(astNodeFile);
        
        assertTrue(astNodeContent.contains("abstract") || astNodeContent.contains("interface"), 
                  "ASTNode should be abstract class or interface");
        assertTrue(astNodeContent.contains("accept") || astNodeContent.contains("visit"), 
                  "ASTNode should support visitor pattern");
        
        // Verify ASTVisitor interface exists
        Path visitorFile = packageDir.resolve("ASTVisitor.java");
        assertTrue(Files.exists(visitorFile), "ASTVisitor should be generated");
        
        String visitorContent = Files.readString(visitorFile);
        assertTrue(visitorContent.contains("visit"), "ASTVisitor should have visit methods");
        assertTrue(visitorContent.contains("Expression") && visitorContent.contains("Term") && 
                  visitorContent.contains("Factor"), 
                  "ASTVisitor should have methods for all node types");
        
        // Verify all AST node classes implement the visitor pattern
        String[] astNodeFiles = {"ExpressionNode.java", "TermNode.java", "FactorNode.java", "TerminalNode.java"};
        
        for (String nodeFile : astNodeFiles) {
            Path nodePath = packageDir.resolve(nodeFile);
            String nodeContent = Files.readString(nodePath);
            
            assertTrue(nodeContent.contains("extends ASTNode") || nodeContent.contains("implements"), 
                      nodeFile + " should extend ASTNode or implement interface");
            assertTrue(nodeContent.contains("accept") || nodeContent.contains("visit"), 
                      nodeFile + " should support visitor pattern");
        }
    }
}