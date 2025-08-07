package com.parsergenerator.parser;

import com.parsergenerator.lexer.GrammarLexer;
import com.parsergenerator.lexer.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to isolate error handling issues.
 */
public class SimpleErrorTest {
    
    @Test
    void testSimpleValidGrammar() {
        String input = "Start = <expr>\n<expr> ::= TERM";
        
        GrammarLexer lexer = new GrammarLexer(input);
        List<Token> tokens = lexer.tokenize();
        GrammarParser parser = new GrammarParser(tokens);
        
        assertDoesNotThrow(() -> {
            parser.parse();
        });
    }
    
    @Test
    void testSimpleError() {
        String input = "TERM ::= {pattern}";  // Wrong operator
        
        GrammarLexer lexer = new GrammarLexer(input);
        List<Token> tokens = lexer.tokenize();
        GrammarParser parser = new GrammarParser(tokens);
        
        assertThrows(ParseException.class, () -> {
            parser.parse();
        });
    }
}