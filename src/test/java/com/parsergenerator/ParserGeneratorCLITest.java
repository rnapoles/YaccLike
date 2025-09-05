package com.parsergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ParserGenerator CLI functionality including command-line options,
 * help system, and various configuration combinations.
 */
class ParserGeneratorCLITest {
    
    @TempDir
    Path tempDir;
    
    private Path grammarFile;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;
    
    @BeforeEach
    void setUp() throws IOException {
        grammarFile = tempDir.resolve("test.grammar");
        
        // Create a simple valid grammar for testing
        String grammarContent = """
            NUMBER = {[0-9]+}
            PLUS = {\\+}
            
            Start = <Expr>
            
            <Expr> ::= <Expr> PLUS NUMBER | NUMBER
            """;
        Files.writeString(grammarFile, grammarContent);
        
        // Capture output streams
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
    
    @Test
    void testHelpOption() {
        int exitCode = ParserGenerator.runMain(new String[]{"--help"});
        
        assertEquals(0, exitCode, "Help option should return success");
        
        String output = outputStream.toString();
        assertTrue(output.contains("Yacc-like Parser Generator"), "Should show title");
        assertTrue(output.contains("Usage:"), "Should show usage");
        assertTrue(output.contains("Options:"), "Should show options");
        assertTrue(output.contains("Examples:"), "Should show examples");
        assertTrue(output.contains("Grammar File Format:"), "Should show format info");
        assertTrue(output.contains("--verbose"), "Should show verbose option");
        assertTrue(output.contains("--debug"), "Should show debug option");
        assertTrue(output.contains("--no-warnings"), "Should show no-warnings option");
        assertTrue(output.contains("--no-comments"), "Should show no-comments option");
    }
    
    @Test
    void testShortHelpOption() {
        int exitCode = ParserGenerator.runMain(new String[]{"-h"});
        
        assertEquals(0, exitCode, "Short help option should return success");
        
        String output = outputStream.toString();
        assertTrue(output.contains("Yacc-like Parser Generator"), "Should show title");
    }
    
    @Test
    void testVerboseOption() throws IOException {
        Path outputDir = tempDir.resolve("output");
        
        int exitCode = ParserGenerator.runMain(new String[]{
            "--verbose",
            "--output", outputDir.toString(),
            grammarFile.toString()
        });
        
        // Note: This will fail due to left recursion, but we're testing verbose output
        String output = outputStream.toString();
        assertTrue(output.contains("[INFO]"), "Verbose mode should show INFO messages");
        assertTrue(output.contains("Reading grammar file"), "Should show detailed steps");
    }
    
    @Test
    void testDebugOption() throws IOException {
        Path outputDir = tempDir.resolve("output");
        
        int exitCode = ParserGenerator.runMain(new String[]{
            "--debug",
            "--output", outputDir.toString(),
            grammarFile.toString()
        });
        
        String output = outputStream.toString();
        assertTrue(output.contains("[INFO]"), "Debug mode should show INFO messages");
        assertTrue(output.contains("[DEBUG]"), "Debug mode should show DEBUG messages");
        assertTrue(output.contains("Grammar file path:"), "Should show debug details");
    }
    
    @Test
    void testShortDebugOption() throws IOException {
        Path outputDir = tempDir.resolve("output");
        
        int exitCode = ParserGenerator.runMain(new String[]{
            "-d",
            "--output", outputDir.toString(),
            grammarFile.toString()
        });
        
        String output = outputStream.toString();
        assertTrue(output.contains("[DEBUG]"), "Short debug option should enable debug mode");
    }
    
    @Test
    void testNoWarningsOption() throws IOException {
        // Create a grammar that generates warnings but no errors
        String grammarContent = """
            UNUSED = {unused}
            NUMBER = {[0-9]+}
            
            Start = <Expr>
            
            <Expr> ::= NUMBER
            """;
        Files.writeString(grammarFile, grammarContent);
        
        Path outputDir = tempDir.resolve("output");
        
        int exitCode = ParserGenerator.runMain(new String[]{
            "--verbose",
            "--no-warnings",
            "--output", outputDir.toString(),
            grammarFile.toString()
        });
        
        assertEquals(0, exitCode, "Should succeed with valid grammar");
        
        String output = outputStream.toString();
        assertFalse(output.contains("[WARNING]"), "Should not show warnings with --no-warnings");
    }
    
    @Test
    void testWarningsOption() throws IOException {
        // Create a grammar that generates warnings but no errors
        String grammarContent = """
            UNUSED = {unused}
            NUMBER = {[0-9]+}
            
            Start = <Expr>
            
            <Expr> ::= NUMBER
            """;
        Files.writeString(grammarFile, grammarContent);
        
        Path outputDir = tempDir.resolve("output");
        
        int exitCode = ParserGenerator.runMain(new String[]{
            "--verbose",
            "--warnings",
            "--output", outputDir.toString(),
            grammarFile.toString()
        });
        
        assertEquals(0, exitCode, "Should succeed with valid grammar");
        
        String output = outputStream.toString();
        // The test should pass if warnings are enabled (which is the default behavior)
        // We're mainly testing that the option is parsed correctly
        assertTrue(output.contains("completed") || output.contains("successfully"), 
                  "Should complete processing");
    }
    
    @Test
    void testOutputDirectoryOption() throws IOException {
        // Use a simple grammar without left recursion
        String grammarContent = """
            NUMBER = {[0-9]+}
            
            Start = <Expr>
            
            <Expr> ::= NUMBER
            """;
        Files.writeString(grammarFile, grammarContent);
        
        Path outputDir = tempDir.resolve("custom-output");
        
        int exitCode = ParserGenerator.runMain(new String[]{
            "--output", outputDir.toString(),
            grammarFile.toString()
        });
        
        assertEquals(0, exitCode, "Should succeed with valid grammar");
        assertTrue(Files.exists(outputDir), "Output directory should be created");
    }
    
    @Test
    void testPackageOption() throws IOException {
        // Use a simple grammar without left recursion
        String grammarContent = """
            NUMBER = {[0-9]+}
            
            Start = <Expr>
            
            <Expr> ::= NUMBER
            """;
        Files.writeString(grammarFile, grammarContent);
        
        Path outputDir = tempDir.resolve("output");
        
        int exitCode = ParserGenerator.runMain(new String[]{
            "--package", "com.test.custom",
            "--output", outputDir.toString(),
            grammarFile.toString()
        });
        
        assertEquals(0, exitCode, "Should succeed with valid grammar");
        
        Path packageDir = outputDir.resolve("com").resolve("test").resolve("custom");
        assertTrue(Files.exists(packageDir), "Package directory should be created");
    }
    
    @Test
    void testGrammarNameOption() throws IOException {
        // Use a simple grammar without left recursion
        String grammarContent = """
            NUMBER = {[0-9]+}
            
            Start = <Expr>
            
            <Expr> ::= NUMBER
            """;
        Files.writeString(grammarFile, grammarContent);
        
        Path outputDir = tempDir.resolve("output");
        
        int exitCode = ParserGenerator.runMain(new String[]{
            "--name", "CustomParser",
            "--output", outputDir.toString(),
            grammarFile.toString()
        });
        
        assertEquals(0, exitCode, "Should succeed with valid grammar");
        
        // Check that the parser file has the custom name
        Path packageDir = outputDir.resolve("com").resolve("generated").resolve("parser");
        assertTrue(Files.exists(packageDir.resolve("CustomParserParser.java")), 
                  "Should generate parser with custom name");
    }
    
    @Test
    void testCombinedOptions() throws IOException {
        // Use a simple grammar without left recursion
        String grammarContent = """
            NUMBER = {[0-9]+}
            PLUS = {\\+}
            
            Start = <Expr>
            
            <Expr> ::= NUMBER PLUS <Expr> | NUMBER
            """;
        Files.writeString(grammarFile, grammarContent);
        
        Path outputDir = tempDir.resolve("output");
        
        int exitCode = ParserGenerator.runMain(new String[]{
            "--debug",
            "--no-warnings",
            "--no-comments",
            "--package", "com.example.parser",
            "--name", "ExprParser",
            "--output", outputDir.toString(),
            grammarFile.toString()
        });
        
        assertEquals(0, exitCode, "Should succeed with valid grammar");
        
        String output = outputStream.toString();
        assertTrue(output.contains("[DEBUG]"), "Should show debug output");
        
        Path packageDir = outputDir.resolve("com").resolve("example").resolve("parser");
        assertTrue(Files.exists(packageDir), "Package directory should be created");
        assertTrue(Files.exists(packageDir.resolve("ExprParserParser.java")), 
                  "Should generate parser with custom name");
    }
    
    @Test
    void testInvalidOption() {
        int exitCode = ParserGenerator.runMain(new String[]{"--invalid-option"});
        
        assertEquals(1, exitCode, "Invalid option should return error");
        
        String error = errorStream.toString();
        assertTrue(error.contains("Unknown option"), "Should report unknown option");
    }
    
    @Test
    void testMissingInputFile() {
        int exitCode = ParserGenerator.runMain(new String[]{"--verbose"});
        
        assertEquals(1, exitCode, "Missing input file should return error");
        
        String error = errorStream.toString();
        assertTrue(error.contains("No input grammar file specified"), 
                  "Should report missing input file");
    }
    
    @Test
    void testOptionRequiringValue() {
        int exitCode = ParserGenerator.runMain(new String[]{"--output"});
        
        assertEquals(1, exitCode, "Option requiring value should return error");
        
        String error = errorStream.toString();
        assertTrue(error.contains("requires a value"), "Should report missing value");
    }
    
    @Test
    void testMultipleInputFiles() {
        int exitCode = ParserGenerator.runMain(new String[]{
            "file1.grammar", 
            "file2.grammar"
        });
        
        assertEquals(1, exitCode, "Multiple input files should return error");
        
        String error = errorStream.toString();
        assertTrue(error.contains("Multiple input files"), "Should report multiple input files");
    }
    
    @Test
    void testNonExistentInputFile() {
        int exitCode = ParserGenerator.runMain(new String[]{"nonexistent.grammar"});
        
        assertEquals(1, exitCode, "Non-existent file should return error");
        
        String error = errorStream.toString();
        assertTrue(error.contains("Grammar file not found") || error.contains("Fatal error"), 
                  "Should report file not found");
    }
}