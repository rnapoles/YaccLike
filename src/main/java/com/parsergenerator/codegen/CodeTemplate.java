package com.parsergenerator.codegen;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Manages Java code templates for parser generation.
 * Provides template loading, variable substitution, and code formatting utilities.
 */
public class CodeTemplate {
    private final String templateName;
    private final String templateContent;
    private final Map<String, Object> variables;
    
    // Pattern for template variables: ${variableName}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * Creates a new CodeTemplate with the specified name and content.
     *
     * @param templateName the name of the template
     * @param templateContent the template content with variables
     */
    public CodeTemplate(String templateName, String templateContent) {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        if (templateContent == null) {
            throw new IllegalArgumentException("Template content cannot be null");
        }
        
        this.templateName = templateName.trim();
        this.templateContent = templateContent;
        this.variables = new HashMap<>();
    }
    
    /**
     * Sets a variable value for template substitution.
     *
     * @param name the variable name
     * @param value the variable value
     * @return this template for method chaining
     */
    public CodeTemplate setVariable(String name, Object value) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }
        variables.put(name.trim(), value);
        return this;
    }
    
    /**
     * Sets multiple variables at once.
     *
     * @param variableMap map of variable names to values
     * @return this template for method chaining
     */
    public CodeTemplate setVariables(Map<String, Object> variableMap) {
        if (variableMap != null) {
            variables.putAll(variableMap);
        }
        return this;
    }
    
    /**
     * Gets the value of a variable.
     *
     * @param name the variable name
     * @return the variable value, or null if not set
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Checks if a variable is set.
     *
     * @param name the variable name
     * @return true if the variable is set, false otherwise
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }
    
    /**
     * Clears all variables.
     *
     * @return this template for method chaining
     */
    public CodeTemplate clearVariables() {
        variables.clear();
        return this;
    }
    
    /**
     * Generates the final code by substituting all variables in the template.
     *
     * @return the generated code with variables substituted
     * @throws TemplateException if required variables are missing or substitution fails
     */
    public String generate() throws TemplateException {
        try {
            String result = templateContent;
            Matcher matcher = VARIABLE_PATTERN.matcher(result);
            
            while (matcher.find()) {
                String variableName = matcher.group(1);
                
                if (!variables.containsKey(variableName)) {
                    throw new TemplateException("Missing required variable: " + variableName);
                }
                
                Object value = variables.get(variableName);
                String replacement = formatValue(value);
                result = result.replace("${" + variableName + "}", replacement);
            }
            
            return result;
        } catch (Exception e) {
            throw new TemplateException("Failed to generate code from template '" + templateName + "': " + e.getMessage(), e);
        }
    }
    
    /**
     * Formats a value for insertion into the template.
     * Handles special formatting for different types of objects.
     *
     * @param value the value to format
     * @return the formatted string representation
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        
        if (value instanceof String) {
            return (String) value;
        }
        
        if (value instanceof Number) {
            return value.toString();
        }
        
        if (value instanceof Boolean) {
            return value.toString();
        }
        
        // For other objects, use toString()
        return value.toString();
    }
    
    /**
     * Returns the template name.
     *
     * @return the template name
     */
    public String getTemplateName() {
        return templateName;
    }
    
    /**
     * Returns the original template content.
     *
     * @return the template content
     */
    public String getTemplateContent() {
        return templateContent;
    }
    
    /**
     * Returns a copy of the current variables map.
     *
     * @return map of current variables
     */
    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }
    
    /**
     * Creates a copy of this template with the same content but cleared variables.
     *
     * @return a new template instance
     */
    public CodeTemplate copy() {
        return new CodeTemplate(templateName, templateContent);
    }
    
    /**
     * Creates a copy of this template with the same content and variables.
     *
     * @return a new template instance with copied variables
     */
    public CodeTemplate copyWithVariables() {
        CodeTemplate copy = new CodeTemplate(templateName, templateContent);
        copy.variables.putAll(this.variables);
        return copy;
    }
    
    @Override
    public String toString() {
        return "CodeTemplate{" +
                "name='" + templateName + '\'' +
                ", variables=" + variables.size() +
                '}';
    }
}