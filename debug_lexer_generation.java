import com.parsergenerator.codegen.LexerCodeGenerator;
import com.parsergenerator.codegen.GeneratedCode;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.symbol.Terminal;
import com.parsergenerator.symbol.NonTerminal;

public class debug_lexer_generation {
    public static void main(String[] args) {
        try {
            // Create a simple test grammar
            Grammar testGrammar = new Grammar();
            testGrammar.addNonTerminal(new NonTerminal("expr"));
            testGrammar.addTerminal(new Terminal("PLUS", "\\+"));
            testGrammar.addTerminal(new Terminal("NUMBER", "[0-9]+"));
            
            // Create lexer generator
            LexerCodeGenerator generator = new LexerCodeGenerator("com.test", "TestGrammar");
            
            // Generate lexer
            GeneratedCode result = generator.generateLexer(testGrammar);
            
            System.out.println("Generated files: " + result.getFileCount());
            for (String filename : result.getFileNames()) {
                System.out.println("File: " + filename);
                System.out.println("Content preview (first 500 chars):");
                String content = result.getFileContent(filename);
                System.out.println(content.substring(0, Math.min(500, content.length())));
                System.out.println("...");
                System.out.println();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}