package com.example.expression.ast;

/**
 * AST node representing a binary operation (e.g., +, -, *, /, %, ^).
 */
public class BinaryOperationNode implements ASTNode {
    private final ASTNode left;
    private final String operator;
    private final ASTNode right;
    
    public BinaryOperationNode(ASTNode left, String operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    public ASTNode getLeft() {
        return left;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public ASTNode getRight() {
        return right;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBinaryOperation(this);
    }
    
    @Override
    public String toString() {
        return String.format("(%s %s %s)", left, operator, right);
    }
}