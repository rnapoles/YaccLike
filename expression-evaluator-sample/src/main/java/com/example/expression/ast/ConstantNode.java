package com.example.expression.ast;

/**
 * AST node representing mathematical constants (pi, e).
 */
public class ConstantNode implements ASTNode {
    private final String name;
    private final double value;
    
    public ConstantNode(String name) {
        this.name = name;
        this.value = getConstantValue(name);
    }
    
    public String getName() {
        return name;
    }
    
    public double getValue() {
        return value;
    }
    
    private static double getConstantValue(String name) {
        switch (name.toLowerCase()) {
            case "pi":
                return Math.PI;
            case "e":
                return Math.E;
            default:
                throw new IllegalArgumentException("Unknown constant: " + name);
        }
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitConstant(this);
    }
    
    @Override
    public String toString() {
        return name;
    }
}