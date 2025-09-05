package com.demo.parser;

/**
 * Library providing implementations for all mathematical functions supported by the expression evaluator.
 * This class contains static methods for trigonometric, logarithmic, and utility functions with proper
 * error handling for invalid function arguments.
 */
public class MathFunctionLibrary {

    /**
     * Evaluates a mathematical function with the given arguments.
     * 
     * @param function the mathematical function to evaluate
     * @param args the function arguments
     * @return the result of the function evaluation
     * @throws FunctionException if the function arguments are invalid or evaluation fails
     */
    public static double evaluate(MathFunction function, double... args) throws FunctionException {
        // Validate argument count
        if (!function.isValidArgCount(args.length)) {
            throw new FunctionException(
                String.format("Function %s expects %d-%d arguments, but got %d", 
                    function.getName(), function.getMinArgs(), function.getMaxArgs(), args.length),
                function.getName(), args.length);
        }

        switch (function) {
            case SIN:
                return sin(args[0]);
            case COS:
                return cos(args[0]);
            case TAN:
                return tan(args[0]);
            case LOG:
                return args.length == 1 ? log(args[0]) : log(args[0], args[1]);
            case LN:
                return ln(args[0]);
            case SQRT:
                return sqrt(args[0]);
            case ABS:
                return abs(args[0]);
            default:
                throw new FunctionException("Unknown function: " + function.getName(), function.getName());
        }
    }

    /**
     * Computes the sine of an angle in radians.
     * 
     * @param x the angle in radians
     * @return the sine of x
     */
    public static double sin(double x) {
        return Math.sin(x);
    }

    /**
     * Computes the cosine of an angle in radians.
     * 
     * @param x the angle in radians
     * @return the cosine of x
     */
    public static double cos(double x) {
        return Math.cos(x);
    }

    /**
     * Computes the tangent of an angle in radians.
     * 
     * @param x the angle in radians
     * @return the tangent of x
     */
    public static double tan(double x) {
        return Math.tan(x);
    }

    /**
     * Computes the natural logarithm (base e) of a number.
     * 
     * @param x the number (must be positive)
     * @return the natural logarithm of x
     * @throws FunctionException if x is not positive
     */
    public static double ln(double x) throws FunctionException {
        if (x <= 0) {
            throw new FunctionException("ln is undefined for non-positive values: " + x, "ln");
        }
        return Math.log(x);
    }

    /**
     * Computes the base-10 logarithm of a number.
     * 
     * @param x the number (must be positive)
     * @return the base-10 logarithm of x
     * @throws FunctionException if x is not positive
     */
    public static double log(double x) throws FunctionException {
        if (x <= 0) {
            throw new FunctionException("log is undefined for non-positive values: " + x, "log");
        }
        return Math.log10(x);
    }

    /**
     * Computes the logarithm of a number with a custom base.
     * 
     * @param base the logarithm base (must be positive and not equal to 1)
     * @param x the number (must be positive)
     * @return the base-b logarithm of x
     * @throws FunctionException if base or x are invalid
     */
    public static double log(double base, double x) throws FunctionException {
        if (base <= 0 || base == 1) {
            throw new FunctionException("log base must be positive and not equal to 1: " + base, "log");
        }
        if (x <= 0) {
            throw new FunctionException("log is undefined for non-positive values: " + x, "log");
        }
        return Math.log(x) / Math.log(base);
    }

    /**
     * Computes the square root of a number.
     * 
     * @param x the number (must be non-negative)
     * @return the square root of x
     * @throws FunctionException if x is negative
     */
    public static double sqrt(double x) throws FunctionException {
        if (x < 0) {
            throw new FunctionException("sqrt is undefined for negative values: " + x, "sqrt");
        }
        return Math.sqrt(x);
    }

    /**
     * Computes the absolute value of a number.
     * 
     * @param x the number
     * @return the absolute value of x
     */
    public static double abs(double x) {
        return Math.abs(x);
    }
}