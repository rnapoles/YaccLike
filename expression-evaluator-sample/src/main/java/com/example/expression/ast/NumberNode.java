package com.example.expression.ast;

/**
 * AST node representing a numeric literal.
 */
public class NumberNode implements ASTNode {
    private final double value;
    
    public NumberNode(double value) {
        this.value = value;
    }
    
    public NumberNode(String valueStr) {
        this.value = Double.parseDouble(valueStr);
    }
    
    public double getValue() {
        return value;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitNumber(this);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}