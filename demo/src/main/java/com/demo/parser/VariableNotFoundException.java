package com.demo.parser;

/**
 * Exception thrown when attempting to access an undefined variable during expression evaluation.
 */
public class VariableNotFoundException extends ExpressionException {
    private final String variableName;

    /**
     * Creates a new VariableNotFoundException for the specified variable.
     * @param variableName the name of the undefined variable
     */
    public VariableNotFoundException(String variableName) {
        super("Variable not found: " + variableName);
        this.variableName = variableName;
    }

    /**
     * Creates a new VariableNotFoundException with position information.
     * @param variableName the name of the undefined variable
     * @param position the position where the variable was referenced
     */
    public VariableNotFoundException(String variableName, Position position) {
        super("Variable not found: " + variableName, position);
        this.variableName = variableName;
    }

    /**
     * Gets the name of the undefined variable.
     * @return the variable name
     */
    public String getVariableName() {
        return variableName;
    }
}