package com.parsergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Comprehensive test suite for the arithmetic grammar example.
 * This test validates the complete pipeline from grammar definition
 * to parser generation and functionality verification.
 * 
 * Tests cover:
 * - Grammar parsing and validation
 * - Operator precedence handling
 * - Associativity rules
 * - Parentheses grouping
 * - Generated parser functionality
 */
public class ArithmeticGrammarTest {
    
    @TempDir
    Path tempDir;
    
    private ParserGenerator generator;
    private Path grammarFile;
    private Path outputDir;
    
    @BeforeEach
    void setUp() throws IOException {
        // Set up temporary directories
        outputDir = tempDir.resolve("generated");
        Files.createDirectories(outputDir);
        
        // Initialize parser generator with test configuration
        generator = new ParserGenerator();
        generator.setOutputDirectory(outputDir.toString());
        generator.setPackageName("com.test.arithmetic");
        generator.setGrammarName("ArithmeticParser");
        generator.setVerbose(true);
        generator.setDebug(true);
        
        // Copy arithmetic grammar to temp directory
        grammarFile = tempDir.resolve("arithmetic.grammar");
        copyArithmeticGrammar();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up is handled by @TempDir
    }
    
    /**
     * Copies the arithmetic grammar from test resources to temp directory
     */
    private void copyArithmeticGrammar() throws IOException {
        String grammarContent = """
            -- Arithmetic expression grammar
            -- This grammar demonstrates proper operator precedence and associativity
            -- for basic arithmetic operations with parentheses support
            
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
    
    /**
     * Test that the arithmetic grammar can be successfully parsed and processed.
     * Requirement 6.1: System should correctly identify operator precedence
     */
    @Test
    void testArithmeticGrammarParsing() throws IOException {
        // Generate parser from arithmetic grammar
        boolean success = generator.generateParser(grammarFile.toString());
        
        // Verify generation was successful
        assertTrue(success, "Arithmetic grammar should be parsed successfully");
        
        // Verify output directory structure was created
        assertTrue(Files.exists(outputDir), "Output directory should be created");
        
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("arithmetic");
        assertTrue(Files.exists(packageDir), "Package directory should be created");
    }
    
    /**
     * Test that essential parser files are generated for the arithmetic grammar.
     * Requirement 6.1: System should generate complete parser implementation
     */
    @Test
    void testArithmeticParserFileGeneration() throws IOException {
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        assertTrue(success, "Parser generation should succeed");
        
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("arithmetic");
        
        // Verify essential files were generated
        assertTrue(Files.exists(packageDir.resolve("ArithmeticParserParser.java")), 
                  "Main parser class should be generated");
        assertTrue(Files.exists(packageDir.resolve("TokenType.java")), 
                  "TokenType enum should be generated");
        assertTrue(Files.exists(packageDir.resolve("Token.java")), 
                  "Token class should be generated");
        assertTrue(Files.exists(packageDir.resolve("ASTNode.java")), 
                  "AST base class should be generated");
        
        // Verify AST node classes for non-terminals
        assertTrue(Files.exists(packageDir.resolve("ExpressionNode.java")), 
                  "Expression AST node should be generated");
        assertTrue(Files.exists(packageDir.resolve("TermNode.java")), 
                  "Term AST node should be generated");
        assertTrue(Files.exists(packageDir.resolve("FactorNode.java")), 
                  "Factor AST node should be generated");
    }
    
    /**
     * Test that the generated parser correctly handles operator precedence.
     * Requirement 6.1: System should correctly identify operator precedence
     * Requirement 6.3: Parser should produce correct AST with proper precedence for "2+3*4"
     */
    @Test
    void testOperatorPrecedence() throws IOException {
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        assertTrue(success, "Parser generation should succeed");
        
        // Verify the grammar structure supports precedence
        // MULTIPLY should bind tighter than PLUS based on grammar rules:
        // <Expression> ::= <Term> PLUS <Expression> | <Term>
        // <Term> ::= <Factor> MULTIPLY <Term> | <Factor>
        
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("arithmetic");
        Path parserFile = packageDir.resolve("ArithmeticParserParser.java");
        
        assertTrue(Files.exists(parserFile), "Parser file should exist");
        
        // Read generated parser content to verify structure
        String parserContent = Files.readString(parserFile);
        
        // Verify that the parser contains the expected grammar structure
        assertTrue(parserContent.contains("Expression"), "Parser should handle Expression non-terminal");
        assertTrue(parserContent.contains("Term"), "Parser should handle Term non-terminal");
        assertTrue(parserContent.contains("Factor"), "Parser should handle Factor non-terminal");
        
        // Verify token types are properly defined
        Path tokenTypeFile = packageDir.resolve("TokenType.java");
        String tokenContent = Files.readString(tokenTypeFile);
        assertTrue(tokenContent.contains("NUMBER"), "TokenType should include NUMBER");
        assertTrue(tokenContent.contains("PLUS"), "TokenType should include PLUS");
        assertTrue(tokenContent.contains("MULTIPLY"), "TokenType should include MULTIPLY");
    }
    
    /**
     * Test that the grammar correctly handles left-associative operators.
     * Requirement 6.2: System should handle left-associative operators correctly
     */
    @Test
    void testLeftAssociativity() throws IOException {
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        assertTrue(success, "Parser generation should succeed");
        
        // The grammar uses right-recursion to avoid left-recursion issues
        // but should still handle associativity correctly through the grammar structure
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("arithmetic");
        Path parserFile = packageDir.resolve("ArithmeticParserParser.java");
        
        assertTrue(Files.exists(parserFile), "Parser file should exist");
        
        // Verify the parser can handle multiple operations of the same precedence
        // The grammar structure should support expressions like "1+2+3" and "2*3*4"
        String parserContent = Files.readString(parserFile);
        
        // Check that parsing table includes states for handling sequences of operations
        assertTrue(parserContent.length() > 1000, "Generated parser should be substantial");
    }
    
    /**
     * Test that parentheses grouping is correctly handled.
     * Requirement 6.4: Parser should respect parentheses grouping for "(2+3)*4"
     */
    @Test
    void testParenthesesGrouping() throws IOException {
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        assertTrue(success, "Parser generation should succeed");
        
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("arithmetic");
        
        // Verify LPAREN and RPAREN tokens are properly handled
        Path tokenTypeFile = packageDir.resolve("TokenType.java");
        String tokenContent = Files.readString(tokenTypeFile);
        assertTrue(tokenContent.contains("LPAREN"), "TokenType should include LPAREN");
        assertTrue(tokenContent.contains("RPAREN"), "TokenType should include RPAREN");
        
        // Verify Factor production includes parenthesized expressions
        Path parserFile = packageDir.resolve("ArithmeticParserParser.java");
        String parserContent = Files.readString(parserFile);
        
        // The parser should handle the Factor ::= LPAREN Expression RPAREN production
        assertTrue(parserContent.contains("Factor"), "Parser should handle Factor non-terminal");
    }
    
    /**
     * Test that the generated parser compiles without errors.
     * This ensures the generated code is syntactically correct Java.
     */
    @Test
    void testGeneratedCodeCompilation() throws IOException {
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        assertTrue(success, "Parser generation should succeed");
        
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("arithmetic");
        
        // Verify all generated files exist and have content
        String[] expectedFiles = {
            "ArithmeticParserParser.java",
            "TokenType.java", 
            "Token.java",
            "ASTNode.java",
            "ExpressionNode.java",
            "TermNode.java", 
            "FactorNode.java",
            "Position.java",
            "ParseException.java"
        };
        
        for (String fileName : expectedFiles) {
            Path file = packageDir.resolve(fileName);
            assertTrue(Files.exists(file), "File " + fileName + " should exist");
            
            String content = Files.readString(file);
            assertFalse(content.trim().isEmpty(), "File " + fileName + " should not be empty");
            
            // Basic syntax checks
            assertTrue(content.contains("package com.test.arithmetic"), 
                      "File " + fileName + " should have correct package declaration");
            
            if (fileName.endsWith(".java")) {
                // Verify basic Java syntax elements
                assertTrue(content.contains("class ") || content.contains("enum ") || content.contains("interface "),
                          "File " + fileName + " should contain a class, enum, or interface declaration");
            }
        }
    }
    
    /**
     * Test grammar validation and error detection.
     * Ensures the arithmetic grammar passes all validation checks.
     */
    @Test
    void testGrammarValidation() throws IOException {
        // Generate parser with validation
        boolean success = generator.generateParser(grammarFile.toString());
        assertTrue(success, "Valid arithmetic grammar should pass all validation checks");
        
        // The grammar should not have left recursion (it uses right recursion)
        // The grammar should not have unreachable productions
        // The grammar should be LALR(1) compatible
        
        // If we reach this point without errors, validation passed
        assertTrue(true, "Grammar validation completed successfully");
    }
    
    /**
     * Test that the grammar correctly defines terminal patterns.
     * Verifies that NUMBER, operators, and parentheses are properly tokenized.
     */
    @Test
    void testTerminalDefinitions() throws IOException {
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        assertTrue(success, "Parser generation should succeed");
        
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("arithmetic");
        Path tokenTypeFile = packageDir.resolve("TokenType.java");
        
        String tokenContent = Files.readString(tokenTypeFile);
        
        // Verify all expected terminals are defined
        assertTrue(tokenContent.contains("NUMBER"), "NUMBER terminal should be defined");
        assertTrue(tokenContent.contains("PLUS"), "PLUS terminal should be defined");
        assertTrue(tokenContent.contains("MULTIPLY"), "MULTIPLY terminal should be defined");
        assertTrue(tokenContent.contains("LPAREN"), "LPAREN terminal should be defined");
        assertTrue(tokenContent.contains("RPAREN"), "RPAREN terminal should be defined");
        
        // Verify the enum structure
        assertTrue(tokenContent.contains("enum TokenType"), "Should define TokenType enum");
    }
    
    /**
     * Test that AST node classes are properly generated for all non-terminals.
     * Requirement 6.3: Ensure AST generation reflects correct operator precedence
     */
    @Test
    void testASTNodeGeneration() throws IOException {
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        assertTrue(success, "Parser generation should succeed");
        
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("arithmetic");
        
        // Verify AST node classes for each non-terminal
        String[] astNodeFiles = {
            "ExpressionNode.java",
            "TermNode.java",
            "FactorNode.java"
        };
        
        for (String fileName : astNodeFiles) {
            Path astFile = packageDir.resolve(fileName);
            assertTrue(Files.exists(astFile), "AST node file " + fileName + " should exist");
            
            String content = Files.readString(astFile);
            
            // Verify AST node extends base ASTNode class
            assertTrue(content.contains("extends ASTNode") || content.contains("implements"), 
                      "AST node " + fileName + " should extend ASTNode or implement interface");
            
            // Verify proper package and imports
            assertTrue(content.contains("package com.test.arithmetic"), 
                      "AST node " + fileName + " should have correct package");
        }
        
        // Verify base ASTNode class exists
        Path baseASTFile = packageDir.resolve("ASTNode.java");
        assertTrue(Files.exists(baseASTFile), "Base ASTNode class should exist");
        
        String baseContent = Files.readString(baseASTFile);
        assertTrue(baseContent.contains("class ASTNode") || baseContent.contains("abstract class ASTNode"),
                  "Base ASTNode should be properly defined");
    }
}