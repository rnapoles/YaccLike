package com.parsergenerator.examples;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

/**
 * Simple calculator demonstration that shows the concept of expression evaluation
 * without requiring generated parser classes. This demonstrates the visitor pattern
 * concept that would be used with the generated parser.
 */
public class SimpleCalculatorDemo {
    
    private final Map<String, Double> variables = new HashMap<>();
    private final Map<String, MathFunction> functions = new HashMap<>();
    
    @FunctionalInterface
    public interface MathFunction {
        double apply(double... args);
    }
    
    public SimpleCalculatorDemo() {
        // Initialize built-in mathematical functions
        functions.put("sin", args -> Math.sin(args[0]));
        functions.put("cos", args -> Math.cos(args[0]));
        functions.put("tan", args -> Math.tan(args[0]));
        functions.put("sqrt", args -> Math.sqrt(args[0]));
        functions.put("abs", args -> Math.abs(args[0]));
        functions.put("log", args -> Math.log10(args[0]));
        functions.put("exp", args -> Math.exp(args[0]));
        
        // Mathematical constants
        variables.put("PI", Math.PI);
        variables.put("E", Math.E);
    }
    
    /**
     * Simple expression evaluator using a basic recursive descent approach.
     * This demonstrates the kind of evaluation logic that would be implemented
     * in a visitor for the generated parser.
     */
    public double evaluate(String expression) {
        expression = expression.trim();
        if (expression.isEmpty()) {
            return 0.0;
        }
        
        // Handle variable assignment
        if (expression.contains("=") && !expression.contains("==")) {
            String[] parts = expression.split("=", 2);
            if (parts.length == 2) {
                String varName = parts[0].trim();
                double value = evaluate(parts[1].trim());
                variables.put(varName, value);
                return value;
            }
        }
        
        return evaluateExpression(expression);
    }
    
    private double evaluateExpression(String expr) {
        // Simple evaluation using built-in JavaScript engine approach
        // This is a simplified version - the real implementation would use the generated parser
        
        // Handle functions first (before parentheses processing)
        for (String funcName : functions.keySet()) {
            // Use word boundary to avoid partial matches
            String pattern = "\\b" + funcName + "\\s*\\(([^)]+)\\)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(expr);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String argStr = m.group(1);
                double arg = evaluateExpression(argStr);
                double result = functions.get(funcName).apply(arg);
                m.appendReplacement(sb, " " + String.valueOf(result) + " ");
            }
            m.appendTail(sb);
            expr = sb.toString();
        }
        
        // Handle parentheses
        while (expr.contains("(")) {
            int start = expr.lastIndexOf('(');
            int end = expr.indexOf(')', start);
            if (end == -1) {
                throw new RuntimeException("Mismatched parentheses");
            }
            String subExpr = expr.substring(start + 1, end);
            double result = evaluateExpression(subExpr);
            expr = expr.substring(0, start) + " " + result + " " + expr.substring(end + 1);
        }
        
        // Replace variables
        for (Map.Entry<String, Double> var : variables.entrySet()) {
            expr = expr.replaceAll("\\b" + var.getKey() + "\\b", var.getValue().toString());
        }
        
