import com.parsergenerator.codegen.LexerCodeGenerator;
import com.parsergenerator.codegen.GeneratedCode;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.symbol.Terminal;

public class debug_lexer_generation {
    public static void main(String[] args) {
        try {
            // Create a simple test grammar
            Grammar testGrammar = new Grammar();
            
            // Add terminals with patterns
            Terminal number = new Terminal("NUMBER", "[0-9]+");
            testGrammar.addTerminal(number);
            
            Terminal plus = new Terminal("PLUS", "\\+");
            testGrammar.addTerminal(plus);
            
            // Create lexer generator
            LexerCodeGenerator generator = new LexerCodeGenerator("com.test.lexer", "TestGrammar");
            
            // Generate lexer code
            GeneratedCode result = generator.generateLexer(testGrammar);
            
            System.out.println("Generated files: " + result.getFileCount());
            for (String filename : result.getFileNames()) {
                System.out.println("File: " + filename);
                System.out.println("Content preview (first 500 chars):");
                String content = result.getFileContent(filename);
                System.out.println(content.substring(0, Math.min(500, content.length())));
                System.out.println("...\n");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}