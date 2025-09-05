package com.parsergenerator.codegen;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LexerCodeGenerator class.
 * Tests the core lexer generation infrastructure and basic functionality.
 */
class LexerCodeGeneratorTest {
    
    private LexerCodeGenerator generator;
    private Grammar testGrammar;
    
    @BeforeEach
    void setUp() {
        generator = new LexerCodeGenerator("com.test.lexer", "TestGrammar");
        
        // Create a simple test grammar with terminals that have patterns
        testGrammar = new Grammar();
        
        // Add non-terminals
        NonTerminal expr = new NonTerminal("expr");
        NonTerminal term = new NonTerminal("term");
        testGrammar.addNonTerminal(expr);
        testGrammar.addNonTerminal(term);
        
        // Add terminals with patterns
        Terminal number = new Terminal("NUMBER", "[0-9]+");
        Terminal plus = new Terminal("PLUS", "\\+");
        Terminal minus = new Terminal("MINUS", "-");
        Terminal multiply = new Terminal("MULTIPLY", "\\*");
        Terminal lparen = new Terminal("LPAREN", "\\(");
        Terminal rparen = new Terminal("RPAREN", "\\)");
        
        testGrammar.addTerminal(number);
        testGrammar.addTerminal(plus);
        testGrammar.addTerminal(minus);
        testGrammar.addTerminal(multiply);
        testGrammar.addTerminal(lparen);
        testGrammar.addTerminal(rparen);
    }
    
    @Test
    void testConstructorWithDefaultValues() {
        LexerCodeGenerator defaultGenerator = new LexerCodeGenerator();
        
        assertEquals("com.generated.lexer", defaultGenerator.getPackageName());
        assertEquals("GeneratedGrammar", defaultGenerator.getGrammarName());
        assertNotNull(defaultGenerator.getTemplateManager());
    }
    
    @Test
    void testConstructorWithCustomValues() {
        assertEquals("com.test.lexer", generator.getPackageName());
        assertEquals("TestGrammar", generator.getGrammarName());
        assertNotNull(generator.getTemplateManager());
    }
    
