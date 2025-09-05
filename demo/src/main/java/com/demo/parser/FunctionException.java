package com.demo.parser;

/**
 * Exception thrown when errors occur during mathematical function evaluation.
 * This includes invalid function arguments, unknown functions, or mathematical domain errors.
 */
public class FunctionException extends ExpressionException {
    private final String functionName;
    private final int argumentCount;

    /**
     * Creates a new FunctionException with a message.
     * @param message the error message
     */
    public FunctionException(String message) {
        super(message);
        this.functionName = null;
        this.argumentCount = -1;
    }

    /**
     * Creates a new FunctionException for a specific function.
     * @param message the error message
     * @param functionName the name of the function that caused the error
     */
    public FunctionException(String message, String functionName) {
        super(message);
        this.functionName = functionName;
        this.argumentCount = -1;
    }

    /**
     * Creates a new FunctionException with function name and argument count.
     * @param message the error message
     * @param functionName the name of the function that caused the error
     * @param argumentCount the number of arguments provided
     */
    public FunctionException(String message, String functionName, int argumentCount) {
        super(message);
        this.functionName = functionName;
        this.argumentCount = argumentCount;
    }

    /**
     * Creates a new FunctionException with position information.
     * @param message the error message
     * @param functionName the name of the function that caused the error
     * @param position the position where the error occurred
     */
    public FunctionException(String message, String functionName, Position position) {
        super(message, position);
        this.functionName = functionName;
        this.argumentCount = -1;
    }

    /**
     * Gets the name of the function that caused the error.
     * @return the function name, or null if not available
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Gets the number of arguments provided to the function.
     * @return the argument count, or -1 if not available
     */
    public int getArgumentCount() {
        return argumentCount;
    }

    @Override
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(super.getDetailedMessage());
        
        if (functionName != null && !functionName.isEmpty()) {
            sb.append(" in function: ").append(functionName);
            if (argumentCount >= 0) {
                sb.append(" with ").append(argumentCount).append(" argument(s)");
            }
        }
        
        return sb.toString();
    }
}