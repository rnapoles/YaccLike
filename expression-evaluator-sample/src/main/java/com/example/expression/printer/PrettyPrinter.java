package com.example.expression.printer;

import com.example.expression.ast.*;

/**
 * Visitor implementation that creates a formatted string representation of expressions.
 */
public class PrettyPrinter implements ASTVisitor<String> {
    private final boolean useParentheses;
    
    public PrettyPrinter() {
        this.useParentheses = false;
    }
    
    public PrettyPrinter(boolean useParentheses) {
        this.useParentheses = useParentheses;
    }
    
    /**
     * Format an expression tree as a string.
     */
    public String format(ASTNode expression) {
        return expression.accept(this);
    }
    
    @Override
    public String visitBinaryOperation(BinaryOperationNode node) {
        String left = node.getLeft().accept(this);
        String right = node.getRight().accept(this);
        String result = left + " " + node.getOperator() + " " + right;
        
        if (useParentheses) {
            result = "(" + result + ")";
        }
        
        return result;
    }
    
    @Override
    public String visitUnaryOperation(UnaryOperationNode node) {
        String operand = node.getOperand().accept(this);
        return node.getOperator() + operand;
    }
    
    @Override
    public String visitNumber(NumberNode node) {
        double value = node.getValue();
        // Format nicely - avoid unnecessary decimal places
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        } else {
            return String.valueOf(value);
        }
    }
    
    @Override
    public String visitIdentifier(IdentifierNode node) {
        return node.getName();
    }
    
    @Override
    public String visitConstant(ConstantNode node) {
        return node.getName();
    }
    
    @Override
    public String visitFunctionCall(FunctionCallNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.getFunctionName()).append("(");
        
        for (int i = 0; i < node.getArguments().size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(node.getArguments().get(i).accept(this));
        }
        
        sb.append(")");
        return sb.toString();
    }
}