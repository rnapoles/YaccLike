package com.parsergenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for generating and using a calculator parser with visitor pattern.
 */
public class CalculatorExampleTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void testCalculatorGrammarGeneration() throws IOException {
        String calculatorGrammar = """
            -- Calculator Grammar
            -- Supports arithmetic operations, functions, variables, and parentheses
            
            -- Literals and identifiers
            NUMBER = {[0-9]+(\\.[0-9]+)?}
            IDENTIFIER = {[a-zA-Z][a-zA-Z0-9]*}
            
            -- Arithmetic operators (in precedence order)
            PLUS = {\\+}
            MINUS = {\\-}
            MULTIPLY = {\\*}
            DIVIDE = {/}
            POWER = {\\^}
            
            -- Punctuation
            LPAREN = {\\(}
            RPAREN = {\\)}
            COMMA = {,}
            ASSIGN = {=}
            SEMICOLON = {;}
            
            Start = <Program>
            
            -- Program can be a sequence of statements
            <Program> ::= <Statement> | <Statement> <Program>
            
            -- Statements can be assignments or expressions
            <Statement> ::= <Assignment> | <Expression> SEMICOLON
            
            -- Variable assignment
            <Assignment> ::= IDENTIFIER ASSIGN <Expression> SEMICOLON
            
            -- Expression with proper operator precedence
            <Expression> ::= <Term> PLUS <Expression> | <Term> MINUS <Expression> | <Term>
            <Term> ::= <Factor> MULTIPLY <Term> | <Factor> DIVIDE <Term> | <Factor>
            <Factor> ::= <Base> POWER <Factor> | <Base>
            <Base> ::= NUMBER | IDENTIFIER | <FunctionCall> | LPAREN <Expression> RPAREN | MINUS <Base>
            
            -- Function calls with arguments
            <FunctionCall> ::= IDENTIFIER LPAREN <ArgumentList> RPAREN
            <ArgumentList> ::= <Expression> | <Expression> COMMA <ArgumentList> | 
            """;
        
        Path grammarFile = tempDir.resolve("calculator.grammar");
        Files.writeString(grammarFile, calculatorGrammar);
        
        ParserGenerator generator = new ParserGenerator();
        boolean success = generator.generateParser(grammarFile.toString());
        
        assertTrue(success, "Calculator grammar should generate successfully");
        
        // Verify that the generated files exist in the default location
        Path generatedDir = Path.of("generated");
        assertTrue(Files.exists(generatedDir), "Generated directory should exist");
        
        System.out.println("Calculator parser generated successfully!");
        System.out.println("Generated files are in: " + generatedDir.toAbsolutePath());
    }
}