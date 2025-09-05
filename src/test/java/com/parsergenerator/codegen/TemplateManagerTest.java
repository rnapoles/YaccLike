package com.parsergenerator.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TemplateManager class using Pebble template engine.
 */
public class TemplateManagerTest {
    
    private TemplateManager templateManager;
    
    @BeforeEach
    void setUp() {
        templateManager = new TemplateManager();
    }
    
    @Test
    void testConstructorInitializesTemplates() {
        assertTrue(templateManager.getTemplateCount() > 0);
        
        // Check that all expected templates are present
        assertTrue(templateManager.hasTemplate(TemplateManager.PARSER_CLASS_TEMPLATE));
        assertTrue(templateManager.hasTemplate(TemplateManager.TOKEN_CLASS_TEMPLATE));
        assertTrue(templateManager.hasTemplate(TemplateManager.TOKEN_TYPE_ENUM_TEMPLATE));
        assertTrue(templateManager.hasTemplate(TemplateManager.AST_NODE_BASE_TEMPLATE));
        assertTrue(templateManager.hasTemplate(TemplateManager.AST_NODE_TERMINAL_TEMPLATE));
        assertTrue(templateManager.hasTemplate(TemplateManager.AST_NODE_NONTERMINAL_TEMPLATE));
        assertTrue(templateManager.hasTemplate(TemplateManager.PARSE_EXCEPTION_TEMPLATE));
        assertTrue(templateManager.hasTemplate(TemplateManager.PARSING_TABLE_TEMPLATE));
    }
    
    @Test
    void testRenderTemplate() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", "com.example");
        variables.put("grammarName", "TestGrammar");
        
