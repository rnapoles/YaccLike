package com.example.expression.evaluator;

import com.example.expression.ast.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Visitor implementation that evaluates mathematical expressions.
 * Supports arithmetic operations, mathematical functions, and constants.
 */
public class ExpressionEvaluator implements ASTVisitor<Double> {
    private final Map<String, Double> variables;
    
    public ExpressionEvaluator() {
        this.variables = new HashMap<>();
    }
    
    public ExpressionEvaluator(Map<String, Double> variables) {
        this.variables = new HashMap<>(variables);
    }
    
    /**
     * Set a variable value for evaluation.
     */
    public void setVariable(String name, double value) {
        variables.put(name, value);
    }
    
    /**
     * Get a variable value.
     */
    public Double getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Evaluate an expression tree.
     */
    public double evaluate(ASTNode expression) {
        return expression.accept(this);
    }
    
    @Override
    public Double visitBinaryOperation(BinaryOperationNode node) {
        double left = node.getLeft().accept(this);
        double right = node.getRight().accept(this);
        
        switch (node.getOperator()) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                if (right == 0.0) {
                    throw new ArithmeticException("Division by zero");
                }
                return left / right;
            case "%":
                if (right == 0.0) {
                    throw new ArithmeticException("Modulo by zero");
                }
                return left % right;
            case "^":
                return Math.pow(left, right);
            default:
                throw new UnsupportedOperationException("Unknown binary operator: " + node.getOperator());
        }
    }
    
    @Override
    public Double visitUnaryOperation(UnaryOperationNode node) {
        double operand = node.getOperand().accept(this);
        
        switch (node.getOperator()) {
            case "-":
                return -operand;
            case "+":
                return operand;
            default:
                throw new UnsupportedOperationException("Unknown unary operator: " + node.getOperator());
        }
    }
    
    @Override
    public Double visitNumber(NumberNode node) {
        return node.getValue();
    }
    
    @Override
    public Double visitIdentifier(IdentifierNode node) {
        Double value = variables.get(node.getName());
        if (value == null) {
            throw new IllegalArgumentException("Undefined variable: " + node.getName());
        }
        return value;
    }
    
    @Override
    public Double visitConstant(ConstantNode node) {
        return node.getValue();
    }
    
    @Override
    public Double visitFunctionCall(FunctionCallNode node) {
        String functionName = node.getFunctionName().toLowerCase();
        
        switch (functionName) {
            case "sin":
                validateArgumentCount(node, 1);
                return Math.sin(node.getArguments().get(0).accept(this));
                
            case "cos":
                validateArgumentCount(node, 1);
                return Math.cos(node.getArguments().get(0).accept(this));
                
            case "tan":
                validateArgumentCount(node, 1);
                return Math.tan(node.getArguments().get(0).accept(this));
                
            case "ln":
                validateArgumentCount(node, 1);
                double arg = node.getArguments().get(0).accept(this);
                if (arg <= 0) {
                    throw new ArithmeticException("Natural logarithm of non-positive number");
                }
                return Math.log(arg);
                
            case "log":
                if (node.getArgumentCount() == 1) {
                    // log10
                    double arg1 = node.getArguments().get(0).accept(this);
                    if (arg1 <= 0) {
                        throw new ArithmeticException("Logarithm of non-positive number");
                    }
                    return Math.log10(arg1);
                } else if (node.getArgumentCount() == 2) {
                    // log(base, value)
                    double base = node.getArguments().get(0).accept(this);
                    double value = node.getArguments().get(1).accept(this);
                    if (base <= 0 || base == 1 || value <= 0) {
                        throw new ArithmeticException("Invalid logarithm arguments");
                    }
                    return Math.log(value) / Math.log(base);
                } else {
                    throw new IllegalArgumentException("log function expects 1 or 2 arguments, got " + node.getArgumentCount());
                }
                
            case "sqrt":
                validateArgumentCount(node, 1);
                double sqrtArg = node.getArguments().get(0).accept(this);
                if (sqrtArg < 0) {
                    throw new ArithmeticException("Square root of negative number");
                }
                return Math.sqrt(sqrtArg);
                
            case "abs":
                validateArgumentCount(node, 1);
                return Math.abs(node.getArguments().get(0).accept(this));
                
            default:
                throw new UnsupportedOperationException("Unknown function: " + functionName);
        }
    }
    
    private void validateArgumentCount(FunctionCallNode node, int expectedCount) {
        if (node.getArgumentCount() != expectedCount) {
            throw new IllegalArgumentException(
                String.format("Function %s expects %d argument(s), got %d", 
                    node.getFunctionName(), expectedCount, node.getArgumentCount()));
        }
    }
}