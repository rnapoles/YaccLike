package com.parsergenerator.integration;

import com.parsergenerator.ParserGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System integration tests covering the entire grammar-to-parser pipeline.
 * Tests various grammar types and end-to-end functionality.
 */
public class SystemIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void testArithmeticGrammarPipeline() throws IOException {
        String arithmeticGrammar = """
            -- Arithmetic expression grammar
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
        
        Path grammarFile = tempDir.resolve("arithmetic.grammar");
        Files.writeString(grammarFile, arithmeticGrammar);
        
        ParserGenerator generator = new ParserGenerator();
        boolean success = generator.generateParser(grammarFile.toString());
        
        assertTrue(success, "Arithmetic grammar should generate successfully");
    }
    
    @Test
    void testSimpleGrammarPipeline() throws IOException {
        String simpleGrammar = """
            ID = {[a-zA-Z][a-zA-Z0-9]*}
            SEMICOLON = {;}
            
            Start = <Statement>
            
            <Statement> ::= ID SEMICOLON
            """;
        
        Path grammarFile = tempDir.resolve("simple.grammar");
        Files.writeString(grammarFile, simpleGrammar);
        
        ParserGenerator generator = new ParserGenerator();
        boolean success = generator.generateParser(grammarFile.toString());
        
        assertTrue(success, "Simple grammar should generate successfully");
    }
    
    @Test
    void testComplexGrammarPipeline() throws IOException {
        String complexGrammar = """
            -- Complex grammar with multiple non-terminals
            IF = {if}
            THEN = {then}
            ELSE = {else}
            ID = {[a-zA-Z][a-zA-Z0-9]*}
            NUM = {[0-9]+}
            ASSIGN = {=}
            SEMICOLON = {;}
            LPAREN = {\\(}
            RPAREN = {\\)}
            PLUS = {\\+}
            
            Start = <Program>
            
            <Program> ::= <Statement> | <Statement> <Program>
            <Statement> ::= <Assignment> | <IfStatement>
            <Assignment> ::= ID ASSIGN <Expression> SEMICOLON
            <IfStatement> ::= IF LPAREN <Expression> RPAREN THEN <Statement> | IF LPAREN <Expression> RPAREN THEN <Statement> ELSE <Statement>
            <Expression> ::= <Term> PLUS <Expression> | <Term>
            <Term> ::= ID | NUM | LPAREN <Expression> RPAREN
            """;
        
        Path grammarFile = tempDir.resolve("complex.grammar");
        Files.writeString(grammarFile, complexGrammar);
        
        ParserGenerator generator = new ParserGenerator();
        boolean success = generator.generateParser(grammarFile.toString());
        
        assertTrue(success, "Complex grammar should generate successfully");
    }
    
    @Test
    void testInvalidGrammarHandling() throws IOException {
        String invalidGrammar = """
            -- Grammar with errors
            Invalid@Symbol = {pattern}  -- Invalid character in terminal name
            
            Start = <Expr>
            
            <Expr> ::= <UndefinedSymbol>  -- Undefined symbol
            """;
        
        Path grammarFile = tempDir.resolve("invalid.grammar");
        Files.writeString(grammarFile, invalidGrammar);
        
        ParserGenerator generator = new ParserGenerator();
        boolean success = generator.generateParser(grammarFile.toString());
        
        assertFalse(success, "Invalid grammar should fail gracefully");
    }
    
    @Test
    void testEmptyGrammarHandling() throws IOException {
        String emptyGrammar = "";
        
        Path grammarFile = tempDir.resolve("empty.grammar");
        Files.writeString(grammarFile, emptyGrammar);
        
        ParserGenerator generator = new ParserGenerator();
        boolean success = generator.generateParser(grammarFile.toString());
        
        assertFalse(success, "Empty grammar should fail gracefully");
    }
    
    @Test
    void testNonExistentFileHandling() {
        Path nonExistentFile = tempDir.resolve("nonexistent.grammar");
        
        ParserGenerator generator = new ParserGenerator();
        boolean success = generator.generateParser(nonExistentFile.toString());
        
        assertFalse(success, "Non-existent file should fail gracefully");
    }
    
    @Test
    void testMultipleGrammarProcessing() throws IOException {
        String[] grammars = {
            "TERM1 = {term1}\nStart = <A>\n<A> ::= TERM1",
            "TERM2 = {term2}\nStart = <B>\n<B> ::= TERM2",
            "TERM3 = {term3}\nStart = <C>\n<C> ::= TERM3"
        };
        
        ParserGenerator generator = new ParserGenerator();
        
        for (int i = 0; i < grammars.length; i++) {
            Path grammarFile = tempDir.resolve("grammar" + i + ".grammar");
            Files.writeString(grammarFile, grammars[i]);
            
            boolean success = generator.generateParser(grammarFile.toString());
            assertTrue(success, "Grammar " + i + " should process successfully");
        }
    }
    
    @Test
    void testPerformanceWithLargeGrammar() throws IOException {
        // Generate a larger grammar programmatically
        StringBuilder largeGrammar = new StringBuilder();
        
        // Add many terminals
        for (int i = 0; i < 20; i++) {
            largeGrammar.append("TERM").append(i).append(" = {term").append(i).append("}\n");
        }
        
        largeGrammar.append("\nStart = <Root>\n\n");
        
        // Add many non-terminals and productions
        largeGrammar.append("<Root> ::= <Level1>\n");
        for (int i = 1; i < 10; i++) {
            largeGrammar.append("<Level").append(i).append("> ::= ");
            for (int j = 0; j < 3; j++) {
                if (j > 0) largeGrammar.append(" | ");
                largeGrammar.append("TERM").append(j).append(" <Level").append(i + 1).append(">");
            }
            largeGrammar.append("\n");
        }
        largeGrammar.append("<Level10> ::= TERM0 | TERM1 | TERM2\n");
        
        Path grammarFile = tempDir.resolve("large.grammar");
        Files.writeString(grammarFile, largeGrammar.toString());
        
        long startTime = System.currentTimeMillis();
        
        ParserGenerator generator = new ParserGenerator();
        boolean success = generator.generateParser(grammarFile.toString());
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(success, "Large grammar should process successfully");
        assertTrue(duration < 30000, "Large grammar should process within 30 seconds, took: " + duration + "ms");
    }
    
    @Test
    void testMemoryUsageWithRepeatedProcessing() throws IOException {
        String baseGrammar = """
            ID = {[a-zA-Z]+}
            Start = <S>
            <S> ::= ID
            """;
        
        Path grammarFile = tempDir.resolve("memory_test.grammar");
        Files.writeString(grammarFile, baseGrammar);
        
        ParserGenerator generator = new ParserGenerator();
        
        // Process the same grammar multiple times to test for memory leaks
        for (int i = 0; i < 10; i++) {
            boolean success = generator.generateParser(grammarFile.toString());
            assertTrue(success, "Memory test iteration " + i + " should succeed");
        }
        
        // If we get here without OutOfMemoryError, the test passes
        assertTrue(true, "Memory usage test completed successfully");
    }
    
    @Test
    void testRegressionArithmeticGrammar() throws IOException {
        // Test the specific arithmetic grammar from requirements
        String arithmeticGrammar = """
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
        
        Path grammarFile = tempDir.resolve("regression_arithmetic.grammar");
        Files.writeString(grammarFile, arithmeticGrammar);
        
        ParserGenerator generator = new ParserGenerator();
        boolean success = generator.generateParser(grammarFile.toString());
        
        assertTrue(success, "Regression arithmetic grammar should always work");
    }
}