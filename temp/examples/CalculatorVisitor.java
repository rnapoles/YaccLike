package com.parsergenerator.examples;

import com.generated.parser.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Calculator visitor that evaluates mathematical expressions using the generated parser.
 * Supports arithmetic operations, variables, and basic mathematical functions.
 */
public class CalculatorVisitor implements ASTVisitor<Double> {
    
    private final Map<String, Double> variables = new HashMap<>();
    private final Map<String, MathFunction> functions = new HashMap<>();
    
    /**
     * Functional interface for mathematical functions.
     */
    @FunctionalInterface
    public interface MathFunction {
        double apply(double... args);
    }
    
    public CalculatorVisitor() {
        // Initialize built-in mathematical functions
        functions.put("sin", args -> Math.sin(args[0]));
        functions.put("cos", args -> Math.cos(args[0]));
        functions.put("tan", args -> Math.tan(args[0]));
        functions.put("sqrt", args -> Math.sqrt(args[0]));
        functions.put("abs", args -> Math.abs(args[0]));
        functions.put("log", args -> Math.log(args[0]));
        functions.put("log10", args -> Math.log10(args[0]));
        functions.put("exp", args -> Math.exp(args[0]));
        functions.put("pow", args -> Math.pow(args[0], args[1]));
        functions.put("min", args -> args.length == 2 ? Math.min(args[0], args[1]) : 
                                    java.util.Arrays.stream(args).min().orElse(0.0));
        functions.put("max", args -> args.length == 2 ? Math.max(args[0], args[1]) : 
                                    java.util.Arrays.stream(args).max().orElse(0.0));
        
        // Mathematical constants
        variables.put("PI", Math.PI);
        variables.put("E", Math.E);
    }
    
    /**
     * Sets a variable value.
     */
    public void setVariable(String name, double value) {
        variables.put(name, value);
    }
    
    /**
     * Gets a variable value.
     */
    public double getVariable(String name) {
        return variables.getOrDefault(name, 0.0);
    }
    
    /**
     * Gets all variables.
     */
    public Map<String, Double> getVariables() {
        return new HashMap<>(variables);
    }
    
    @Override
    public Double visitTerminal(TerminalNode node) {
        Token token = node.getToken();
        switch (token.getType()) {
            case NUMBER:
                return Double.parseDouble(token.getValue());
            case IDENTIFIER:
                String name = token.getValue();
                if (variables.containsKey(name)) {
                    return variables.get(name);
                } else {
                    throw new RuntimeException("Undefined variable: " + name);
                }
            default:
                throw new RuntimeException("Unexpected terminal: " + token.getType());
        }
    }
    
    @Override
    public Double visitProgram(ProgramNode node) {
        Double result = null;
        // Execute all statements in the program
        for (ASTNode child : node.getChildren()) {
            result = child.accept(this);
        }
        return result; // Return result of last statement
    }
    
    @Override
    public Double visitStatement(StatementNode node) {
        // A statement is either an assignment or an expression
        return node.getChild(0).accept(this);
    }
    
    @Override
    public Double visitAssignment(AssignmentNode node) {
        // Assignment: IDENTIFIER ASSIGN Expression SEMICOLON
        String varName = ((TerminalNode) node.getChild(0)).getToken().getValue();
        Double value = node.getChild(2).accept(this); // Skip ASSIGN token
        variables.put(varName, value);
        return value;
    }
    
    @Override
    public Double visitExpression(ExpressionNode node) {
        int production = node.getProductionNumber();
        
        switch (production) {
            case 0: // <Term> PLUS <Expression>
                return node.getChild(0).accept(this) + node.getChild(2).accept(this);
            case 1: // <Term> MINUS <Expression>
                return node.getChild(0).accept(this) - node.getChild(2).accept(this);
            case 2: // <Term>
                return node.getChild(0).accept(this);
            default:
                throw new RuntimeException("Unknown Expression production: " + production);
        }
    }
    
    @Override
    public Double visitTerm(TermNode node) {
        int production = node.getProductionNumber();
        
        switch (production) {
            case 0: // <Factor> MULTIPLY <Term>
                return node.getChild(0).accept(this) * node.getChild(2).accept(this);
            case 1: // <Factor> DIVIDE <Term>
                double divisor = node.getChild(2).accept(this);
                if (divisor == 0.0) {
                    throw new RuntimeException("Division by zero");
                }
                return node.getChild(0).accept(this) / divisor;
            case 2: // <Factor>
                return node.getChild(0).accept(this);
            default:
                throw new RuntimeException("Unknown Term production: " + production);
        }
    }
    
