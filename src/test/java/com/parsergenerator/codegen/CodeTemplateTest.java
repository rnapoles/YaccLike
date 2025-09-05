package com.parsergenerator.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CodeTemplate class.
 */
public class CodeTemplateTest {
    
    private CodeTemplate template;
    private String simpleTemplate;
    private String complexTemplate;
    
    @BeforeEach
    void setUp() {
        simpleTemplate = "Hello ${name}!";
        complexTemplate = "package ${packageName};\n\n" +
            "public class ${className} {\n" +
            "    private ${fieldType} ${fieldName};\n\n" +
            "    public ${className}(${fieldType} ${fieldName}) {\n" +
            "        this.${fieldName} = ${fieldName};\n" +
            "    }\n\n" +
            "    public ${fieldType} get${capitalizedFieldName}() {\n" +
            "        return ${fieldName};\n" +
            "    }\n" +
            "}";
    }
    
    @Test
    void testConstructorWithValidParameters() {
        template = new CodeTemplate("test", simpleTemplate);
        assertEquals("test", template.getTemplateName());
        assertEquals(simpleTemplate, template.getTemplateContent());
    }
    
    @Test
    void testConstructorWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> 
            new CodeTemplate(null, simpleTemplate));
    }
    
    @Test
    void testConstructorWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> 
            new CodeTemplate("", simpleTemplate));
    }
    
    @Test
    void testConstructorWithWhitespaceName() {
        assertThrows(IllegalArgumentException.class, () -> 
            new CodeTemplate("   ", simpleTemplate));
    }
    
    @Test
    void testConstructorWithNullContent() {
        assertThrows(IllegalArgumentException.class, () -> 
            new CodeTemplate("test", null));
    }
    
    @Test
    void testConstructorTrimsName() {
        template = new CodeTemplate("  test  ", simpleTemplate);
        assertEquals("test", template.getTemplateName());
    }
    
    @Test
    void testSetVariable() {
        template = new CodeTemplate("test", simpleTemplate);
        CodeTemplate result = template.setVariable("name", "World");
        
        assertSame(template, result); // Should return same instance for chaining
        assertEquals("World", template.getVariable("name"));
        assertTrue(template.hasVariable("name"));
    }
    
    @Test
    void testSetVariableWithNullName() {
        template = new CodeTemplate("test", simpleTemplate);
        assertThrows(IllegalArgumentException.class, () -> 
            template.setVariable(null, "value"));
    }
    
    @Test
    void testSetVariableWithEmptyName() {
        template = new CodeTemplate("test", simpleTemplate);
        assertThrows(IllegalArgumentException.class, () -> 
            template.setVariable("", "value"));
    }
    
    @Test
    void testSetVariableTrimsName() {
        template = new CodeTemplate("test", simpleTemplate);
        template.setVariable("  name  ", "World");
        assertEquals("World", template.getVariable("name"));
        assertNull(template.getVariable("  name  "));
    }
    
    @Test
    void testSetVariables() {
        template = new CodeTemplate("test", complexTemplate);
        Map<String, Object> variables = new HashMap<>();
        variables.put("packageName", "com.example");
        variables.put("className", "TestClass");
        variables.put("fieldType", "String");
        variables.put("fieldName", "value");
        
        CodeTemplate result = template.setVariables(variables);
        assertSame(template, result);
        
        assertEquals("com.example", template.getVariable("packageName"));
        assertEquals("TestClass", template.getVariable("className"));
        assertEquals("String", template.getVariable("fieldType"));
        assertEquals("value", template.getVariable("fieldName"));
    }
    
    @Test
    void testSetVariablesWithNull() {
        template = new CodeTemplate("test", simpleTemplate);
        assertDoesNotThrow(() -> template.setVariables(null));
    }
    
    @Test
    void testGetVariableNotSet() {
        template = new CodeTemplate("test", simpleTemplate);
        assertNull(template.getVariable("nonexistent"));
        assertFalse(template.hasVariable("nonexistent"));
    }
    
    @Test
    void testClearVariables() {
        template = new CodeTemplate("test", simpleTemplate);
        template.setVariable("name", "World");
        template.setVariable("other", "value");
        
        assertTrue(template.hasVariable("name"));
        assertTrue(template.hasVariable("other"));
        
        CodeTemplate result = template.clearVariables();
        assertSame(template, result);
        
        assertFalse(template.hasVariable("name"));
        assertFalse(template.hasVariable("other"));
    }
    
    @Test
    void testGenerateSimpleTemplate() throws TemplateException {
        template = new CodeTemplate("test", simpleTemplate);
        template.setVariable("name", "World");
        
        String result = template.generate();
        assertEquals("Hello World!", result);
    }
    
    @Test
    void testGenerateComplexTemplate() throws TemplateException {
        template = new CodeTemplate("test", complexTemplate);
        template.setVariable("packageName", "com.example")
                .setVariable("className", "Person")
                .setVariable("fieldType", "String")
                .setVariable("fieldName", "name")
                .setVariable("capitalizedFieldName", "Name");
        
        String result = template.generate();
        
        assertTrue(result.contains("package com.example;"));
        assertTrue(result.contains("public class Person {"));
        assertTrue(result.contains("private String name;"));
        assertTrue(result.contains("public Person(String name) {"));
        assertTrue(result.contains("public String getName() {"));
    }
    
    @Test
    void testGenerateWithMissingVariable() {
        template = new CodeTemplate("test", simpleTemplate);
        // Don't set the "name" variable
        
        TemplateException exception = assertThrows(TemplateException.class, () -> 
            template.generate());
        assertTrue(exception.getMessage().contains("Missing required variable: name"));
    }
    
    @Test
    void testGenerateWithNoVariables() throws TemplateException {
        String noVarTemplate = "This template has no variables.";
        template = new CodeTemplate("test", noVarTemplate);
        
        String result = template.generate();
        assertEquals(noVarTemplate, result);
    }
    
    @Test
    void testFormatValueString() throws TemplateException {
        template = new CodeTemplate("test", "${value}");
        template.setVariable("value", "test string");
        
        String result = template.generate();
        assertEquals("test string", result);
    }
    
    @Test
    void testFormatValueNumber() throws TemplateException {
        template = new CodeTemplate("test", "${value}");
        template.setVariable("value", 42);
        
        String result = template.generate();
        assertEquals("42", result);
    }
    
    @Test
    void testFormatValueBoolean() throws TemplateException {
        template = new CodeTemplate("test", "${value}");
        template.setVariable("value", true);
        
        String result = template.generate();
        assertEquals("true", result);
    }
    
    @Test
    void testFormatValueNull() throws TemplateException {
        template = new CodeTemplate("test", "${value}");
        template.setVariable("value", null);
        
        String result = template.generate();
        assertEquals("null", result);
    }
    
    @Test
    void testFormatValueObject() throws TemplateException {
        template = new CodeTemplate("test", "${value}");
        Object obj = new Object() {
            @Override
            public String toString() {
                return "custom object";
            }
        };
        template.setVariable("value", obj);
        
        String result = template.generate();
        assertEquals("custom object", result);
    }
    
    @Test
    void testGetVariables() {
        template = new CodeTemplate("test", simpleTemplate);
        template.setVariable("name", "World");
        template.setVariable("other", "value");
        
        Map<String, Object> variables = template.getVariables();
        assertEquals(2, variables.size());
        assertEquals("World", variables.get("name"));
        assertEquals("value", variables.get("other"));
        
        // Should be a copy - modifying returned map shouldn't affect template
        variables.put("new", "value");
        assertFalse(template.hasVariable("new"));
    }
    
    @Test
    void testCopy() {
        template = new CodeTemplate("test", simpleTemplate);
        template.setVariable("name", "World");
        
        CodeTemplate copy = template.copy();
        
        assertEquals(template.getTemplateName(), copy.getTemplateName());
        assertEquals(template.getTemplateContent(), copy.getTemplateContent());
        assertFalse(copy.hasVariable("name")); // Variables should not be copied
        assertNotSame(template, copy);
    }
    
    @Test
    void testCopyWithVariables() {
        template = new CodeTemplate("test", simpleTemplate);
        template.setVariable("name", "World");
        template.setVariable("other", "value");
        
        CodeTemplate copy = template.copyWithVariables();
        
        assertEquals(template.getTemplateName(), copy.getTemplateName());
        assertEquals(template.getTemplateContent(), copy.getTemplateContent());
        assertTrue(copy.hasVariable("name"));
        assertTrue(copy.hasVariable("other"));
        assertEquals("World", copy.getVariable("name"));
        assertEquals("value", copy.getVariable("other"));
        assertNotSame(template, copy);
        
        // Modifying copy shouldn't affect original
        copy.setVariable("name", "Modified");
        assertEquals("World", template.getVariable("name"));
        assertEquals("Modified", copy.getVariable("name"));
    }
    
    @Test
    void testToString() {
        template = new CodeTemplate("test", simpleTemplate);
        template.setVariable("name", "World");
        template.setVariable("other", "value");
        
        String str = template.toString();
        assertTrue(str.contains("test"));
        assertTrue(str.contains("2")); // Should show variable count
    }
    
    @Test
    void testMultipleVariableSubstitutions() throws TemplateException {
        String multiTemplate = "${greeting} ${name}! Your age is ${age} and you are ${active}.";
        template = new CodeTemplate("test", multiTemplate);
        template.setVariable("greeting", "Hello")
                .setVariable("name", "John")
                .setVariable("age", 25)
                .setVariable("active", true);
        
        String result = template.generate();
        assertEquals("Hello John! Your age is 25 and you are true.", result);
    }
    
    @Test
    void testVariableWithSpecialCharacters() throws TemplateException {
        template = new CodeTemplate("test", "${value}");
        template.setVariable("value", "Special chars: ${}[]()");
        
        String result = template.generate();
        assertEquals("Special chars: ${}[]()", result);
    }
    
    @Test
    void testEmptyTemplate() throws TemplateException {
        template = new CodeTemplate("test", "");
        String result = template.generate();
        assertEquals("", result);
    }
}