        // Simple arithmetic evaluation
        return evaluateArithmetic(expr);
    }
    
    private double evaluateArithmetic(String expr) {
        // This is a very simplified arithmetic evaluator
        // The real implementation would use the generated parser's AST
        
        try {
            // Handle simple cases
            expr = expr.trim();
            
            // Handle unary minus
            if (expr.startsWith("-")) {
                return -evaluateArithmetic(expr.substring(1));
            }
            
            // Try to parse as a number
            try {
                return Double.parseDouble(expr);
            } catch (NumberFormatException e) {
                // Continue with operator handling
            }
            
            // Handle operators with precedence (simplified)
            String[] addSubOps = {"+", "-"};
            for (String op : addSubOps) {
                int lastIndex = expr.lastIndexOf(op);
                if (lastIndex > 0) { // Not at the beginning (unary minus)
                    String left = expr.substring(0, lastIndex).trim();
                    String right = expr.substring(lastIndex + 1).trim();
                    double leftVal = evaluateArithmetic(left);
                    double rightVal = evaluateArithmetic(right);
                    
                    switch (op) {
                        case "+": return leftVal + rightVal;
                        case "-": return leftVal - rightVal;
                    }
                }
            }
            
            String[] mulDivOps = {"*", "/"};
            for (String op : mulDivOps) {
                int lastIndex = expr.lastIndexOf(op);
                if (lastIndex > 0) {
                    String left = expr.substring(0, lastIndex).trim();
                    String right = expr.substring(lastIndex + 1).trim();
                    double leftVal = evaluateArithmetic(left);
                    double rightVal = evaluateArithmetic(right);
                    
                    switch (op) {
                        case "*": return leftVal * rightVal;
                        case "/": 
                            if (rightVal == 0) throw new RuntimeException("Division by zero");
                            return leftVal / rightVal;
                    }
                }
            }
            
            // Handle power operator (right-associative)
            int powIndex = expr.indexOf("^");
            if (powIndex > 0) {
                String left = expr.substring(0, powIndex).trim();
                String right = expr.substring(powIndex + 1).trim();
                double leftVal = evaluateArithmetic(left);
                double rightVal = evaluateArithmetic(right);
                return Math.pow(leftVal, rightVal);
            }
            
            throw new RuntimeException("Cannot evaluate expression: " + expr);
            
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating: " + expr + " - " + e.getMessage());
        }
    }
    
    public void setVariable(String name, double value) {
        variables.put(name, value);
    }
    
    public double getVariable(String name) {
        return variables.getOrDefault(name, 0.0);
    }
    
    public Map<String, Double> getVariables() {
        return new HashMap<>(variables);
    }
    
    public static void main(String[] args) {
        SimpleCalculatorDemo calc = new SimpleCalculatorDemo();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Simple Calculator Demo ===");
        System.out.println("This demonstrates the concept of expression evaluation");
        System.out.println("that would be implemented with the generated parser.");
        System.out.println();
        System.out.println("Supported operations: +, -, *, /, ^");
        System.out.println("Supported functions: sin, cos, tan, sqrt, abs, log, exp");
        System.out.println("Variables: Use 'var = expression' to assign values");
        System.out.println("Constants: PI, E are predefined");
        System.out.println("Type 'demo' for examples, 'vars' to see variables, 'quit' to exit");
        System.out.println();
        
        // Run demo examples
        runDemo(calc);
        
        // Interactive mode
        while (true) {
            System.out.print("calc> ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                break;
            }
            
            if (input.equalsIgnoreCase("demo")) {
                runDemo(calc);
                continue;
            }
            
            if (input.equalsIgnoreCase("vars")) {
                showVariables(calc);
                continue;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            try {
                double result = calc.evaluate(input);
                System.out.println("Result: " + formatResult(result));
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        
        scanner.close();
        System.out.println("Calculator demo ended.");
    }
    
    private static void runDemo(SimpleCalculatorDemo calc) {
        System.out.println("\n=== Running Demo Examples ===");
        String[] examples = {
            "2 + 3 * 4",                    // Basic arithmetic
            "x = 10",                       // Variable assignment
            "y = x * 2 + 5",               // Using variables
            "sqrt(x^2 + y^2)",             // Function call with expression
            "angle = PI / 4",               // Using constants
            "sin(angle)",                   // Trigonometric function
            "result = (x + y) * sin(angle)", // Complex expression
            "abs(-15.5)",                   // Absolute value
            "log(100)",                     // Logarithm
            "2^3^2"                         // Right-associative exponentiation
        };
        
        for (String example : examples) {
            System.out.println("Executing: " + example);
            try {
                double result = calc.evaluate(example);
                System.out.println("Result: " + formatResult(result));
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
        System.out.println("Demo completed.\n");
    }
    
    private static void showVariables(SimpleCalculatorDemo calc) {
        System.out.println("\n=== Current Variables ===");
        var variables = calc.getVariables();
        if (variables.isEmpty()) {
            System.out.println("No variables defined.");
        } else {
            for (var entry : variables.entrySet()) {
                System.out.println(entry.getKey() + " = " + formatResult(entry.getValue()));
            }
        }
        System.out.println();
    }
    
    private static String formatResult(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.format("%.0f", value);
        } else {
            return String.format("%.6f", value).replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }
}