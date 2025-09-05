import com.parsergenerator.analysis.AnalysisResult;
import com.parsergenerator.analysis.GrammarAnalyzer;
import com.parsergenerator.error.ErrorReporter;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.lexer.GrammarLexer;
import com.parsergenerator.lexer.Token;
import com.parsergenerator.parser.GrammarParser;
import com.parsergenerator.parser.ParseException;

import java.util.List;

public class debug_test3 {
    public static void main(String[] args) {
        // Exact replication of the failing test
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
        
        GrammarParser parser = new GrammarParser(tokens, errorReporter);
        
        try {
            Grammar grammar = parser.parse();
            GrammarAnalyzer analyzer = new GrammarAnalyzer(grammar, errorReporter);
            AnalysisResult result = analyzer.analyze();
            
            // Should have no errors
            System.out.println("errorReporter.hasErrors(): " + errorReporter.hasErrors());
            System.out.println("result.hasErrors(): " + result.hasErrors());
            
            // Should have informational messages about successful processing
            int infoCount = errorReporter.getErrors(ErrorReporter.Severity.INFO).size();
            System.out.println("INFO messages count: " + infoCount);
            System.out.println("Test condition (infoCount > 0): " + (infoCount > 0));
            
            String summary = errorReporter.generateSummary();
            System.out.println("Summary contains 'No errors': " + summary.contains("No errors"));
            System.out.println("Summary: " + summary);
            
        } catch (ParseException e) {
            System.out.println("ParseException: " + e.getMessage());
        }
    }
}