        String result = templateManager.renderTemplate(TemplateManager.TOKEN_CLASS_TEMPLATE, variables);
        assertNotNull(result);
        assertTrue(result.contains("package com.example;"));
        assertTrue(result.contains("TestGrammar grammar"));
    }
    
    @Test
    void testRenderTemplateWithNullVariables() throws TemplateException {
        String result = templateManager.renderTemplate(TemplateManager.PARSE_EXCEPTION_TEMPLATE, null);
        assertNotNull(result);
        assertTrue(result.contains("public class ParseException"));
    }
    
    @Test
    void testRenderTemplateNotFound() {
        Map<String, Object> variables = new HashMap<>();
        assertThrows(TemplateException.class, () -> 
            templateManager.renderTemplate("nonexistent", variables));
    }
    
    @Test
    void testCreateTemplate() {
        CodeTemplate template = templateManager.createTemplate(TemplateManager.PARSER_CLASS_TEMPLATE);
        assertNotNull(template);
        assertEquals(TemplateManager.PARSER_CLASS_TEMPLATE, template.getTemplateName());
    }
    
    @Test
    void testCreateTemplateNotFound() {
        assertThrows(IllegalArgumentException.class, () -> 
            templateManager.createTemplate("nonexistent"));
    }
    
    @Test
    void testHasTemplate() {
        assertTrue(templateManager.hasTemplate(TemplateManager.PARSER_CLASS_TEMPLATE));
        assertTrue(templateManager.hasTemplate(TemplateManager.TOKEN_CLASS_TEMPLATE));
        assertFalse(templateManager.hasTemplate("nonexistent"));
    }
    
    @Test
    void testGetTemplateNames() {
        Set<String> names = templateManager.getTemplateNames();
        assertNotNull(names);
        assertEquals(10, names.size()); // Exactly 10 predefined templates
        
        assertTrue(names.contains(TemplateManager.PARSER_CLASS_TEMPLATE));
        assertTrue(names.contains(TemplateManager.TOKEN_CLASS_TEMPLATE));
        assertTrue(names.contains(TemplateManager.TOKEN_TYPE_ENUM_TEMPLATE));
        assertTrue(names.contains(TemplateManager.AST_NODE_BASE_TEMPLATE));
        assertTrue(names.contains(TemplateManager.AST_NODE_TERMINAL_TEMPLATE));
        assertTrue(names.contains(TemplateManager.AST_NODE_NONTERMINAL_TEMPLATE));
        assertTrue(names.contains(TemplateManager.AST_VISITOR_TEMPLATE));
        assertTrue(names.contains(TemplateManager.PARSE_EXCEPTION_TEMPLATE));
        assertTrue(names.contains(TemplateManager.PARSING_TABLE_TEMPLATE));
        assertTrue(names.contains(TemplateManager.POSITION_CLASS_TEMPLATE));
    }
    
    @Test
    void testGetTemplateCount() {
        assertEquals(10, templateManager.getTemplateCount());
    }
    
    @Test
    void testGetPebbleEngine() {
        assertNotNull(templateManager.getPebbleEngine());
    }
    
    @Test
    void testParserClassTemplateRendering() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", "com.example.parser");
        variables.put("grammarName", "Calculator");
        variables.put("className", "CalculatorParser");
        variables.put("astNodeBaseClass", "ASTNode");
        variables.put("terminalNodeClass", "TerminalNode");
        variables.put("actionTable", "{{1, 2}, {3, 4}}");
        variables.put("gotoTable", "{{5, 6}, {7, 8}}");
        variables.put("eofToken", "0");
        variables.put("productionLengths", "{2, 3, 1}");
        variables.put("productionLeftSides", "{0, 1, 2}");
        variables.put("createNodeSwitch", "return null; // TODO: implement");
        
        String result = templateManager.renderTemplate(TemplateManager.PARSER_CLASS_TEMPLATE, variables);
        
        // Check for essential parser components
        assertTrue(result.contains("package com.example.parser;"));
        assertTrue(result.contains("class CalculatorParser"));
        assertTrue(result.contains("ACTION_TABLE"));
        assertTrue(result.contains("GOTO_TABLE"));
        assertTrue(result.contains("parse(List<Token> tokens)"));
        assertTrue(result.contains("shift("));
        assertTrue(result.contains("reduce("));
        assertTrue(result.contains("ParseException"));
    }
    
    @Test
    void testTokenClassTemplateRendering() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", "com.example.tokens");
        variables.put("grammarName", "Calculator");
        
        String result = templateManager.renderTemplate(TemplateManager.TOKEN_CLASS_TEMPLATE, variables);
        
        // Check for essential token components
        assertTrue(result.contains("package com.example.tokens;"));
        assertTrue(result.contains("class Token"));
        assertTrue(result.contains("TokenType type"));
        assertTrue(result.contains("String value"));
        assertTrue(result.contains("Position position"));
        // Position is now a separate class, not an inner class
        assertFalse(result.contains("class Position"));
    }
    
    @Test
    void testTokenTypeEnumTemplateRendering() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", "com.example.tokens");
        variables.put("grammarName", "Calculator");
        variables.put("tokenTypes", new String[]{"NUMBER", "PLUS", "MINUS", "MULTIPLY", "DIVIDE"});
        
        String result = templateManager.renderTemplate(TemplateManager.TOKEN_TYPE_ENUM_TEMPLATE, variables);
        
        // Check for enum structure
        assertTrue(result.contains("package com.example.tokens;"));
        assertTrue(result.contains("enum TokenType"));
        assertTrue(result.contains("NUMBER, PLUS, MINUS, MULTIPLY, DIVIDE"));
        assertTrue(result.contains("EOF"));
    }
    
    @Test
    void testASTNodeBaseTemplateRendering() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", "com.example.ast");
        variables.put("grammarName", "Calculator");
        variables.put("className", "ASTNode");
        
        String result = templateManager.renderTemplate(TemplateManager.AST_NODE_BASE_TEMPLATE, variables);
        
        // Check for abstract base class structure
        assertTrue(result.contains("package com.example.ast;"));
        assertTrue(result.contains("abstract class ASTNode"));
        assertTrue(result.contains("abstract String getNodeType()"));
        assertTrue(result.contains("abstract List<ASTNode> getChildren()"));
        assertTrue(result.contains("abstract boolean isTerminal()"));
        assertTrue(result.contains("abstract <T> T accept(ASTVisitor<T> visitor)"));
    }
    
    @Test
    void testASTNodeTerminalTemplateRendering() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", "com.example.ast");
        variables.put("className", "TerminalNode");
        variables.put("baseClassName", "ASTNode");
        
        String result = templateManager.renderTemplate(TemplateManager.AST_NODE_TERMINAL_TEMPLATE, variables);
        
        // Check for terminal node structure
        assertTrue(result.contains("package com.example.ast;"));
        assertTrue(result.contains("class TerminalNode extends ASTNode"));
        assertTrue(result.contains("Token token"));
        assertTrue(result.contains("isTerminal()"));
        assertTrue(result.contains("return true"));
        assertTrue(result.contains("visitTerminal"));
    }
    
    @Test
    void testASTNodeNonTerminalTemplateRendering() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", "com.example.ast");
        variables.put("className", "ExpressionNode");
        variables.put("baseClassName", "ASTNode");
        variables.put("symbolName", "Expression");
        
        String result = templateManager.renderTemplate(TemplateManager.AST_NODE_NONTERMINAL_TEMPLATE, variables);
        
        // Check for non-terminal node structure
        assertTrue(result.contains("package com.example.ast;"));
        assertTrue(result.contains("class ExpressionNode extends ASTNode"));
        assertTrue(result.contains("List<ASTNode> children"));
        assertTrue(result.contains("int productionNumber"));
        assertTrue(result.contains("isTerminal()"));
        assertTrue(result.contains("return false"));
        assertTrue(result.contains("visitExpression"));
    }
    
    @Test
    void testParseExceptionTemplateRendering() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", "com.example.parser");
        
        String result = templateManager.renderTemplate(TemplateManager.PARSE_EXCEPTION_TEMPLATE, variables);
        
        // Check for exception structure
        assertTrue(result.contains("package com.example.parser;"));
        assertTrue(result.contains("class ParseException extends Exception"));
        assertTrue(result.contains("String message"));
        assertTrue(result.contains("Throwable cause"));
    }
    
    @Test
    void testParsingTableTemplateRendering() throws TemplateException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", "com.example.parser");
        variables.put("grammarName", "Calculator");
        variables.put("actionTable", "{{1, 2}, {3, 4}}");
        variables.put("gotoTable", "{{5, 6}, {7, 8}}");
        variables.put("productionLengths", "{2, 3, 1}");
        variables.put("productionLeftSides", "{0, 1, 2}");
        variables.put("terminalNames", "{\"NUMBER\", \"PLUS\"}");
        variables.put("nonterminalNames", "{\"Expression\", \"Term\"}");
        
        String result = templateManager.renderTemplate(TemplateManager.PARSING_TABLE_TEMPLATE, variables);
        
        // Check for parsing table structure
        assertTrue(result.contains("package com.example.parser;"));
        assertTrue(result.contains("class ParsingTables"));
        assertTrue(result.contains("ACTION_TABLE"));
        assertTrue(result.contains("GOTO_TABLE"));
        assertTrue(result.contains("PRODUCTION_LENGTHS"));
        assertTrue(result.contains("PRODUCTION_LEFT_SIDES"));
        assertTrue(result.contains("TERMINAL_NAMES"));
        assertTrue(result.contains("NONTERMINAL_NAMES"));
    }
    
    @Test
    void testCodeTemplateBackwardCompatibility() throws TemplateException {
        CodeTemplate template = templateManager.createTemplate(TemplateManager.TOKEN_CLASS_TEMPLATE);
        template.setVariable("packageName", "com.example")
                .setVariable("grammarName", "TestGrammar");
        
        String result = template.generate();
        assertTrue(result.contains("package com.example;"));
        assertTrue(result.contains("TestGrammar grammar"));
        
        // Test variable management
        assertTrue(template.hasVariable("packageName"));
        assertEquals("com.example", template.getVariable("packageName"));
        
        Map<String, Object> variables = template.getVariables();
        assertEquals(2, variables.size());
        
        template.clearVariables();
        assertFalse(template.hasVariable("packageName"));
    }
    
    @Test
    void testCodeTemplateCopy() {
        CodeTemplate template = templateManager.createTemplate(TemplateManager.TOKEN_CLASS_TEMPLATE);
        template.setVariable("packageName", "com.example");
        
        CodeTemplate copy = template.copy();
        assertEquals(template.getTemplateName(), copy.getTemplateName());
        assertFalse(copy.hasVariable("packageName")); // Variables should not be copied
        
        CodeTemplate copyWithVars = template.copyWithVariables();
        assertEquals(template.getTemplateName(), copyWithVars.getTemplateName());
        assertTrue(copyWithVars.hasVariable("packageName")); // Variables should be copied
        assertEquals("com.example", copyWithVars.getVariable("packageName"));
    }
    
    @Test
    void testTemplateConstants() {
        // Verify that all template constants are defined
        assertEquals("parser_class", TemplateManager.PARSER_CLASS_TEMPLATE);
        assertEquals("token_class", TemplateManager.TOKEN_CLASS_TEMPLATE);
        assertEquals("token_type_enum", TemplateManager.TOKEN_TYPE_ENUM_TEMPLATE);
        assertEquals("ast_node_base", TemplateManager.AST_NODE_BASE_TEMPLATE);
        assertEquals("ast_node_terminal", TemplateManager.AST_NODE_TERMINAL_TEMPLATE);
        assertEquals("ast_node_nonterminal", TemplateManager.AST_NODE_NONTERMINAL_TEMPLATE);
        assertEquals("parse_exception", TemplateManager.PARSE_EXCEPTION_TEMPLATE);
        assertEquals("parsing_table", TemplateManager.PARSING_TABLE_TEMPLATE);
    }
}