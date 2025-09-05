package com.demo.parser;

/**
 * Enumeration of mathematical functions supported by the expression evaluator.
 * Each function has a name and specifies the number of arguments it accepts.
 */
public enum MathFunction {
    SIN("sin", 1),
    COS("cos", 1),
    TAN("tan", 1),
    LOG("log", 1, 2), // log can take 1 or 2 arguments (base 10 or custom base)
    LN("ln", 1),
    SQRT("sqrt", 1),
    ABS("abs", 1);

    private final String name;
    private final int minArgs;
    private final int maxArgs;

    // Constructor for functions with fixed argument count
    MathFunction(String name, int argCount) {
        this.name = name;
        this.minArgs = argCount;
        this.maxArgs = argCount;
    }

    // Constructor for functions with variable argument count
    MathFunction(String name, int minArgs, int maxArgs) {
        this.name = name;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
    }

    /**
     * Gets the function name.
     * @return the function name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the minimum number of arguments this function accepts.
     * @return the minimum argument count
     */
    public int getMinArgs() {
        return minArgs;
    }

    /**
     * Gets the maximum number of arguments this function accepts.
     * @return the maximum argument count
     */
    public int getMaxArgs() {
        return maxArgs;
    }

    /**
     * Checks if the given argument count is valid for this function.
     * @param argCount the number of arguments to check
     * @return true if the argument count is valid, false otherwise
     */
    public boolean isValidArgCount(int argCount) {
        return argCount >= minArgs && argCount <= maxArgs;
    }

    /**
     * Finds a mathematical function by its name.
     * @param name the function name to look for (case-insensitive)
     * @return the matching MathFunction, or null if not found
     */
    public static MathFunction fromName(String name) {
        for (MathFunction func : values()) {
            if (func.name.equalsIgnoreCase(name)) {
                return func;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}