package com.example.expression.ast;

/**
 * Visitor interface for traversing and operating on AST nodes.
 * 
 * @param <T> the return type of visitor methods
 */
public interface ASTVisitor<T> {
    T visitBinaryOperation(BinaryOperationNode node);
    T visitUnaryOperation(UnaryOperationNode node);
    T visitNumber(NumberNode node);
    T visitIdentifier(IdentifierNode node);
    T visitConstant(ConstantNode node);
    T visitFunctionCall(FunctionCallNode node);
}