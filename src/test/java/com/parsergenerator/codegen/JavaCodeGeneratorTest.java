package com.parsergenerator.codegen;

import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.grammar.Production;
import com.parsergenerator.lalr.ParsingTable;
import com.parsergenerator.lalr.Action;
import com.parsergenerator.lalr.ActionType;
import com.parsergenerator.symbol.Terminal;
import com.parsergenerator.symbol.NonTerminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JavaCodeGenerator.
 * Tests the generation of complete Java parser code from grammar and parsing table.
 */
class JavaCodeGeneratorTest {
    
    private JavaCodeGenerator generator;
    private Grammar testGrammar;
    private ParsingTable testParsingTable;
    
    @BeforeEach
    void setUp() {
        generator = new JavaCodeGenerator("com.test.parser", "TestGrammar");
        setupTestGrammar();
        setupTestParsingTable();
    }
    
    private void setupTestGrammar() {
        testGrammar = new Grammar();
        
        // Create terminals
        Terminal plus = new Terminal("+");
        Terminal mult = new Terminal("*");
        Terminal lparen = new Terminal("(");
        Terminal rparen = new Terminal(")");
        Terminal id = new Terminal("ID");
        
        // Create non-terminals
        NonTerminal expr = new NonTerminal("<expr>");
        NonTerminal term = new NonTerminal("<term>");
        NonTerminal factor = new NonTerminal("<factor>");
        
        // Add symbols to grammar
        testGrammar.addTerminal(plus);
        testGrammar.addTerminal(mult);
        testGrammar.addTerminal(lparen);
        testGrammar.addTerminal(rparen);
        testGrammar.addTerminal(id);
        
        testGrammar.addNonTerminal(expr);
        testGrammar.addNonTerminal(term);
        testGrammar.addNonTerminal(factor);
        
        // Create productions
        // <expr> ::= <expr> + <term>
        Production p1 = new Production(expr, Arrays.asList(expr, plus, term));
        // <expr> ::= <term>
        Production p2 = new Production(expr, Arrays.asList(term));
        // <term> ::= <term> * <factor>
        Production p3 = new Production(term, Arrays.asList(term, mult, factor));
        // <term> ::= <factor>
        Production p4 = new Production(term, Arrays.asList(factor));
        // <factor> ::= ( <expr> )
        Production p5 = new Production(factor, Arrays.asList(lparen, expr, rparen));
        // <factor> ::= ID
        Production p6 = new Production(factor, Arrays.asList(id));
        
        testGrammar.addProduction(p1);
        testGrammar.addProduction(p2);
        testGrammar.addProduction(p3);
        testGrammar.addProduction(p4);
        testGrammar.addProduction(p5);
        testGrammar.addProduction(p6);
        
        testGrammar.setStartSymbol(expr);
    }
    
    private void setupTestParsingTable() {
        testParsingTable = new ParsingTable();
        
        // Add some basic actions for testing
        Terminal plus = testGrammar.getTerminals().stream()
                .filter(t -> t.getName().equals("+"))
                .findFirst().orElse(null);
        Terminal id = testGrammar.getTerminals().stream()
                .filter(t -> t.getName().equals("ID"))
                .findFirst().orElse(null);
        
        if (plus != null && id != null) {
            testParsingTable.setAction(0, id, Action.shift(1));
            // Get the last production (ID production)
            Production idProduction = testGrammar.getProductions().stream()
                    .filter(p -> p.getRightSide().size() == 1 && p.getRightSide().get(0).equals(id))
                    .findFirst().orElse(null);
            if (idProduction != null) {
                testParsingTable.setAction(1, plus, Action.reduce(idProduction));
            }
        }
    }
    
    @Test
    @DisplayName("Constructor should initialize with valid parameters")
    void testConstructorWithValidParameters() {
        JavaCodeGenerator gen = new JavaCodeGenerator("com.example", "MyGrammar");
        assertEquals("com.example", gen.getPackageName());
        assertEquals("MyGrammar", gen.getGrammarName());
        assertNotNull(gen.getTemplateManager());
    }
    
    @Test
    @DisplayName("Constructor should use default values")
    void testDefaultConstructor() {
        JavaCodeGenerator gen = new JavaCodeGenerator();
        assertEquals("com.generated.parser", gen.getPackageName());
        assertEquals("GeneratedGrammar", gen.getGrammarName());
        assertNotNull(gen.getTemplateManager());
    }
    
