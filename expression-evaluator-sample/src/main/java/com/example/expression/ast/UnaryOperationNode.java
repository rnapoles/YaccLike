package com.example.expression.ast;

/**
 * AST node representing a unary operation (e.g., unary minus).
 */
public class UnaryOperationNode implements ASTNode {
    private final String operator;
    private final ASTNode operand;
    
    public UnaryOperationNode(String operator, ASTNode operand) {
        this.operator = operator;
        this.operand = operand;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public ASTNode getOperand() {
        return operand;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUnaryOperation(this);
    }
    
    @Override
    public String toString() {
        return String.format("(%s%s)", operator, operand);
    }
}