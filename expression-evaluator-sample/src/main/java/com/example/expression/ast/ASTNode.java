package com.example.expression.ast;

/**
 * Base interface for all AST nodes in the expression evaluator.
 * Implements the visitor pattern to allow different operations on the AST.
 */
public interface ASTNode {
    /**
     * Accept a visitor and delegate to the appropriate visit method.
     * 
     * @param visitor the visitor to accept
     * @param <T> the return type of the visitor
     * @return the result of the visitor operation
     */
    <T> T accept(ASTVisitor<T> visitor);
}