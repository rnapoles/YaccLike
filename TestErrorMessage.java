import java.util.*;
import com.parsergenerator.parser.*;
import com.parsergenerator.lexer.*;

public class TestErrorMessage {
    public static void main(String[] args) {
        try {
            // Test case from GrammarParserErrorHandlingTest.testErrorPositionAccuracy
            String input = "Start = <expr>\n" +
                          "<expr> ::= <term>\n" +
                          "<term> = TERM";  // Error on line 3
            
            System.out.println("Input: " + input);
            
            GrammarLexer lexer = new GrammarLexer(input);
            List<Token> tokens = lexer.tokenize();
            
            System.out.println("Tokens:");
            for (Token token : tokens) {
                System.out.println("  " + token.getType() + ": '" + token.getValue() + "' at " + token.getPosition());
            }
            
            GrammarParser parser = new GrammarParser(tokens);
            parser.parse();
        } catch (Exception e) {
            System.out.println("Exception type: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
            if (e instanceof ParseException) {
                ParseException pe = (ParseException) e;
                System.out.println("Position: " + pe.getPosition());
                if (pe.getPosition() != null) {
                    System.out.println("Line: " + pe.getPosition().getLine());
                }
            }
            System.out.println("Number of errors: " + (e.getMessage().contains("Multiple parse errors") ? "Multiple" : "Single"));
        }
    }
}