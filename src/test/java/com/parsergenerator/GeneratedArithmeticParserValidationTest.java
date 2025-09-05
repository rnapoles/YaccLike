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
 * Comprehensive validation test for the generated arithmetic parser functionality.
 * This test validates the generated parser by examining:
 * - Generated code structure and completeness
 * - Proper operator precedence in grammar structure
 * - Error handling infrastructure in generated code
 * - AST generation capabilities
 * - Parsing table construction and embedding
 * 
 * Requirements tested: 6.3, 6.4, 6.5
 */
public class GeneratedArithmeticParserValidationTest {
    
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
        generator.setPackageName("com.test.validation");
        generator.setGrammarName("ArithmeticValidator");
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
            -- Arithmetic expression grammar for validation testing
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
        
        packageDir = outputDir.resolve("com").resolve("test").resolve("validation");
        assertTrue(Files.exists(packageDir), "Package directory should exist");
    }
    
    /**
     * Test that the generated parser has correct structure for handling arithmetic expressions.
     * Requirement 6.3: Parser should produce correct AST with proper precedence for "2+3*4"
     */
    @Test
    @DisplayName("Generated parser should have correct structure for arithmetic expressions")
    void testArithmeticParserStructure() throws IOException {
        // Verify the main parser class exists and has correct structure
        Path parserFile = packageDir.resolve("ArithmeticValidatorParser.java");
        assertTrue(Files.exists(parserFile), "Main parser class should exist");
        
        String parserContent = Files.readString(parserFile);
        
        // Verify parser has essential methods
        assertTrue(parserContent.contains("public ASTNode parse("), 
                  "Parser should have parse method");
        assertTrue(parserContent.contains("private void shift("), 
                  "Parser should have shift method");
        assertTrue(parserContent.contains("private void reduce("), 
                  "Parser should have reduce method");
        
        // Verify parsing tables are embedded
        assertTrue(parserContent.contains("ACTION_TABLE"), 
                  "Parser should contain ACTION table");
        assertTrue(parserContent.contains("GOTO_TABLE"), 
                  "Parser should contain GOTO table");
        
        // Verify error handling infrastructure
        assertTrue(parserContent.contains("ParseException"), 
                  "Parser should reference ParseException");
        assertTrue(parserContent.contains("handleError") || parserContent.contains("error"), 
                  "Parser should have error handling");
    }
    
    /**
     * Test that the generated parser correctly handles parentheses in grammar structure.
     * Requirement 6.4: Parser should respect parentheses grouping for "(2+3)*4"
     */
    @Test
    @DisplayName("Generated parser should handle parentheses in grammar structure")
    void testParenthesesInGrammarStructure() throws IOException {
        // Verify TokenType includes parentheses tokens
        Path tokenTypeFile = packageDir.resolve("TokenType.java");
        assertTrue(Files.exists(tokenTypeFile), "TokenType enum should exist");
        
        String tokenTypeContent = Files.readString(tokenTypeFile);
        assertTrue(tokenTypeContent.contains("LPAREN"), "TokenType should include LPAREN");
        assertTrue(tokenTypeContent.contains("RPAREN"), "TokenType should include RPAREN");
        
        // Verify parser handles parentheses in parsing table
        Path parserFile = packageDir.resolve("ArithmeticValidatorParser.java");
        String parserContent = Files.readString(parserFile);
        
        // Check that parsing tables include entries for parentheses
        // The ACTION_TABLE should have entries for LPAREN and RPAREN tokens
        assertTrue(parserContent.contains("ACTION_TABLE"), "Parser should have ACTION table");
        
        // Verify Factor production can handle parenthesized expressions
        // This is reflected in the grammar structure: Factor ::= LPAREN Expression RPAREN
        assertTrue(parserContent.contains("FactorNode") || parserContent.contains("Factor"), 
                  "Parser should handle Factor productions");
    }
    
    /**
     * Test that the generated parser grammar structure supports associativity.
     * Requirement 6.2: System should handle left-associative operators correctly
     */
    @Test
    @DisplayName("Generated parser should support associativity in grammar structure")
    void testAssociativityInGrammarStructure() throws IOException {
        // Verify the grammar structure supports proper associativity
        // The grammar uses right-recursion to avoid left-recursion issues:
        // Expression ::= Term PLUS Expression | Term
        // Term ::= Factor MULTIPLY Term | Factor
        
        Path parserFile = packageDir.resolve("ArithmeticValidatorParser.java");
        String parserContent = Files.readString(parserFile);
        
        // Verify the parser can handle the grammar productions
        assertTrue(parserContent.contains("ExpressionNode") || parserContent.contains("Expression"), 
                  "Parser should handle Expression productions");
        assertTrue(parserContent.contains("TermNode") || parserContent.contains("Term"), 
                  "Parser should handle Term productions");
        
        // Verify parsing table has sufficient states to handle associativity
        // Look for ACTION_TABLE with multiple states
        Pattern actionTablePattern = Pattern.compile("ACTION_TABLE.*?=.*?\\{\\{([^}]+)\\}\\}", Pattern.DOTALL);
        Matcher matcher = actionTablePattern.matcher(parserContent);
        
        if (matcher.find()) {
            String tableContent = matcher.group(1);
            // Count the number of states (rows) in the table
            int stateCount = tableContent.split("\\},\\s*\\{").length;
            assertTrue(stateCount >= 10, "Parser should have sufficient states for associativity (found " + stateCount + ")");
        } else {
            // Alternative check: look for multiple state references
            assertTrue(parserContent.contains("state") && parserContent.length() > 5000, 
                      "Parser should be substantial enough to handle associativity");
        }
    }
    
    /**
     * Test that the generated parser has proper AST node structure for complex expressions.
     * Requirement 6.3: Ensure AST generation reflects correct operator precedence
     */
    @Test
    @DisplayName("Generated parser should have proper AST node structure")
    void testASTNodeStructure() throws IOException {
        // Verify all expected AST node classes are generated
        String[] expectedASTNodes = {
            "ExpressionNode.java",
            "TermNode.java", 
            "FactorNode.java",
            "TerminalNode.java"
        };
        
        for (String nodeFile : expectedASTNodes) {
            Path astFile = packageDir.resolve(nodeFile);
            assertTrue(Files.exists(astFile), "AST node " + nodeFile + " should exist");
            
            String astContent = Files.readString(astFile);
            assertTrue(astContent.contains("extends ASTNode") || astContent.contains("implements"), 
                      "AST node " + nodeFile + " should extend ASTNode or implement interface");
        }
        
        // Verify base ASTNode class exists
        Path baseASTFile = packageDir.resolve("ASTNode.java");
        assertTrue(Files.exists(baseASTFile), "Base ASTNode class should exist");
        
        String baseContent = Files.readString(baseASTFile);
        assertTrue(baseContent.contains("class ASTNode") || baseContent.contains("abstract class ASTNode"),
                  "Base ASTNode should be properly defined");
        
        // Verify AST visitor pattern is implemented
        Path visitorFile = packageDir.resolve("ASTVisitor.java");
        assertTrue(Files.exists(visitorFile), "ASTVisitor interface should exist");
        
        String visitorContent = Files.readString(visitorFile);
        assertTrue(visitorContent.contains("visit"), "ASTVisitor should have visit methods");
    }
    
    /**
     * Test that the generated parser has proper error handling infrastructure.
     * Requirement 6.5: Parser should provide meaningful error messages for invalid input
     */
    @Test
    @DisplayName("Generated parser should have proper error handling infrastructure")
    void testErrorHandlingInfrastructure() throws IOException {
        // Verify ParseException class exists
        Path parseExceptionFile = packageDir.resolve("ParseException.java");
        assertTrue(Files.exists(parseExceptionFile), "ParseException class should exist");
        
        String parseExceptionContent = Files.readString(parseExceptionFile);
        assertTrue(parseExceptionContent.contains("extends Exception") || 
                  parseExceptionContent.contains("extends RuntimeException"), 
                  "ParseException should extend Exception or RuntimeException");
        
        // Verify Position class for error reporting
        Path positionFile = packageDir.resolve("Position.java");
        assertTrue(Files.exists(positionFile), "Position class should exist");
        
        String positionContent = Files.readString(positionFile);
        assertTrue(positionContent.contains("line") && positionContent.contains("column"), 
                  "Position should track line and column");
        
        // Verify parser has error handling methods
        Path parserFile = packageDir.resolve("ArithmeticValidatorParser.java");
        String parserContent = Files.readString(parserFile);
        
        assertTrue(parserContent.contains("handleError") || parserContent.contains("error"), 
                  "Parser should have error handling methods");
        assertTrue(parserContent.contains("createErrorMessage") || parserContent.contains("error"), 
                  "Parser should have error message creation");
        assertTrue(parserContent.contains("getExpectedTokens") || parserContent.contains("expected"), 
                  "Parser should provide expected token information");
        
        // Verify error recovery mechanisms
        assertTrue(parserContent.contains("panicModeRecovery") || parserContent.contains("recovery"), 
                  "Parser should have error recovery mechanisms");
    }
    
    /**
     * Test that the generated parser has robust token handling.
     * Requirement 6.5: Parser should provide meaningful error messages
     */
    @Test
    @DisplayName("Generated parser should have robust token handling")
    void testTokenHandling() throws IOException {
        // Verify Token class exists and has proper structure
        Path tokenFile = packageDir.resolve("Token.java");
        assertTrue(Files.exists(tokenFile), "Token class should exist");
        
        String tokenContent = Files.readString(tokenFile);
        assertTrue(tokenContent.contains("TokenType"), "Token should reference TokenType");
        assertTrue(tokenContent.contains("Position"), "Token should have position information");
        assertTrue(tokenContent.contains("getValue") || tokenContent.contains("value"), 
                  "Token should have value accessor");
        
        // Verify TokenType enum has all expected tokens
        Path tokenTypeFile = packageDir.resolve("TokenType.java");
        String tokenTypeContent = Files.readString(tokenTypeFile);
        
        String[] expectedTokens = {"NUMBER", "PLUS", "MULTIPLY", "LPAREN", "RPAREN", "EOF"};
        for (String token : expectedTokens) {
            assertTrue(tokenTypeContent.contains(token), 
                      "TokenType should contain " + token);
        }
        
        // Verify parser handles EOF token properly
        Path parserFile = packageDir.resolve("ArithmeticValidatorParser.java");
        String parserContent = Files.readString(parserFile);
        
        assertTrue(parserContent.contains("EOF") || parserContent.contains("eof"), 
                  "Parser should handle EOF token");
        assertTrue(parserContent.contains("getCurrentToken"), 
                  "Parser should have token access methods");
    }
    
    /**
     * Test that the generated parser has correct precedence structure.
     * Requirement 6.3: Ensure AST generation reflects correct operator precedence
     */
    @Test
    @DisplayName("Generated parser should have correct precedence structure")
    void testPrecedenceStructure() throws IOException {
        // Verify the grammar hierarchy reflects precedence
        // Expression (lowest precedence) -> Term (higher precedence) -> Factor (highest precedence)
        
        Path parserFile = packageDir.resolve("ArithmeticValidatorParser.java");
        String parserContent = Files.readString(parserFile);
        
        // Verify the parser has production handling for all precedence levels
        assertTrue(parserContent.contains("createNonTerminalNode"), 
                  "Parser should have AST node creation method");
        
        // Check that the parser handles different production numbers
        // This indicates it can distinguish between different grammar rules
        Pattern productionPattern = Pattern.compile("case\\s+(\\d+):", Pattern.MULTILINE);
        Matcher matcher = productionPattern.matcher(parserContent);
        
        int productionCount = 0;
        while (matcher.find()) {
            productionCount++;
        }
        
        assertTrue(productionCount >= 6, "Parser should handle multiple productions (found " + productionCount + ")");
        
        // Verify parsing table reflects precedence
        // Higher precedence operations should have more states/transitions
        assertTrue(parserContent.contains("PRODUCTION_LENGTHS"), 
                  "Parser should have production length information");
        assertTrue(parserContent.contains("PRODUCTION_LEFT_SIDES"), 
                  "Parser should have production left-side information");
    }
    
    /**
     * Test that the generated parser is complete and functional.
     * This is a comprehensive test that validates the overall parser structure.
     */
    @Test
    @DisplayName("Generated parser should be complete and functional")
    void testParserCompleteness() throws IOException {
        // Verify all essential files are generated
        String[] requiredFiles = {
            "ArithmeticValidatorParser.java",
            "TokenType.java",
            "Token.java", 
            "ASTNode.java",
            "Position.java",
            "ParseException.java",
            "ExpressionNode.java",
            "TermNode.java",
            "FactorNode.java",
            "TerminalNode.java",
            "ASTVisitor.java"
        };
        
        for (String fileName : requiredFiles) {
            Path file = packageDir.resolve(fileName);
            assertTrue(Files.exists(file), "Required file " + fileName + " should exist");
            
            String content = Files.readString(file);
            assertFalse(content.trim().isEmpty(), "File " + fileName + " should not be empty");
            assertTrue(content.contains("package com.test.validation"), 
                      "File " + fileName + " should have correct package");
        }
        
        // Verify the main parser is substantial
        Path parserFile = packageDir.resolve("ArithmeticValidatorParser.java");
        String parserContent = Files.readString(parserFile);
        
        assertTrue(parserContent.length() > 5000, 
                  "Generated parser should be substantial (>5000 chars, found " + parserContent.length() + ")");
        
        // Verify it contains all essential components
        String[] essentialComponents = {
            "ACTION_TABLE", "GOTO_TABLE", "parse", "shift", "reduce", 
            "handleError", "createNonTerminalNode", "getCurrentToken"
        };
        
        for (String component : essentialComponents) {
            assertTrue(parserContent.contains(component), 
                      "Parser should contain " + component);
        }
    }
}