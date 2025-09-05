package com.parsergenerator.codegen;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for pattern validation and compilation in lexer generation.
 */
class PatternValidationTest {
    
    private LexerCodeGenerator generator;
    private Grammar grammar;
    
    @BeforeEach
    void setUp() {
        generator = new LexerCodeGenerator("com.test", "TestGrammar");
        grammar = new Grammar();
    }
    
    @Test
    void testValidPatternCompilation() throws TemplateException {
        // Add terminals with valid patterns
        grammar.addTerminal(new Terminal("NUMBER", "[0-9]+"));
        grammar.addTerminal(new Terminal("IDENTIFIER", "[a-zA-Z][a-zA-Z0-9]*"));
        grammar.addTerminal(new Terminal("PLUS", "\\+"));
        
        // Should not throw exception
        GeneratedCode result = generator.generateLexer(grammar);
        assertNotNull(result);
        assertTrue(result.hasFile("TestGrammarLexer.java"));
    }
    
    @Test
    void testInvalidPatternThrowsException() {
        // Add terminal with invalid regex pattern
        grammar.addTerminal(new Terminal("INVALID", "[unclosed"));
        
        // Should throw TemplateException due to invalid pattern
        assertThrows(TemplateException.class, () -> {
            generator.generateLexer(grammar);
        });
    }
    
    @Test
    void testEmptyPatternThrowsException() {
        // Add terminal with empty pattern
        grammar.addTerminal(new Terminal("EMPTY", ""));
        
        // Should throw TemplateException due to empty pattern
        assertThrows(TemplateException.class, () -> {
            generator.generateLexer(grammar);
        });
    }
    
    @Test
    void testNullPatternThrowsException() {
        // Add terminal with null pattern
        grammar.addTerminal(new Terminal("NULL", null));
        
        // Should throw TemplateException due to null pattern
        assertThrows(TemplateException.class, () -> {
            generator.generateLexer(grammar);
        });
    }
    
    @Test
    void testTerminalInfoSpecificityCalculation() {
        // Test specificity calculation for different pattern types
        LexerCodeGenerator.TerminalInfo literal = 
                new LexerCodeGenerator.TerminalInfo("LITERAL", "hello", "LITERAL_PATTERN", "LITERAL");
        LexerCodeGenerator.TerminalInfo charClass = 
                new LexerCodeGenerator.TerminalInfo("CHARCLASS", "[a-z]+", "CHARCLASS_PATTERN", "CHARCLASS");
        LexerCodeGenerator.TerminalInfo wildcard = 
                new LexerCodeGenerator.TerminalInfo("WILDCARD", ".*", "WILDCARD_PATTERN", "WILDCARD");
        
        // Literal should be more specific than character class
        assertTrue(literal.getSpecificity() > charClass.getSpecificity());
        
        // Character class should be more specific than wildcard
        assertTrue(charClass.getSpecificity() > wildcard.getSpecificity());
    }
    
    @Test
    void testComplexPatternValidation() throws TemplateException {
        // Test various complex but valid patterns
        grammar.addTerminal(new Terminal("STRING", "\"([^\"\\\\]|\\\\.)*\""));
        grammar.addTerminal(new Terminal("COMMENT", "//.*"));
        grammar.addTerminal(new Terminal("FLOAT", "[0-9]+\\.[0-9]+"));
        grammar.addTerminal(new Terminal("HEX", "0[xX][0-9a-fA-F]+"));
        
        // Should not throw exception
        GeneratedCode result = generator.generateLexer(grammar);
        assertNotNull(result);
    }
    
    @Test
    void testPatternWithQuantifiers() throws TemplateException {
        // Test patterns with various quantifiers
        grammar.addTerminal(new Terminal("OPTIONAL", "a?"));
        grammar.addTerminal(new Terminal("ZERO_OR_MORE", "a*"));
        grammar.addTerminal(new Terminal("ONE_OR_MORE", "a+"));
        grammar.addTerminal(new Terminal("RANGE", "a{2,5}"));
        
        // Should not throw exception
        GeneratedCode result = generator.generateLexer(grammar);
        assertNotNull(result);
    }
    
    @Test
    void testPatternWithAlternation() throws TemplateException {
        // Test patterns with alternation
        grammar.addTerminal(new Terminal("KEYWORD", "(if|else|while|for)"));
        grammar.addTerminal(new Terminal("OPERATOR", "(\\+|\\-|\\*|/)"));
        
        // Should not throw exception
        GeneratedCode result = generator.generateLexer(grammar);
        assertNotNull(result);
    }
    
    @Test
    void testTerminalOrderingBySpecificity() throws TemplateException {
        // Add terminals in reverse specificity order
        grammar.addTerminal(new Terminal("WILDCARD", ".*"));           // Least specific
        grammar.addTerminal(new Terminal("CHARCLASS", "[a-z]+"));      // Medium specific
        grammar.addTerminal(new Terminal("KEYWORD", "if"));            // Most specific
        
        String lexerCode = generator.generateLexerClass(grammar);
        
        // Verify that the generated code has terminals in correct order
        // More specific patterns should appear first in TERMINAL_ORDER
        int keywordIndex = lexerCode.indexOf("\"KEYWORD\"");
        int charclassIndex = lexerCode.indexOf("\"CHARCLASS\"");
        int wildcardIndex = lexerCode.indexOf("\"WILDCARD\"");
        
        assertTrue(keywordIndex < charclassIndex);
        assertTrue(charclassIndex < wildcardIndex);
    }
}