    @Test
    @DisplayName("Constructor should reject null package name")
    void testConstructorWithNullPackageName() {
        assertThrows(IllegalArgumentException.class, () -> 
            new JavaCodeGenerator(null, "TestGrammar"));
    }
    
    @Test
    @DisplayName("Constructor should reject empty package name")
    void testConstructorWithEmptyPackageName() {
        assertThrows(IllegalArgumentException.class, () -> 
            new JavaCodeGenerator("", "TestGrammar"));
    }
    
    @Test
    @DisplayName("Constructor should reject null grammar name")
    void testConstructorWithNullGrammarName() {
        assertThrows(IllegalArgumentException.class, () -> 
            new JavaCodeGenerator("com.test", null));
    }
    
    @Test
    @DisplayName("Constructor should reject empty grammar name")
    void testConstructorWithEmptyGrammarName() {
        assertThrows(IllegalArgumentException.class, () -> 
            new JavaCodeGenerator("com.test", ""));
    }
    
    @Test
    @DisplayName("Generate should reject null grammar")
    void testGenerateWithNullGrammar() {
        assertThrows(IllegalArgumentException.class, () -> 
            generator.generate(null, testParsingTable));
    }
    
    @Test
    @DisplayName("Generate should reject null parsing table")
    void testGenerateWithNullParsingTable() {
        assertThrows(IllegalArgumentException.class, () -> 
            generator.generate(testGrammar, null));
    }
    
    @Test
    @DisplayName("Generate should produce complete code set")
    void testGenerateProducesCompleteCodeSet() throws TemplateException {
        GeneratedCode result = generator.generate(testGrammar, testParsingTable);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Check that all expected files are generated
        assertTrue(result.hasFile("TestGrammarParser.java"));
        assertTrue(result.hasFile("TokenType.java"));
        assertTrue(result.hasFile("Token.java"));
        assertTrue(result.hasFile("ASTNode.java"));
        assertTrue(result.hasFile("TerminalNode.java"));
        assertTrue(result.hasFile("ASTVisitor.java"));
        assertTrue(result.hasFile("ParseException.java"));
        
        // Check that AST node classes are generated for non-terminals
        assertTrue(result.hasFile("ExprNode.java"));
        assertTrue(result.hasFile("TermNode.java"));
        assertTrue(result.hasFile("FactorNode.java"));
    }
    
    @Test
    @DisplayName("Generated parser class should contain package declaration")
    void testGeneratedParserClassPackage() throws TemplateException {
        GeneratedCode result = generator.generate(testGrammar, testParsingTable);
        String parserCode = result.getFileContent("TestGrammarParser.java");
        
        assertNotNull(parserCode);
        assertTrue(parserCode.contains("package com.test.parser;"));
        assertTrue(parserCode.contains("class TestGrammarParser"));
    }
    
    @Test
    @DisplayName("Generated TokenType enum should contain all terminals")
    void testGeneratedTokenTypeEnum() throws TemplateException {
        GeneratedCode result = generator.generate(testGrammar, testParsingTable);
        String tokenTypeCode = result.getFileContent("TokenType.java");
        
        assertNotNull(tokenTypeCode);
        assertTrue(tokenTypeCode.contains("package com.test.parser;"));
        assertTrue(tokenTypeCode.contains("enum TokenType"));
        
        // Check that all terminals are included with proper enum names
        for (Terminal terminal : testGrammar.getTerminals()) {
            String expectedEnumName;
            switch (terminal.getName()) {
                case "+": expectedEnumName = "PLUS"; break;
                case "*": expectedEnumName = "MULTIPLY"; break;
                case "(": expectedEnumName = "LPAREN"; break;
                case ")": expectedEnumName = "RPAREN"; break;
                case "ID": expectedEnumName = "ID"; break;
                default: 
                    expectedEnumName = terminal.getName().replaceAll("[^a-zA-Z0-9_]", "_")
                                                        .replaceAll("_+", "_")
                                                        .replaceAll("^_|_$", "")
                                                        .toUpperCase();
                    if (expectedEnumName.isEmpty() || Character.isDigit(expectedEnumName.charAt(0))) {
                        expectedEnumName = "TOKEN_" + expectedEnumName;
                    }
                    break;
            }
            assertTrue(tokenTypeCode.contains(expectedEnumName), 
                "TokenType should contain " + expectedEnumName + " for terminal " + terminal.getName());
        }
    }
    