    @Override
    public Double visitFactor(FactorNode node) {
        int production = node.getProductionNumber();
        
        switch (production) {
            case 0: // <Base> POWER <Factor>
                double base = node.getChild(0).accept(this);
                double exponent = node.getChild(2).accept(this);
                return Math.pow(base, exponent);
            case 1: // <Base>
                return node.getChild(0).accept(this);
            default:
                throw new RuntimeException("Unknown Factor production: " + production);
        }
    }
    
    @Override
    public Double visitBase(BaseNode node) {
        int production = node.getProductionNumber();
        
        switch (production) {
            case 0: // NUMBER
                return node.getChild(0).accept(this);
            case 1: // IDENTIFIER
                return node.getChild(0).accept(this);
            case 2: // <FunctionCall>
                return node.getChild(0).accept(this);
            case 3: // LPAREN <Expression> RPAREN
                return node.getChild(1).accept(this);
            case 4: // MINUS <Base> (unary minus)
                return -node.getChild(1).accept(this);
            default:
                throw new RuntimeException("Unknown Base production: " + production);
        }
    }
    
    @Override
    public Double visitFunctioncall(FunctioncallNode node) {
        // IDENTIFIER LPAREN <ArgumentList> RPAREN
        String functionName = ((TerminalNode) node.getChild(0)).getToken().getValue();
        
        if (!functions.containsKey(functionName)) {
            throw new RuntimeException("Unknown function: " + functionName);
        }
        
        // Get arguments from argument list
        double[] args = getArgumentsFromList(node.getChild(2));
        
        try {
            return functions.get(functionName).apply(args);
        } catch (Exception e) {
            throw new RuntimeException("Error calling function " + functionName + ": " + e.getMessage());
        }
    }
    
    @Override
    public Double visitArgumentlist(ArgumentlistNode node) {
        // This method shouldn't be called directly, use getArgumentsFromList instead
        throw new RuntimeException("visitArgumentlist should not be called directly");
    }
    
    /**
     * Helper method to extract arguments from an argument list node.
     */
    private double[] getArgumentsFromList(ASTNode argumentListNode) {
        if (argumentListNode.getChildren().size() == 0) {
            // Empty argument list
            return new double[0];
        }
        
        java.util.List<Double> args = new java.util.ArrayList<>();
        collectArguments(argumentListNode, args);
        return args.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    /**
     * Recursively collect arguments from the argument list.
     */
    private void collectArguments(ASTNode node, java.util.List<Double> args) {
        if (node.getChildren().size() == 0) {
            return; // Empty argument list
        }
        
        ArgumentlistNode argList = (ArgumentlistNode) node;
        int production = argList.getProductionNumber();
        
        switch (production) {
            case 0: // <Expression>
                args.add(argList.getChild(0).accept(this));
                break;
            case 1: // <Expression> COMMA <ArgumentList>
                args.add(argList.getChild(0).accept(this));
                collectArguments(argList.getChild(2), args);
                break;
            case 2: // Empty production
                break;
            default:
                throw new RuntimeException("Unknown ArgumentList production: " + production);
        }
    }
    
    /**
     * Evaluates a mathematical expression string.
     */
    public double evaluate(String expression) {
        try {
            // This would require integrating with the lexer and parser
            // For now, this is a placeholder that shows the intended usage
            throw new UnsupportedOperationException(
                "Direct string evaluation requires integration with the generated lexer and parser. " +
                "Use the generated parser to create an AST, then call accept(this) on the root node."
            );
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating expression: " + expression, e);
        }
    }
    
    /**
     * Formats a number for display, removing unnecessary decimal places.
     */
    public static String formatResult(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.format("%.0f", value);
        } else {
            return String.format("%.6f", value).replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }
    
    /**
     * Prints the current state of variables.
     */
    public void printVariables() {
        System.out.println("Variables:");
        variables.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> System.out.println("  " + entry.getKey() + " = " + formatResult(entry.getValue())));
    }
    
    /**
     * Prints available functions.
     */
    public void printFunctions() {
        System.out.println("Available functions:");
        functions.keySet().stream()
            .sorted()
            .forEach(name -> System.out.println("  " + name + "()"));
    }
}