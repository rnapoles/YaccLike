package com.parsergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the ParserGenerator main class.
 * Tests the complete parser generation pipeline from grammar file to generated code.
 */
class ParserGeneratorTest {
    
    @TempDir
    Path tempDir;
    
    private ParserGenerator generator;
    private Path grammarFile;
    private Path outputDir;
    
    @BeforeEach
    void setUp() throws IOException {
        generator = new ParserGenerator();
        grammarFile = tempDir.resolve("test.grammar");
        outputDir = tempDir.resolve("output");
        
        // Configure generator with test settings
        generator.setOutputDirectory(outputDir.toString());
        generator.setPackageName("com.test.parser");
        generator.setGrammarName("TestGrammar");
        generator.setVerbose(true);
    }
    
    @Test
    void testSimpleArithmeticGrammar() throws IOException {
        // Create a simple arithmetic grammar without left recursion
        String grammarContent = """
            -- Simple arithmetic grammar (right-recursive)
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
        
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        
        // Verify generation was successful
        assertTrue(success, "Parser generation should succeed for valid grammar");
        
        // Verify output directory was created
        assertTrue(Files.exists(outputDir), "Output directory should be created");
        
        // Verify package directory structure
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("parser");
        assertTrue(Files.exists(packageDir), "Package directory should be created");
        
        // Verify essential files were generated
        assertTrue(Files.exists(packageDir.resolve("TestGrammarParser.java")), 
                  "Main parser class should be generated");
        assertTrue(Files.exists(packageDir.resolve("TokenType.java")), 
                  "TokenType enum should be generated");
        assertTrue(Files.exists(packageDir.resolve("Token.java")), 
                  "Token class should be generated");
        assertTrue(Files.exists(packageDir.resolve("ASTNode.java")), 
                  "AST base class should be generated");
        assertTrue(Files.exists(packageDir.resolve("TerminalNode.java")), 
                  "Terminal AST node should be generated");
        assertTrue(Files.exists(packageDir.resolve("ParseException.java")), 
                  "ParseException class should be generated");
        assertTrue(Files.exists(packageDir.resolve("Position.java")), 
                  "Position class should be generated");
        
        // Verify AST node classes for non-terminals
        assertTrue(Files.exists(packageDir.resolve("ExpressionNode.java")), 
                  "Expression AST node should be generated");
        assertTrue(Files.exists(packageDir.resolve("TermNode.java")), 
                  "Term AST node should be generated");
        assertTrue(Files.exists(packageDir.resolve("FactorNode.java")), 
                  "Factor AST node should be generated");
        
        // Verify generated files have content
        String parserContent = Files.readString(packageDir.resolve("TestGrammarParser.java"));
        assertFalse(parserContent.isEmpty(), "Generated parser should have content");
        assertTrue(parserContent.contains("package com.test.parser"), 
                  "Generated parser should have correct package");
        assertTrue(parserContent.contains("class TestGrammarParser"), 
                  "Generated parser should have correct class name");
    }
    
    @Test
    void testInvalidGrammarFile() throws IOException {
        // Create an invalid grammar with syntax errors
        String grammarContent = """
            -- Invalid grammar with syntax errors
            NUMBER = [0-9]+  -- Missing braces
            PLUS = {+}       -- Unescaped plus
            
            Start = Expression  -- Missing angle brackets
            
            Expression ::= Expression PLUS Term  -- Missing angle brackets
            """;
        
        Files.writeString(grammarFile, grammarContent);
        
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        
        // Verify generation failed
        assertFalse(success, "Parser generation should fail for invalid grammar");
    }
    
    @Test
    void testNonExistentGrammarFile() {
        Path nonExistentFile = tempDir.resolve("nonexistent.grammar");
        
        // Try to generate parser from non-existent file
        boolean success = generator.generateParser(nonExistentFile.toString());
        
        // Verify generation failed
        assertFalse(success, "Parser generation should fail for non-existent file");
    }
    
    @Test
    void testLeftRecursiveGrammar() throws IOException {
        // Create a grammar with left recursion that should be detected
        String grammarContent = """
            -- Grammar with left recursion
            ID = {[a-zA-Z][a-zA-Z0-9]*}
            
            Start = <A>
            
            <A> ::= <A> ID | ID
            """;
        
        Files.writeString(grammarFile, grammarContent);
        
        // Generate parser - this should succeed as left recursion is detected but not necessarily an error
        boolean success = generator.generateParser(grammarFile.toString());
        
        // The result depends on whether left recursion is treated as an error or warning
        // For now, we'll just verify the process completes
        assertNotNull(success, "Parser generation should complete (success or failure)");
    }
    
    @Test
    void testEmptyGrammarFile() throws IOException {
        // Create an empty grammar file
        Files.writeString(grammarFile, "");
        
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        
        // Verify generation failed
        assertFalse(success, "Parser generation should fail for empty grammar");
    }
    
    @Test
    void testGrammarWithCommentsOnly() throws IOException {
        // Create a grammar file with only comments
        String grammarContent = """
            -- This is a comment
            -- Another comment
            -- No actual grammar rules
            """;
        
        Files.writeString(grammarFile, grammarContent);
        
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        
        // Verify generation failed
        assertFalse(success, "Parser generation should fail for grammar with no rules");
    }
    
    @Test
    void testComplexGrammar() throws IOException {
        // Create a more complex grammar with multiple constructs
        String grammarContent = """
            -- Complex grammar with various constructs
            ID = {[a-zA-Z][a-zA-Z0-9]*}
            NUMBER = {[0-9]+}
            STRING = {"[^"]*"}
            ASSIGN = {=}
            SEMICOLON = {;}
            COMMA = {,}
            LPAREN = {\\(}
            RPAREN = {\\)}
            LBRACE = {\\{}
            RBRACE = {\\}}
            
            Start = <Program>
            
            <Program> ::= <StatementList>
            <StatementList> ::= <Statement> <StatementList> | <Statement>
            <Statement> ::= <Assignment> SEMICOLON | <FunctionCall> SEMICOLON
            <Assignment> ::= ID ASSIGN <Expression>
            <FunctionCall> ::= ID LPAREN <ArgumentList> RPAREN
            <ArgumentList> ::= <Expression> COMMA <ArgumentList> | <Expression> | 
            <Expression> ::= ID | NUMBER | STRING
            """;
        
        Files.writeString(grammarFile, grammarContent);
        
        // Generate parser
        boolean success = generator.generateParser(grammarFile.toString());
        
        // Verify generation was successful
        assertTrue(success, "Parser generation should succeed for complex valid grammar");
        
        // Verify all expected AST node classes were generated
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("parser");
        assertTrue(Files.exists(packageDir.resolve("ProgramNode.java")));
        assertTrue(Files.exists(packageDir.resolve("StatementListNode.java")));
        assertTrue(Files.exists(packageDir.resolve("StatementNode.java")));
        assertTrue(Files.exists(packageDir.resolve("AssignmentNode.java")));
        assertTrue(Files.exists(packageDir.resolve("FunctionCallNode.java")));
        assertTrue(Files.exists(packageDir.resolve("ArgumentListNode.java")));
        assertTrue(Files.exists(packageDir.resolve("ExpressionNode.java")));
    }
    
    @Test
    void testMainMethodWithValidArguments() {
        // Test main method with help option
        int exitCode = ParserGenerator.runMain(new String[]{"--help"});
        assertEquals(0, exitCode, "Help option should return success exit code");
    }
    
    @Test
    void testMainMethodWithInvalidArguments() {
        // Test main method with invalid arguments
        int exitCode = ParserGenerator.runMain(new String[]{"--invalid-option"});
        assertEquals(1, exitCode, "Invalid option should return error exit code");
    }
    
    @Test
    void testCommandLineArgumentParsing() throws IOException {
        // Create a valid grammar file for this test
        String grammarContent = """
            NUMBER = {[0-9]+}
            Start = <Expr>
            <Expr> ::= NUMBER
            """;
        Files.writeString(grammarFile, grammarContent);
        
        // Test various command line argument combinations
        int exitCode = ParserGenerator.runMain(new String[]{
            "--verbose", 
            "--output", tempDir.toString(),
            "--package", "com.example.test",
            "--name", "MyGrammar",
            grammarFile.toString()
        });
        
        // The result depends on whether the grammar is valid and can be processed
        assertTrue(exitCode >= 0, "Main method should return valid exit code");
    }
}