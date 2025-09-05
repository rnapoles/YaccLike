import com.parsergenerator.codegen.LexerCodeGenerator;
import com.parsergenerator.codegen.GeneratedCode;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;

public class DemoLexerGeneration {
    public static void main(String[] args) {
        try {
            // Create a simple grammar for demonstration
            Grammar grammar = new Grammar();
            
            // Add non-terminals
            grammar.addNonTerminal(new NonTerminal("expr"));
            grammar.addNonTerminal(new NonTerminal("term"));
            
            // Add terminals with patterns
            grammar.addTerminal(new Terminal("NUMBER", "[0-9]+"));
            grammar.addTerminal(new Terminal("PLUS", "\\+"));
            grammar.addTerminal(new Terminal("MINUS", "-"));
            grammar.addTerminal(new Terminal("MULTIPLY", "\\*"));
            grammar.addTerminal(new Terminal("LPAREN", "\\("));
            grammar.addTerminal(new Terminal("RPAREN", "\\)"));
            
            // Create lexer generator
            LexerCodeGenerator generator = new LexerCodeGenerator("com.demo.lexer", "ArithmeticGrammar");
            
            // Generate lexer code
            GeneratedCode result = generator.generateLexer(grammar);
            
            System.out.println("Generated " + result.getFileCount() + " files:");
            for (String fileName : result.getFileNames()) {
                System.out.println("- " + fileName);
            }
            
            // Show the generated lexer class
            System.out.println("\n=== Generated ArithmeticGrammarLexer.java ===");
            System.out.println(result.getFileContent("ArithmeticGrammarLexer.java"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}