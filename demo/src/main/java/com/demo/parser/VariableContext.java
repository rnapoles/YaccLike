package com.demo.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages variable storage and lookup for expression evaluation.
 * Provides support for user-defined variables and built-in mathematical constants.
 */
public class VariableContext {
    private final Map<String, Double> variables;
    private final Map<String, Double> constants;

    /**
     * Creates a new empty VariableContext.
     */
    public VariableContext() {
        this.variables = new HashMap<>();
        this.constants = new HashMap<>();
    }

    /**
     * Creates a new VariableContext with the specified variables and constants.
     * @param variables the initial variables map
     * @param constants the initial constants map
     */
    private VariableContext(Map<String, Double> variables, Map<String, Double> constants) {
        this.variables = new HashMap<>(variables);
        this.constants = new HashMap<>(constants);
    }

    /**
     * Creates a default VariableContext with built-in mathematical constants.
     * Includes pi and e constants.
     * @return a new VariableContext with default constants
     */
    public static VariableContext createDefault() {
        VariableContext context = new VariableContext();
        context.constants.put("pi", Math.PI);
        context.constants.put("e", Math.E);
        return context;
    }

    /**
     * Sets the value of a variable.
     * @param name the variable name (case-sensitive)
     * @param value the variable value
     * @throws IllegalArgumentException if the variable name is null or empty
     */
    public void setVariable(String name, double value) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }
        variables.put(name.trim(), value);
    }

    /**
     * Gets the value of a variable or constant.
     * Variables take precedence over constants if both exist with the same name.
     * @param name the variable or constant name (case-sensitive)
     * @return the variable or constant value
     * @throws VariableNotFoundException if the variable or constant is not found
     */
    public double getVariable(String name) throws VariableNotFoundException {
        if (name == null || name.trim().isEmpty()) {
            throw new VariableNotFoundException("null or empty name");
        }
        
        String trimmedName = name.trim();
        
        // Check variables first (they take precedence over constants)
        if (variables.containsKey(trimmedName)) {
            return variables.get(trimmedName);
        }
        
        // Check constants
        if (constants.containsKey(trimmedName)) {
            return constants.get(trimmedName);
        }
        
        throw new VariableNotFoundException(trimmedName);
    }

    /**
     * Checks if a variable or constant exists.
     * @param name the variable or constant name (case-sensitive)
     * @return true if the variable or constant exists, false otherwise
     */
    public boolean hasVariable(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = name.trim();
        return variables.containsKey(trimmedName) || constants.containsKey(trimmedName);
    }

    /**
     * Removes a variable from the context.
     * Constants cannot be removed.
     * @param name the variable name to remove
     * @return true if the variable was removed, false if it didn't exist
     */
    public boolean removeVariable(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return variables.remove(name.trim()) != null;
    }

    /**
     * Gets a copy of all variables in the context.
     * @return a new map containing all variables
     */
    public Map<String, Double> getVariables() {
        return new HashMap<>(variables);
    }

    /**
     * Gets a copy of all constants in the context.
     * @return a new map containing all constants
     */
    public Map<String, Double> getConstants() {
        return new HashMap<>(constants);
    }

    /**
     * Gets the names of all variables and constants.
     * @return a set containing all variable and constant names
     */
    public Set<String> getAllNames() {
        Set<String> allNames = new HashMap<String, Double>() {{
            putAll(variables);
            putAll(constants);
        }}.keySet();
        return allNames;
    }

    /**
     * Clears all user-defined variables.
     * Constants are not affected.
     */
    public void clearVariables() {
        variables.clear();
    }

    /**
     * Creates a copy of this VariableContext.
     * @return a new VariableContext with the same variables and constants
     */
    public VariableContext copy() {
        return new VariableContext(variables, constants);
    }

    /**
     * Gets the number of variables (excluding constants).
     * @return the number of variables
     */
    public int getVariableCount() {
        return variables.size();
    }

    /**
     * Gets the number of constants.
     * @return the number of constants
     */
    public int getConstantCount() {
        return constants.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("VariableContext{");
        sb.append("variables=").append(variables);
        sb.append(", constants=").append(constants);
        sb.append('}');
        return sb.toString();
    }
}