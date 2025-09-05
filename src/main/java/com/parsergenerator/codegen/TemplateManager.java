package com.parsergenerator.codegen;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.pebbletemplates.pebble.loader.ClasspathLoader;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages a collection of code templates for parser generation using Pebble template engine.
 * Provides access to predefined templates for parser classes, token classes, and AST nodes.
 */
public class TemplateManager {
    private final PebbleEngine pebbleEngine;
    private final Map<String, String> templatePaths;
    
    // Template names
    public static final String PARSER_CLASS_TEMPLATE = "parser_class";
    public static final String TOKEN_CLASS_TEMPLATE = "token_class";
    public static final String TOKEN_TYPE_ENUM_TEMPLATE = "token_type_enum";
    public static final String AST_NODE_BASE_TEMPLATE = "ast_node_base";
    public static final String AST_NODE_TERMINAL_TEMPLATE = "ast_node_terminal";
    public static final String AST_NODE_NONTERMINAL_TEMPLATE = "ast_node_nonterminal";
    public static final String AST_VISITOR_TEMPLATE = "ast_visitor";
    public static final String PARSE_EXCEPTION_TEMPLATE = "parse_exception";
    public static final String PARSING_TABLE_TEMPLATE = "parsing_table";
    public static final String POSITION_CLASS_TEMPLATE = "position_class";
    
    /**
     * Creates a new TemplateManager with Pebble engine configured for classpath templates.
     */
    public TemplateManager() {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates/");
        loader.setSuffix("");
        
        this.pebbleEngine = new PebbleEngine.Builder()
                .loader(loader)
                .strictVariables(false)
                .build();
        
        this.templatePaths = new HashMap<>();
        initializeTemplatePaths();
    }
    
    /**
     * Renders a template with the given variables.
     *
     * @param templateName the name of the template
     * @param variables the variables to pass to the template
     * @return the rendered template content
     * @throws TemplateException if template rendering fails
     */
    public String renderTemplate(String templateName, Map<String, Object> variables) throws TemplateException {
        String templatePath = templatePaths.get(templateName);
        if (templatePath == null) {
            throw new TemplateException("Template not found: " + templateName);
        }
        
        try {
            PebbleTemplate template = pebbleEngine.getTemplate(templatePath);
            StringWriter writer = new StringWriter();
            template.evaluate(writer, variables != null ? variables : new HashMap<>());
            return writer.toString();
        } catch (IOException e) {
            throw new TemplateException("Failed to render template '" + templateName + "': " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a CodeTemplate instance for the specified template name.
     * This method provides backward compatibility with the existing CodeTemplate API.
     *
     * @param templateName the name of the template
     * @return a new PebbleCodeTemplate instance
     * @throws IllegalArgumentException if template is not found
     */
    public CodeTemplate createTemplate(String templateName) {
        if (!hasTemplate(templateName)) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        return new PebbleCodeTemplate(templateName, this);
    }
    
    /**
     * Checks if a template with the given name exists.
     *
     * @param templateName the name to check
     * @return true if the template exists, false otherwise
     */
    public boolean hasTemplate(String templateName) {
        return templatePaths.containsKey(templateName);
    }
    
    /**
     * Returns the names of all available templates.
     *
     * @return set of template names
     */
    public Set<String> getTemplateNames() {
        return templatePaths.keySet();
    }
    
    /**
     * Returns the number of templates.
     *
     * @return the template count
     */
    public int getTemplateCount() {
        return templatePaths.size();
    }
    
    /**
     * Gets the Pebble engine instance.
     *
     * @return the Pebble engine
     */
    public PebbleEngine getPebbleEngine() {
        return pebbleEngine;
    }
    
    /**
     * Initializes the mapping of template names to template file paths.
     */
    private void initializeTemplatePaths() {
        templatePaths.put(PARSER_CLASS_TEMPLATE, "parser_class.peb");
        templatePaths.put(TOKEN_CLASS_TEMPLATE, "token_class.peb");
        templatePaths.put(TOKEN_TYPE_ENUM_TEMPLATE, "token_type_enum.peb");
        templatePaths.put(AST_NODE_BASE_TEMPLATE, "ast_node_base.peb");
        templatePaths.put(AST_NODE_TERMINAL_TEMPLATE, "ast_node_terminal.peb");
        templatePaths.put(AST_NODE_NONTERMINAL_TEMPLATE, "ast_node_nonterminal.peb");
        templatePaths.put(AST_VISITOR_TEMPLATE, "ast_visitor.peb");
        templatePaths.put(PARSE_EXCEPTION_TEMPLATE, "parse_exception.peb");
        templatePaths.put(PARSING_TABLE_TEMPLATE, "parsing_table.peb");
        templatePaths.put(POSITION_CLASS_TEMPLATE, "position_class.peb");
    }
    
    /**
     * Backward compatibility wrapper that adapts Pebble templates to the CodeTemplate interface.
     */
    private static class PebbleCodeTemplate extends CodeTemplate {
        private final String templateName;
        private final TemplateManager templateManager;
        private final Map<String, Object> variables;
        
        public PebbleCodeTemplate(String templateName, TemplateManager templateManager) {
            super(templateName, ""); // Empty content since we use Pebble
            this.templateName = templateName;
            this.templateManager = templateManager;
            this.variables = new HashMap<>();
        }
        
        @Override
        public CodeTemplate setVariable(String name, Object value) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Variable name cannot be null or empty");
            }
            variables.put(name.trim(), value);
            return this;
        }
        
        @Override
        public CodeTemplate setVariables(Map<String, Object> variableMap) {
            if (variableMap != null) {
                variables.putAll(variableMap);
            }
            return this;
        }
        
        @Override
        public Object getVariable(String name) {
            return variables.get(name);
        }
        
        @Override
        public boolean hasVariable(String name) {
            return variables.containsKey(name);
        }
        
        @Override
        public CodeTemplate clearVariables() {
            variables.clear();
            return this;
        }
        
        @Override
        public String generate() throws TemplateException {
            return templateManager.renderTemplate(templateName, variables);
        }
        
        @Override
        public Map<String, Object> getVariables() {
            return new HashMap<>(variables);
        }
        
        @Override
        public CodeTemplate copy() {
            return new PebbleCodeTemplate(templateName, templateManager);
        }
        
        @Override
        public CodeTemplate copyWithVariables() {
            PebbleCodeTemplate copy = new PebbleCodeTemplate(templateName, templateManager);
            copy.variables.putAll(this.variables);
            return copy;
        }
    }
}