import com.parsergenerator.analysis.AnalysisResult;
import com.parsergenerator.analysis.GrammarAnalyzer;
import com.parsergenerator.error.ErrorReporter;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.lexer.GrammarLexer;
import com.parsergenerator.lexer.Token;
import com.parsergenerator.parser.GrammarParser;
import com.parsergenerator.parser.ParseException;

import java.util.List;

public class debug_test {
    public static void main(String[] args) {
        ErrorReporter errorReporter = new ErrorReporter();
        
        String validGrammar = "PLUS = {\\+}\n" +
                            "MULT = {\\*}\n" +
                            "NUM = {[0-9]+}\n" +
                            "LPAREN = {\\(}\n" +
                            "RPAREN = {\\)}\n" +
                            "Start = <expr>\n" +
                            "<expr> ::= <term> PLUS <expr> | <term>\n" +
                            "<term> ::= <factor> MULT <term> | <factor>\n" +
                            "<factor> ::= NUM | LPAREN <expr> RPAREN";
        
        GrammarLexer lexer = new GrammarLexer(validGrammar, "test.grammar", errorReporter);
        List<Token> tokens = lexer.tokenize();
        
        System.out.println("After lexing:");
        System.out.println("Has errors: " + errorReporter.hasErrors());
        System.out.println("Total errors: " + errorReporter.getErrorCount());
        System.out.println("INFO messages: " + errorReporter.getErrors(ErrorReporter.Severity.INFO).size());
        
        GrammarParser parser = new GrammarParser(tokens, errorReporter);
        
        try {
            Grammar grammar = parser.parse();
            
            System.out.println("After parsing:");
            System.out.println("Has errors: " + errorReporter.hasErrors());
            System.out.println("Total errors: " + errorReporter.getErrorCount());
            System.out.println("INFO messages: " + errorReporter.getErrors(ErrorReporter.Severity.INFO).size());
            
            GrammarAnalyzer analyzer = new GrammarAnalyzer(grammar, errorReporter);
            AnalysisResult result = analyzer.analyze();
            
            System.out.println("After analysis:");
            System.out.println("Has errors: " + errorReporter.hasErrors());
            System.out.println("Result has errors: " + result.hasErrors());
            System.out.println("Total errors: " + errorReporter.getErrorCount());
            System.out.println("INFO messages: " + errorReporter.getErrors(ErrorReporter.Severity.INFO).size());
            
            // Print all INFO messages
            System.out.println("INFO messages:");
            for (var error : errorReporter.getErrors(ErrorReporter.Severity.INFO)) {
                System.out.println("  - " + error.getMessage());
            }
            
        } catch (ParseException e) {
            System.out.println("Parse exception: " + e.getMessage());
        }
    }
}