    @Test
    @DisplayName("Generated AST nodes should extend base class")
    void testGeneratedASTNodes() throws TemplateException {
        GeneratedCode result = generator.generate(testGrammar, testParsingTable);
        
        String exprNodeCode = result.getFileContent("ExprNode.java");
        assertNotNull(exprNodeCode);
        assertTrue(exprNodeCode.contains("package com.test.parser;"));
        assertTrue(exprNodeCode.contains("class ExprNode extends ASTNode"));
        
        String termNodeCode = result.getFileContent("TermNode.java");
        assertNotNull(termNodeCode);
        assertTrue(termNodeCode.contains("class TermNode extends ASTNode"));
        
        String factorNodeCode = result.getFileContent("FactorNode.java");
        assertNotNull(factorNodeCode);
        assertTrue(factorNodeCode.contains("class FactorNode extends ASTNode"));
    }
    
    @Test
    @DisplayName("Generated visitor should include all non-terminals")
    void testGeneratedVisitor() throws TemplateException {
        GeneratedCode result = generator.generate(testGrammar, testParsingTable);
        String visitorCode = result.getFileContent("ASTVisitor.java");
        
        assertNotNull(visitorCode);
        assertTrue(visitorCode.contains("package com.test.parser;"));
        assertTrue(visitorCode.contains("interface ASTVisitor"));
        
        // Check that visit methods are generated for all non-terminals
        assertTrue(visitorCode.contains("visitExpr"));
        assertTrue(visitorCode.contains("visitTerm"));
        assertTrue(visitorCode.contains("visitFactor"));
    }
    
    @Test
    @DisplayName("Generated parser should contain parsing tables")
    void testGeneratedParserTables() throws TemplateException {
        GeneratedCode result = generator.generate(testGrammar, testParsingTable);
        String parserCode = result.getFileContent("TestGrammarParser.java");
        
        assertNotNull(parserCode);
        assertTrue(parserCode.contains("ACTION_TABLE"));
        assertTrue(parserCode.contains("GOTO_TABLE"));
        assertTrue(parserCode.contains("PRODUCTION_LENGTHS"));
        assertTrue(parserCode.contains("PRODUCTION_LEFT_SIDES"));
    }
    
    @Test
    @DisplayName("Generated code should be valid Java syntax")
    void testGeneratedCodeSyntax() throws TemplateException {
        GeneratedCode result = generator.generate(testGrammar, testParsingTable);
        
        // Basic syntax checks for all generated files
        for (String fileName : result.getFileNames()) {
            String code = result.getFileContent(fileName);
            assertNotNull(code);
            assertFalse(code.trim().isEmpty());
            
            // Check for basic Java syntax elements
            assertTrue(code.contains("package com.test.parser;"), 
                "File " + fileName + " should have package declaration");
            
            // Check for proper class/interface/enum declaration
            assertTrue(code.matches("(?s).*\\b(class|interface|enum)\\s+\\w+.*"), 
                "File " + fileName + " should have proper type declaration");
        }
    }
    
    @Test
    @DisplayName("Generated code should handle empty grammar")
    void testGenerateWithEmptyGrammar() throws TemplateException {
        Grammar emptyGrammar = new Grammar();
        ParsingTable emptyTable = new ParsingTable();
        
        GeneratedCode result = generator.generate(emptyGrammar, emptyTable);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Should still generate basic files
        assertTrue(result.hasFile("TestGrammarParser.java"));
        assertTrue(result.hasFile("TokenType.java"));
        assertTrue(result.hasFile("Token.java"));
    }
    
    @Test
    @DisplayName("Template manager should be accessible")
    void testTemplateManagerAccess() {
        TemplateManager templateManager = generator.getTemplateManager();
        assertNotNull(templateManager);
        
        // Check that required templates exist
        assertTrue(templateManager.hasTemplate(TemplateManager.PARSER_CLASS_TEMPLATE));
        assertTrue(templateManager.hasTemplate(TemplateManager.TOKEN_TYPE_ENUM_TEMPLATE));
        assertTrue(templateManager.hasTemplate(TemplateManager.AST_NODE_BASE_TEMPLATE));
    }
}