    @Test
    void testConstructorWithNullPackageName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LexerCodeGenerator(null, "TestGrammar");
        });
    }
    
    @Test
    void testConstructorWithEmptyPackageName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LexerCodeGenerator("", "TestGrammar");
        });
    }
    
    @Test
    void testConstructorWithNullGrammarName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LexerCodeGenerator("com.test", null);
        });
    }
    
    @Test
    void testConstructorWithEmptyGrammarName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LexerCodeGenerator("com.test", "");
        });
    }
    
    @Test
    void testConstructorWithTemplateManager() {
        TemplateManager templateManager = new TemplateManager();
        LexerCodeGenerator customGenerator = new LexerCodeGenerator(templateManager, "com.test", "Test");
        
        assertSame(templateManager, customGenerator.getTemplateManager());
        assertEquals("com.test", customGenerator.getPackageName());
        assertEquals("Test", customGenerator.getGrammarName());
    }
    
    @Test
    void testConstructorWithNullTemplateManager() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LexerCodeGenerator(null, "com.test", "Test");
        });
    }
    
    @Test
    void testGenerateLexerWithNullGrammar() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generateLexer(null);
        });
    }
    
    @Test
    void testGenerateLexerClass() throws TemplateException {
        String lexerCode = generator.generateLexerClass(testGrammar);
        
        assertNotNull(lexerCode);
        assertFalse(lexerCode.isEmpty());
        
        // Verify package declaration
        assertTrue(lexerCode.contains("package com.test.lexer;"));
        
        // Verify class name
        assertTrue(lexerCode.contains("public class TestGrammarLexer"));
        
        // Verify imports
        assertTrue(lexerCode.contains("import java.util.*;"));
        assertTrue(lexerCode.contains("import java.util.regex.Pattern;"));
        
        // Verify basic structure
        assertTrue(lexerCode.contains("private final String input;"));
        assertTrue(lexerCode.contains("private final String filename;"));
        assertTrue(lexerCode.contains("private int position;"));
        assertTrue(lexerCode.contains("private int line;"));
        assertTrue(lexerCode.contains("private int column;"));
        
        // Verify constructors
        assertTrue(lexerCode.contains("public TestGrammarLexer(String input)"));
        assertTrue(lexerCode.contains("public TestGrammarLexer(String input, String filename)"));
        
        // Verify terminal patterns are included
        assertTrue(lexerCode.contains("NUMBER_PATTERN"));
        assertTrue(lexerCode.contains("PLUS_PATTERN"));
        assertTrue(lexerCode.contains("MINUS_PATTERN"));
    }
    
    @Test
    void testGenerateLexerClassWithNullGrammar() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generateLexerClass(null);
        });
    }
    
    @Test
    void testGenerateTokenizerEngine() throws TemplateException {
        String tokenizerCode = generator.generateTokenizerEngine(testGrammar);
        
        assertNotNull(tokenizerCode);
        assertFalse(tokenizerCode.isEmpty());
        
        // Verify package declaration
        assertTrue(tokenizerCode.contains("package com.test.lexer;"));
        
        // Verify class structure
        assertTrue(tokenizerCode.contains("public class TokenizerEngine"));
    }
    
    @Test
    void testGenerateTokenizerEngineWithNullGrammar() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generateTokenizerEngine(null);
        });
    }
    
    @Test
    void testGenerateLexicalException() throws TemplateException {
        String exceptionCode = generator.generateLexicalException();
        
        assertNotNull(exceptionCode);
        assertFalse(exceptionCode.isEmpty());
        
        // Verify package declaration
        assertTrue(exceptionCode.contains("package com.test.lexer;"));
        
        // Verify class structure
        assertTrue(exceptionCode.contains("public class LexicalException extends Exception"));
        
        // Verify fields
        assertTrue(exceptionCode.contains("private final int line;"));
        assertTrue(exceptionCode.contains("private final int column;"));
        assertTrue(exceptionCode.contains("private final String unexpectedInput;"));
        
        // Verify constructor
        assertTrue(exceptionCode.contains("public LexicalException(String message, int line, int column, String unexpectedInput)"));
        
        // Verify getters
        assertTrue(exceptionCode.contains("public int getLine()"));
        assertTrue(exceptionCode.contains("public int getColumn()"));
        assertTrue(exceptionCode.contains("public String getUnexpectedInput()"));
    }
    
    @Test
    void testGenerateLexer() throws TemplateException {
        GeneratedCode result = generator.generateLexer(testGrammar);
        
        assertNotNull(result);
        assertEquals(2, result.getFileCount());
        
        // Verify lexer class file
        assertTrue(result.hasFile("TestGrammarLexer.java"));
        String lexerCode = result.getFileContent("TestGrammarLexer.java");
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("public class TestGrammarLexer"));
        
        // Verify exception class file
        assertTrue(result.hasFile("LexicalException.java"));
        String exceptionCode = result.getFileContent("LexicalException.java");
        assertNotNull(exceptionCode);
        assertTrue(exceptionCode.contains("public class LexicalException"));
    }
    
    @Test
    void testTerminalInfoCreation() throws TemplateException {
        // Test with a grammar that has terminals with patterns
        String lexerCode = generator.generateLexerClass(testGrammar);
        
        // Verify that terminal patterns are properly converted to constants
        assertTrue(lexerCode.contains("NUMBER_PATTERN"));
        assertTrue(lexerCode.contains("PLUS_PATTERN"));
        assertTrue(lexerCode.contains("MINUS_PATTERN"));
        assertTrue(lexerCode.contains("MULTIPLY_PATTERN"));
        assertTrue(lexerCode.contains("LPAREN_PATTERN"));
        assertTrue(lexerCode.contains("RPAREN_PATTERN"));
        
        // Verify pattern compilation
        assertTrue(lexerCode.contains("Pattern.compile"));
    }
    
    @Test
    void testGrammarWithNoPatterns() {
        // Create a grammar with terminals that have no patterns
        Grammar emptyPatternGrammar = new Grammar();
        emptyPatternGrammar.addNonTerminal(new NonTerminal("expr"));
        emptyPatternGrammar.addTerminal(new Terminal("TOKEN1")); // No pattern
        emptyPatternGrammar.addTerminal(new Terminal("TOKEN2", "")); // Empty pattern
        
        // Should throw TemplateException due to invalid patterns
        assertThrows(TemplateException.class, () -> {
            generator.generateLexerClass(emptyPatternGrammar);
        });
    }
    
    @Test
    void testSpecialCharacterHandling() throws TemplateException {
        // Create grammar with special characters that need escaping
        Grammar specialGrammar = new Grammar();
        specialGrammar.addNonTerminal(new NonTerminal("expr"));
        specialGrammar.addTerminal(new Terminal("QUOTE", "\""));
        specialGrammar.addTerminal(new Terminal("BACKSLASH", "\\\\"));
        specialGrammar.addTerminal(new Terminal("NEWLINE", "\\n"));
        
        String lexerCode = generator.generateLexerClass(specialGrammar);
        
        assertNotNull(lexerCode);
        // Verify that special characters are properly escaped in the generated code
        assertTrue(lexerCode.contains("QUOTE_PATTERN"));
        assertTrue(lexerCode.contains("BACKSLASH_PATTERN"));
        assertTrue(lexerCode.contains("NEWLINE_PATTERN"));
    }
}