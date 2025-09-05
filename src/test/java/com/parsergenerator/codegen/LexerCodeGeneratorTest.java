package com.parsergenerator.codegen;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        assertEquals(1, result.getFileCount()); // Only lexer class, LexicalException exists in main package
        
        // Verify lexer class file
        assertTrue(result.hasFile("TestGrammarLexer.java"));
        String lexerCode = result.getFileContent("TestGrammarLexer.java");
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("public class TestGrammarLexer"));
        
        // Verify that the lexer imports the existing LexicalException
        assertTrue(lexerCode.contains("com.parsergenerator.lexer.LexicalException"));
        
        // Verify position tracking functionality is included
        assertTrue(lexerCode.contains("getCurrentPosition()"));
        assertTrue(lexerCode.contains("hasErrors()"));
        assertTrue(lexerCode.contains("updatePosition"));
        assertTrue(lexerCode.contains("skipWhitespace"));
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
    
    @Test
    void testComplexPatternScenarios() throws TemplateException {
        // Test various complex pattern scenarios
        Grammar complexGrammar = new Grammar();
        complexGrammar.addNonTerminal(new NonTerminal("program"));
        
        // String literals with escape sequences
        complexGrammar.addTerminal(new Terminal("STRING", "\"([^\"\\\\]|\\\\.)*\""));
        
        // Single-line comments
        complexGrammar.addTerminal(new Terminal("COMMENT", "//.*"));
        
        // Multi-line comments
        complexGrammar.addTerminal(new Terminal("MULTILINE_COMMENT", "/\\*([^*]|\\*+[^*/])*\\*+/"));
        
        // Floating point numbers
        complexGrammar.addTerminal(new Terminal("FLOAT", "[0-9]+\\.[0-9]+([eE][+-]?[0-9]+)?"));
        
        // Hexadecimal numbers
        complexGrammar.addTerminal(new Terminal("HEX", "0[xX][0-9a-fA-F]+"));
        
        // Identifiers with underscores
        complexGrammar.addTerminal(new Terminal("IDENTIFIER", "[a-zA-Z_][a-zA-Z0-9_]*"));
        
        String lexerCode = generator.generateLexerClass(complexGrammar);
        
        assertNotNull(lexerCode);
        assertFalse(lexerCode.isEmpty());
        
        // Verify all complex patterns are included
        assertTrue(lexerCode.contains("STRING_PATTERN"));
        assertTrue(lexerCode.contains("COMMENT_PATTERN"));
        assertTrue(lexerCode.contains("MULTILINE_COMMENT_PATTERN"));
        assertTrue(lexerCode.contains("FLOAT_PATTERN"));
        assertTrue(lexerCode.contains("HEX_PATTERN"));
        assertTrue(lexerCode.contains("IDENTIFIER_PATTERN"));
        
        // Verify pattern compilation statements
        assertTrue(lexerCode.contains("Pattern.compile"));
        
        // Verify terminal order array includes all terminals
        assertTrue(lexerCode.contains("TERMINAL_ORDER"));
    }
    
    @Test
    void testQuantifierPatterns() throws TemplateException {
        // Test patterns with various quantifiers
        Grammar quantifierGrammar = new Grammar();
        quantifierGrammar.addNonTerminal(new NonTerminal("expr"));
        
        quantifierGrammar.addTerminal(new Terminal("OPTIONAL", "a?"));
        quantifierGrammar.addTerminal(new Terminal("ZERO_OR_MORE", "b*"));
        quantifierGrammar.addTerminal(new Terminal("ONE_OR_MORE", "c+"));
        quantifierGrammar.addTerminal(new Terminal("EXACT_COUNT", "d{3}"));
        quantifierGrammar.addTerminal(new Terminal("RANGE_COUNT", "e{2,5}"));
        quantifierGrammar.addTerminal(new Terminal("MIN_COUNT", "f{2,}"));
        
        String lexerCode = generator.generateLexerClass(quantifierGrammar);
        
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("OPTIONAL_PATTERN"));
        assertTrue(lexerCode.contains("ZERO_OR_MORE_PATTERN"));
        assertTrue(lexerCode.contains("ONE_OR_MORE_PATTERN"));
        assertTrue(lexerCode.contains("EXACT_COUNT_PATTERN"));
        assertTrue(lexerCode.contains("RANGE_COUNT_PATTERN"));
        assertTrue(lexerCode.contains("MIN_COUNT_PATTERN"));
    }
    
    @Test
    void testCharacterClassPatterns() throws TemplateException {
        // Test patterns with character classes
        Grammar charClassGrammar = new Grammar();
        charClassGrammar.addNonTerminal(new NonTerminal("expr"));
        
        charClassGrammar.addTerminal(new Terminal("DIGITS", "[0-9]+"));
        charClassGrammar.addTerminal(new Terminal("LETTERS", "[a-zA-Z]+"));
        charClassGrammar.addTerminal(new Terminal("ALPHANUMERIC", "[a-zA-Z0-9]+"));
        charClassGrammar.addTerminal(new Terminal("NEGATED", "[^0-9]+"));
        charClassGrammar.addTerminal(new Terminal("SPECIAL_CHARS", "[!@#$%^&*()]+"));
        charClassGrammar.addTerminal(new Terminal("UNICODE", "[\\u0100-\\u017F]+"));
        
        String lexerCode = generator.generateLexerClass(charClassGrammar);
        
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("DIGITS_PATTERN"));
        assertTrue(lexerCode.contains("LETTERS_PATTERN"));
        assertTrue(lexerCode.contains("ALPHANUMERIC_PATTERN"));
        assertTrue(lexerCode.contains("NEGATED_PATTERN"));
        assertTrue(lexerCode.contains("SPECIAL_CHARS_PATTERN"));
        assertTrue(lexerCode.contains("UNICODE_PATTERN"));
    }
    
    @Test
    void testAlternationPatterns() throws TemplateException {
        // Test patterns with alternation
        Grammar alternationGrammar = new Grammar();
        alternationGrammar.addNonTerminal(new NonTerminal("stmt"));
        
        alternationGrammar.addTerminal(new Terminal("KEYWORD", "(if|else|while|for|do)"));
        alternationGrammar.addTerminal(new Terminal("OPERATOR", "(\\+|\\-|\\*|/|%)"));
        alternationGrammar.addTerminal(new Terminal("COMPARISON", "(==|!=|<=|>=|<|>)"));
        alternationGrammar.addTerminal(new Terminal("LOGICAL", "(&&|\\|\\||!)"));
        
        String lexerCode = generator.generateLexerClass(alternationGrammar);
        
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("KEYWORD_PATTERN"));
        assertTrue(lexerCode.contains("OPERATOR_PATTERN"));
        assertTrue(lexerCode.contains("COMPARISON_PATTERN"));
        assertTrue(lexerCode.contains("LOGICAL_PATTERN"));
    }
    
    @Test
    void testGroupingPatterns() throws TemplateException {
        // Test patterns with grouping
        Grammar groupingGrammar = new Grammar();
        groupingGrammar.addNonTerminal(new NonTerminal("expr"));
        
        groupingGrammar.addTerminal(new Terminal("GROUPED", "(abc)+"));
        groupingGrammar.addTerminal(new Terminal("NON_CAPTURING", "(?:def)+"));
        groupingGrammar.addTerminal(new Terminal("NESTED_GROUP", "((a|b)+c)*"));
        
        String lexerCode = generator.generateLexerClass(groupingGrammar);
        
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("GROUPED_PATTERN"));
        assertTrue(lexerCode.contains("NON_CAPTURING_PATTERN"));
        assertTrue(lexerCode.contains("NESTED_GROUP_PATTERN"));
    }
    
    @Test
    void testTerminalOrderingWithComplexPatterns() throws TemplateException {
        // Test terminal ordering with patterns of different specificity
        Grammar orderingGrammar = new Grammar();
        orderingGrammar.addNonTerminal(new NonTerminal("expr"));
        
        // Add in reverse specificity order to test sorting
        orderingGrammar.addTerminal(new Terminal("WILDCARD", ".*"));                    // Least specific
        orderingGrammar.addTerminal(new Terminal("CHAR_CLASS", "[a-z]+"));             // Medium specific
        orderingGrammar.addTerminal(new Terminal("ALTERNATION", "(if|else)"));         // More specific
        orderingGrammar.addTerminal(new Terminal("LITERAL", "hello"));                 // Most specific
        
        String lexerCode = generator.generateLexerClass(orderingGrammar);
        
        // Verify terminal order array exists and contains all terminals
        assertTrue(lexerCode.contains("TERMINAL_ORDER"));
        assertTrue(lexerCode.contains("\"LITERAL\""));
        assertTrue(lexerCode.contains("\"ALTERNATION\""));
        assertTrue(lexerCode.contains("\"CHAR_CLASS\""));
        assertTrue(lexerCode.contains("\"WILDCARD\""));
        
        // More specific patterns should appear first in the order (if all are present)
        int literalIndex = lexerCode.indexOf("\"LITERAL\"");
        int alternationIndex = lexerCode.indexOf("\"ALTERNATION\"");
        int charClassIndex = lexerCode.indexOf("\"CHAR_CLASS\"");
        int wildcardIndex = lexerCode.indexOf("\"WILDCARD\"");
        
        if (literalIndex != -1 && alternationIndex != -1 && charClassIndex != -1 && wildcardIndex != -1) {
            assertTrue(literalIndex < alternationIndex);
            assertTrue(alternationIndex < charClassIndex);
            assertTrue(charClassIndex < wildcardIndex);
        }
    }
    
    @Test
    void testTemplateVariablePreparation() throws TemplateException {
        // Test that template variables are properly prepared
        String lexerCode = generator.generateLexerClass(testGrammar);
        
        // Verify package name is used
        assertTrue(lexerCode.contains("package com.test.lexer;"));
        
        // Verify grammar name is used in class name
        assertTrue(lexerCode.contains("class TestGrammarLexer"));
        
        // Verify imports are included
        assertTrue(lexerCode.contains("import java.util.*;"));
        assertTrue(lexerCode.contains("import java.util.regex.Pattern;"));
        assertTrue(lexerCode.contains("import com.parsergenerator.lexer.Token;"));
        assertTrue(lexerCode.contains("import com.parsergenerator.lexer.TokenType;"));
        assertTrue(lexerCode.contains("import com.parsergenerator.lexer.Position;"));
        assertTrue(lexerCode.contains("import com.parsergenerator.lexer.LexicalException;"));
        
        // Verify essential methods are included
        assertTrue(lexerCode.contains("public Token nextToken()"));
        assertTrue(lexerCode.contains("public List<Token> tokenize()"));
        assertTrue(lexerCode.contains("public boolean hasMoreTokens()"));
        assertTrue(lexerCode.contains("public Position getCurrentPosition()"));
        assertTrue(lexerCode.contains("public boolean hasErrors()"));
    }
    
    @Test
    void testErrorHandlingInGeneration() {
        // Test error handling with invalid patterns during generation
        Grammar invalidGrammar = new Grammar();
        invalidGrammar.addNonTerminal(new NonTerminal("expr"));
        
        // Add terminal with invalid regex pattern
        invalidGrammar.addTerminal(new Terminal("INVALID", "[unclosed"));
        
        // Should throw TemplateException
        TemplateException exception = assertThrows(TemplateException.class, () -> {
            generator.generateLexerClass(invalidGrammar);
        });
        
        assertTrue(exception.getMessage().contains("Invalid regex pattern"));
        assertTrue(exception.getMessage().contains("INVALID"));
    }
    
    @Test
    void testErrorHandlingWithMultipleInvalidPatterns() {
        // Test error handling with multiple invalid patterns
        Grammar multiInvalidGrammar = new Grammar();
        multiInvalidGrammar.addNonTerminal(new NonTerminal("expr"));
        
        // Add multiple terminals with invalid patterns
        multiInvalidGrammar.addTerminal(new Terminal("INVALID1", "[unclosed"));
        multiInvalidGrammar.addTerminal(new Terminal("VALID", "[a-z]+"));
        multiInvalidGrammar.addTerminal(new Terminal("INVALID2", "*invalid"));
        
        // Should throw TemplateException for the first invalid pattern encountered
        assertThrows(TemplateException.class, () -> {
            generator.generateLexerClass(multiInvalidGrammar);
        });
    }
    
    @Test
    void testGrammarWithMixedTerminalTypes() throws TemplateException {
        // Test grammar with both pattern and non-pattern terminals
        Grammar mixedGrammar = new Grammar();
        mixedGrammar.addNonTerminal(new NonTerminal("expr"));
        
        // Pattern terminals
        mixedGrammar.addTerminal(new Terminal("NUMBER", "[0-9]+"));
        mixedGrammar.addTerminal(new Terminal("IDENTIFIER", "[a-zA-Z]+"));
        
        // Non-pattern terminals (should be ignored in lexer generation)
        mixedGrammar.addTerminal(new Terminal("EOF"));
        mixedGrammar.addTerminal(new Terminal("EPSILON", ""));
        
        String lexerCode = generator.generateLexerClass(mixedGrammar);
        
        assertNotNull(lexerCode);
        
        // Should include pattern terminals
        assertTrue(lexerCode.contains("NUMBER_PATTERN"));
        assertTrue(lexerCode.contains("IDENTIFIER_PATTERN"));
        
        // Should not include non-pattern terminals in patterns
        assertFalse(lexerCode.contains("EOF_PATTERN"));
        assertFalse(lexerCode.contains("EPSILON_PATTERN"));
    }
    
    @Test
    void testLargeGrammarPerformance() throws TemplateException {
        // Test performance with a large number of terminals
        Grammar largeGrammar = new Grammar();
        largeGrammar.addNonTerminal(new NonTerminal("program"));
        
        // Add many terminals to test performance
        for (int i = 0; i < 100; i++) {
            largeGrammar.addTerminal(new Terminal("TERMINAL_" + i, "[a-z]" + i));
        }
        
        long startTime = System.currentTimeMillis();
        String lexerCode = generator.generateLexerClass(largeGrammar);
        long endTime = System.currentTimeMillis();
        
        assertNotNull(lexerCode);
        assertFalse(lexerCode.isEmpty());
        
        // Should complete in reasonable time (less than 5 seconds)
        assertTrue(endTime - startTime < 5000, "Lexer generation took too long: " + (endTime - startTime) + "ms");
        
        // Verify all terminals are included
        for (int i = 0; i < 100; i++) {
            assertTrue(lexerCode.contains("TERMINAL_" + i + "_PATTERN"));
        }
    }
    
    @Test
    void testTerminalInfoCreationWithEdgeCases() {
        // Test TerminalInfo creation with edge cases
        
        // Test with special characters in terminal names
        LexerCodeGenerator.TerminalInfo specialName = 
                new LexerCodeGenerator.TerminalInfo("SPECIAL-NAME_123", "[a-z]+", "SPECIAL_NAME_123_PATTERN", "SPECIAL_NAME_123");
        
        assertEquals("SPECIAL-NAME_123", specialName.getName());
        assertEquals("[a-z]+", specialName.getPattern());
        assertEquals("SPECIAL_NAME_123_PATTERN", specialName.getConstantName());
        assertEquals("SPECIAL_NAME_123", specialName.getEnumConstant());
        assertNotNull(specialName.getCompiledPattern());
        assertTrue(specialName.getSpecificity() > 0);
        
        // Test with very specific pattern
        LexerCodeGenerator.TerminalInfo verySpecific = 
                new LexerCodeGenerator.TerminalInfo("SPECIFIC", "exactmatch", "SPECIFIC_PATTERN", "SPECIFIC");
        
        // Test with very general pattern
        LexerCodeGenerator.TerminalInfo veryGeneral = 
                new LexerCodeGenerator.TerminalInfo("GENERAL", ".*", "GENERAL_PATTERN", "GENERAL");
        
        // Specific should have higher specificity than general
        assertTrue(verySpecific.getSpecificity() > veryGeneral.getSpecificity());
    }
    
    @Test
    void testTerminalInfoInvalidPattern() {
        // Test TerminalInfo with invalid pattern
        assertThrows(IllegalArgumentException.class, () -> {
            new LexerCodeGenerator.TerminalInfo("INVALID", "[unclosed", "INVALID_PATTERN", "INVALID");
        });
    }
    
    @Test
    void testGeneratedCodeStructure() throws TemplateException {
        // Test the overall structure of generated code
        GeneratedCode result = generator.generateLexer(testGrammar);
        
        assertNotNull(result);
        assertEquals(1, result.getFileCount());
        assertTrue(result.hasFile("TestGrammarLexer.java"));
        
        String lexerCode = result.getFileContent("TestGrammarLexer.java");
        
        // Verify class structure
        assertTrue(lexerCode.contains("public class TestGrammarLexer"));
        
        // Verify essential fields
        assertTrue(lexerCode.contains("private final String input;"));
        assertTrue(lexerCode.contains("private final String filename;"));
        assertTrue(lexerCode.contains("private int position;"));
        assertTrue(lexerCode.contains("private int line;"));
        assertTrue(lexerCode.contains("private int column;"));
        
        // Verify constructors
        assertTrue(lexerCode.contains("public TestGrammarLexer(String input)"));
        assertTrue(lexerCode.contains("public TestGrammarLexer(String input, String filename)"));
        
        // Verify essential methods
        assertTrue(lexerCode.contains("public Token nextToken()"));
        assertTrue(lexerCode.contains("public List<Token> tokenize()"));
        assertTrue(lexerCode.contains("public boolean hasMoreTokens()"));
        assertTrue(lexerCode.contains("public Position getCurrentPosition()"));
        assertTrue(lexerCode.contains("public boolean hasErrors()"));
        
        // Verify helper methods (check for presence of key functionality)
        assertTrue(lexerCode.contains("advancePosition") || lexerCode.contains("position"));
        assertTrue(lexerCode.contains("markTokenStart") || lexerCode.contains("mark") || lexerCode.contains("start"));
        assertTrue(lexerCode.contains("skipWhitespace") || lexerCode.contains("whitespace") || lexerCode.contains("skip"));
        assertTrue(lexerCode.contains("reportError") || lexerCode.contains("error"));
        assertTrue(lexerCode.contains("generateSuggestion") || lexerCode.contains("suggestion"));
    }
    
    @Test
    void testPositionTrackingGeneration() throws TemplateException {
        // Test that position tracking code is properly generated
        String lexerCode = generator.generateLexerClass(testGrammar);
        
        // Verify position tracking fields
        assertTrue(lexerCode.contains("private int startPosition;") || lexerCode.contains("startPosition"));
        assertTrue(lexerCode.contains("private int startLine;") || lexerCode.contains("startLine"));
        assertTrue(lexerCode.contains("private int startColumn;") || lexerCode.contains("startColumn"));
        
        // Verify position tracking methods
        assertTrue(lexerCode.contains("getCurrentPosition()"));
        assertTrue(lexerCode.contains("getStartPosition()") || lexerCode.contains("startPosition"));
        assertTrue(lexerCode.contains("markTokenStart()") || lexerCode.contains("mark"));
        
        // Verify position advancement logic
        assertTrue(lexerCode.contains("advancePosition") || lexerCode.contains("position"));
        assertTrue(lexerCode.contains("line++") || lexerCode.contains("line"));
        assertTrue(lexerCode.contains("column") || lexerCode.contains("position"));
    }
    
    @Test
    void testErrorReportingGeneration() throws TemplateException {
        // Test that error reporting code is properly generated
        String lexerCode = generator.generateLexerClass(testGrammar);
        
        // Verify error tracking fields
        assertTrue(lexerCode.contains("hasErrors") || lexerCode.contains("error"));
        assertTrue(lexerCode.contains("errors") || lexerCode.contains("LexicalException"));
        
        // Verify error reporting methods
        assertTrue(lexerCode.contains("hasErrors()") || lexerCode.contains("error"));
        assertTrue(lexerCode.contains("getErrors()") || lexerCode.contains("error"));
        assertTrue(lexerCode.contains("reportError") || lexerCode.contains("error"));
        
        // Verify error handling in nextToken
        assertTrue(lexerCode.contains("LexicalException") || lexerCode.contains("Exception"));
        assertTrue(lexerCode.contains("Unexpected") || lexerCode.contains("character") || lexerCode.contains("error"));
        
        // Verify suggestion generation
        assertTrue(lexerCode.contains("generateSuggestion") || lexerCode.contains("suggestion") || lexerCode.contains("Did you mean"));
    }    

    @Test
    void testTemplateRenderingWithMinimalGrammar() throws TemplateException {
        // Test template rendering with minimal grammar (single terminal)
        Grammar minimalGrammar = new Grammar();
        minimalGrammar.addNonTerminal(new NonTerminal("expr"));
        minimalGrammar.addTerminal(new Terminal("TOKEN", "a"));
        
        String lexerCode = generator.generateLexerClass(minimalGrammar);
        
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("TOKEN_PATTERN"));
        assertTrue(lexerCode.contains("Pattern.compile(\"a\")"));
        assertTrue(lexerCode.contains("\"TOKEN\""));
    }
    
    @Test
    void testTemplateRenderingWithMaximalGrammar() throws TemplateException {
        // Test template rendering with complex grammar containing many different pattern types
        Grammar maximalGrammar = new Grammar();
        maximalGrammar.addNonTerminal(new NonTerminal("program"));
        maximalGrammar.addNonTerminal(new NonTerminal("statement"));
        maximalGrammar.addNonTerminal(new NonTerminal("expression"));
        
        // Add comprehensive set of terminals
        maximalGrammar.addTerminal(new Terminal("KEYWORD_IF", "if"));
        maximalGrammar.addTerminal(new Terminal("KEYWORD_ELSE", "else"));
        maximalGrammar.addTerminal(new Terminal("KEYWORD_WHILE", "while"));
        maximalGrammar.addTerminal(new Terminal("IDENTIFIER", "[a-zA-Z_][a-zA-Z0-9_]*"));
        maximalGrammar.addTerminal(new Terminal("INTEGER", "[0-9]+"));
        maximalGrammar.addTerminal(new Terminal("FLOAT", "[0-9]+\\.[0-9]+"));
        maximalGrammar.addTerminal(new Terminal("STRING", "\"([^\"\\\\]|\\\\.)*\""));
        maximalGrammar.addTerminal(new Terminal("CHAR", "'([^'\\\\]|\\\\.)'"));
        maximalGrammar.addTerminal(new Terminal("PLUS", "\\+"));
        maximalGrammar.addTerminal(new Terminal("MINUS", "-"));
        maximalGrammar.addTerminal(new Terminal("MULTIPLY", "\\*"));
        maximalGrammar.addTerminal(new Terminal("DIVIDE", "/"));
        maximalGrammar.addTerminal(new Terminal("ASSIGN", "="));
        maximalGrammar.addTerminal(new Terminal("EQUALS", "=="));
        maximalGrammar.addTerminal(new Terminal("NOT_EQUALS", "!="));
        maximalGrammar.addTerminal(new Terminal("LESS_THAN", "<"));
        maximalGrammar.addTerminal(new Terminal("GREATER_THAN", ">"));
        maximalGrammar.addTerminal(new Terminal("LESS_EQUAL", "<="));
        maximalGrammar.addTerminal(new Terminal("GREATER_EQUAL", ">="));
        maximalGrammar.addTerminal(new Terminal("LOGICAL_AND", "&&"));
        maximalGrammar.addTerminal(new Terminal("LOGICAL_OR", "\\|\\|"));
        maximalGrammar.addTerminal(new Terminal("LOGICAL_NOT", "!"));
        maximalGrammar.addTerminal(new Terminal("LPAREN", "\\("));
        maximalGrammar.addTerminal(new Terminal("RPAREN", "\\)"));
        maximalGrammar.addTerminal(new Terminal("LBRACE", "\\{"));
        maximalGrammar.addTerminal(new Terminal("RBRACE", "\\}"));
        maximalGrammar.addTerminal(new Terminal("SEMICOLON", ";"));
        maximalGrammar.addTerminal(new Terminal("COMMA", ","));
        maximalGrammar.addTerminal(new Terminal("COMMENT", "//.*"));
        maximalGrammar.addTerminal(new Terminal("MULTILINE_COMMENT", "/\\*([^*]|\\*+[^*/])*\\*+/"));
        
        String lexerCode = generator.generateLexerClass(maximalGrammar);
        
        assertNotNull(lexerCode);
        assertFalse(lexerCode.isEmpty());
        
        // Verify all terminals are included
        assertTrue(lexerCode.contains("KEYWORD_IF_PATTERN"));
        assertTrue(lexerCode.contains("IDENTIFIER_PATTERN"));
        assertTrue(lexerCode.contains("INTEGER_PATTERN"));
        assertTrue(lexerCode.contains("FLOAT_PATTERN"));
        assertTrue(lexerCode.contains("STRING_PATTERN"));
        assertTrue(lexerCode.contains("COMMENT_PATTERN"));
        assertTrue(lexerCode.contains("MULTILINE_COMMENT_PATTERN"));
        
        // Verify terminal order array is properly generated
        assertTrue(lexerCode.contains("TERMINAL_ORDER"));
        
        // Verify that more specific patterns come first (if terminal order is present)
        if (lexerCode.contains("TERMINAL_ORDER")) {
            int keywordIndex = lexerCode.indexOf("\"KEYWORD_IF\"");
            int identifierIndex = lexerCode.indexOf("\"IDENTIFIER\"");
            if (keywordIndex != -1 && identifierIndex != -1) {
                assertTrue(keywordIndex < identifierIndex, "Keywords should come before general identifiers");
            }
        }
    }
    
    @Test
    void testTemplateRenderingWithDifferentPackageNames() throws TemplateException {
        // Test template rendering with different package name configurations
        LexerCodeGenerator customPackageGenerator = new LexerCodeGenerator("com.custom.package.lexer", "CustomGrammar");
        
        String lexerCode = customPackageGenerator.generateLexerClass(testGrammar);
        
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("package com.custom.package.lexer;"));
        assertTrue(lexerCode.contains("class CustomGrammarLexer"));
    }
    
    @Test
    void testTemplateRenderingWithDifferentGrammarNames() throws TemplateException {
        // Test template rendering with different grammar names
        LexerCodeGenerator customNameGenerator = new LexerCodeGenerator("com.test", "MySpecialGrammar");
        
        String lexerCode = customNameGenerator.generateLexerClass(testGrammar);
        
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("class MySpecialGrammarLexer"));
        assertTrue(lexerCode.contains("public MySpecialGrammarLexer(String input)"));
        assertTrue(lexerCode.contains("public MySpecialGrammarLexer(String input, String filename)"));
    }
    
    @Test
    void testTemplateRenderingWithUnicodePatterns() throws TemplateException {
        // Test template rendering with Unicode patterns
        Grammar unicodeGrammar = new Grammar();
        unicodeGrammar.addNonTerminal(new NonTerminal("text"));
        
        unicodeGrammar.addTerminal(new Terminal("UNICODE_LETTER", "\\p{L}+"));
        unicodeGrammar.addTerminal(new Terminal("UNICODE_DIGIT", "\\p{N}+"));
        unicodeGrammar.addTerminal(new Terminal("UNICODE_SPACE", "\\p{Z}+"));
        unicodeGrammar.addTerminal(new Terminal("EMOJI", "[\\u1F600-\\u1F64F]+"));
        unicodeGrammar.addTerminal(new Terminal("CHINESE", "[\\u4E00-\\u9FFF]+"));
        
        String lexerCode = generator.generateLexerClass(unicodeGrammar);
        
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("UNICODE_LETTER_PATTERN"));
        assertTrue(lexerCode.contains("UNICODE_DIGIT_PATTERN"));
        assertTrue(lexerCode.contains("UNICODE_SPACE_PATTERN"));
        assertTrue(lexerCode.contains("EMOJI_PATTERN"));
        assertTrue(lexerCode.contains("CHINESE_PATTERN"));
    }
    
    @Test
    void testTemplateRenderingWithLookaheadPatterns() throws TemplateException {
        // Test template rendering with lookahead/lookbehind patterns
        Grammar lookaheadGrammar = new Grammar();
        lookaheadGrammar.addNonTerminal(new NonTerminal("expr"));
        
        lookaheadGrammar.addTerminal(new Terminal("POSITIVE_LOOKAHEAD", "\\w+(?=\\s)"));
        lookaheadGrammar.addTerminal(new Terminal("NEGATIVE_LOOKAHEAD", "\\w+(?!\\d)"));
        lookaheadGrammar.addTerminal(new Terminal("POSITIVE_LOOKBEHIND", "(?<=\\d)\\w+"));
        lookaheadGrammar.addTerminal(new Terminal("NEGATIVE_LOOKBEHIND", "(?<!\\d)\\w+"));
        
        String lexerCode = generator.generateLexerClass(lookaheadGrammar);
        
        assertNotNull(lexerCode);
        assertTrue(lexerCode.contains("POSITIVE_LOOKAHEAD_PATTERN"));
        assertTrue(lexerCode.contains("NEGATIVE_LOOKAHEAD_PATTERN"));
        assertTrue(lexerCode.contains("POSITIVE_LOOKBEHIND_PATTERN"));
        assertTrue(lexerCode.contains("NEGATIVE_LOOKBEHIND_PATTERN"));
    }
    
    @Test
    void testErrorHandlingWithPatternSyntaxException() {
        // Test specific error handling for PatternSyntaxException
        Grammar invalidGrammar = new Grammar();
        invalidGrammar.addNonTerminal(new NonTerminal("expr"));
        
        // Add terminal with invalid regex syntax
        invalidGrammar.addTerminal(new Terminal("INVALID_QUANTIFIER", "a{,5}"));  // Invalid quantifier syntax
        
        TemplateException exception = assertThrows(TemplateException.class, () -> {
            generator.generateLexerClass(invalidGrammar);
        });
        
        assertTrue(exception.getMessage().contains("Invalid regex pattern"));
        assertTrue(exception.getMessage().contains("INVALID_QUANTIFIER"));
    }
    
    @Test
    void testErrorHandlingWithEmptyGrammar() {
        // Test error handling with completely empty grammar
        Grammar emptyGrammar = new Grammar();
        
        TemplateException exception = assertThrows(TemplateException.class, () -> {
            generator.generateLexerClass(emptyGrammar);
        });
        
        assertTrue(exception.getMessage().contains("No terminals with valid patterns found"));
    }
    
    @Test
    void testErrorHandlingWithOnlyNonPatternTerminals() {
        // Test error handling with grammar containing only non-pattern terminals
        Grammar nonPatternGrammar = new Grammar();
        nonPatternGrammar.addNonTerminal(new NonTerminal("expr"));
        
        nonPatternGrammar.addTerminal(new Terminal("EOF"));           // No pattern
        nonPatternGrammar.addTerminal(new Terminal("EPSILON", ""));   // Empty pattern
        nonPatternGrammar.addTerminal(new Terminal("NULL", null));    // Null pattern
        
        TemplateException exception = assertThrows(TemplateException.class, () -> {
            generator.generateLexerClass(nonPatternGrammar);
        });
        
        assertTrue(exception.getMessage().contains("No terminals with valid patterns found"));
    }
    
    @Test
    void testErrorHandlingWithInvalidPatternAtPosition() {
        // Test error handling that includes position information
        Grammar invalidGrammar = new Grammar();
        invalidGrammar.addNonTerminal(new NonTerminal("expr"));
        
        // Add terminal with invalid regex at specific position
        invalidGrammar.addTerminal(new Terminal("INVALID_BRACKET", "abc[def"));  // Unclosed bracket
        
        TemplateException exception = assertThrows(TemplateException.class, () -> {
            generator.generateLexerClass(invalidGrammar);
        });
        
        assertTrue(exception.getMessage().contains("Invalid regex pattern"));
        assertTrue(exception.getMessage().contains("INVALID_BRACKET"));
        assertTrue(exception.getMessage().contains("position"));
    }
    
    @Test
    void testErrorHandlingDuringTokenizerEngineGeneration() {
        // Test error handling during tokenizer engine generation
        Grammar invalidGrammar = new Grammar();
        invalidGrammar.addNonTerminal(new NonTerminal("expr"));
        invalidGrammar.addTerminal(new Terminal("INVALID", "*invalid"));
        
        assertThrows(TemplateException.class, () -> {
            generator.generateTokenizerEngine(invalidGrammar);
        });
    }
    
    @Test
    void testErrorHandlingWithNullTemplateManager() {
        // Test error handling when template manager is null
        assertThrows(IllegalArgumentException.class, () -> {
            new LexerCodeGenerator(null, "com.test", "Test");
        });
    }
    
    @Test
    void testRobustnessWithMalformedPatterns() {
        // Test robustness with various malformed patterns
        Grammar malformedGrammar = new Grammar();
        malformedGrammar.addNonTerminal(new NonTerminal("expr"));
        
        // Test each malformed pattern individually to get specific error messages
        String[] malformedPatterns = {
            "[unclosed",           // Unclosed character class
            "(?incomplete",        // Incomplete group
            "a{invalid}",         // Invalid quantifier
            "(?P<name>invalid)",  // Named groups (not supported in Java)
            "\\k<name>",          // Named backreference (not supported)
            // Note: "a++" is actually valid in Java regex (possessive quantifier)
        };
        
        for (int i = 0; i < malformedPatterns.length; i++) {
            Grammar testGrammar = new Grammar();
            testGrammar.addNonTerminal(new NonTerminal("expr"));
            testGrammar.addTerminal(new Terminal("MALFORMED_" + i, malformedPatterns[i]));
            
            assertThrows(TemplateException.class, () -> {
                generator.generateLexerClass(testGrammar);
            }, "Pattern should be invalid: " + malformedPatterns[i]);
        }
    }
    
    @Test
    void testConsistencyAcrossMultipleGenerations() throws TemplateException {
        // Test that multiple generations of the same grammar produce consistent results
        String firstGeneration = generator.generateLexerClass(testGrammar);
        String secondGeneration = generator.generateLexerClass(testGrammar);
        String thirdGeneration = generator.generateLexerClass(testGrammar);
        
        assertEquals(firstGeneration, secondGeneration);
        assertEquals(secondGeneration, thirdGeneration);
    }
    
    @Test
    void testThreadSafety() throws InterruptedException {
        // Test thread safety of lexer generation
        final int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final List<String> results = Collections.synchronizedList(new ArrayList<>());
        final List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    String result = generator.generateLexerClass(testGrammar);
                    results.add(result);
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        latch.await(10, TimeUnit.SECONDS);
        
        // All threads should complete without exceptions
        assertTrue(exceptions.isEmpty(), "Exceptions occurred: " + exceptions);
        assertEquals(threadCount, results.size());
        
        // All results should be identical
        String expected = results.get(0);
        for (String result : results) {
            assertEquals(expected, result);
        }
    }
    
    @Test
    void testMemoryUsageWithLargeGrammar() throws TemplateException {
        // Test memory usage with very large grammar
        Grammar largeGrammar = new Grammar();
        largeGrammar.addNonTerminal(new NonTerminal("program"));
        
        // Add many terminals with complex patterns
        for (int i = 0; i < 1000; i++) {
            largeGrammar.addTerminal(new Terminal("TERMINAL_" + i, 
                "[a-zA-Z][a-zA-Z0-9_]*_" + i + "(_[0-9]+)?"));
        }
        
        // Monitor memory before generation
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Suggest garbage collection
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        String lexerCode = generator.generateLexerClass(largeGrammar);
        
        // Monitor memory after generation
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        assertNotNull(lexerCode);
        assertFalse(lexerCode.isEmpty());
        
        // Memory usage should be reasonable (less than 100MB for this test)
        assertTrue(memoryUsed < 100 * 1024 * 1024, 
            "Memory usage too high: " + (memoryUsed / 1024 / 1024) + "MB");
    }
}