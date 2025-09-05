package com.example.expression.ast;

/**
 * AST node representing an identifier (variable name).
 */
public class IdentifierNode implements ASTNode {
    private final String name;
    
    public IdentifierNode(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }
    
    @Override
    public String toString() {
        return name;
    }
}