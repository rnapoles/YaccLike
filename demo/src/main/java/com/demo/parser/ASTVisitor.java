package com.demo.parser;

/**
 * Visitor interface for traversing AST nodes in the GeneratedGrammar grammar.
 * Implements the Visitor pattern for type-safe AST traversal.
 *
 * @param <T> the return type of visit methods
 */
public interface ASTVisitor<T> {
    
    /**
     * Visits a terminal node.
     *
     * @param node the terminal node to visit
     * @return the result of visiting the node
     */
    T visitTerminal(TerminalNode node);
    
        /**
     * Visits a Expression node.
     *
     * @param node the Expression node to visit
     * @return the result of visiting the node
     */
    T visitExpression(ExpressionNode node);
    
        /**
     * Visits a Term node.
     *
     * @param node the Term node to visit
     * @return the result of visiting the node
     */
    T visitTerm(TermNode node);
    
        /**
     * Visits a Factor node.
     *
     * @param node the Factor node to visit
     * @return the result of visiting the node
     */
    T visitFactor(FactorNode node);
    
        /**
     * Visits a Base node.
     *
     * @param node the Base node to visit
     * @return the result of visiting the node
     */
    T visitBase(BaseNode node);
    
        /**
     * Visits a Unaryfunction node.
     *
     * @param node the Unaryfunction node to visit
     * @return the result of visiting the node
     */
    T visitUnaryfunction(UnaryfunctionNode node);
    
        /**
     * Visits a Binaryfunction node.
     *
     * @param node the Binaryfunction node to visit
     * @return the result of visiting the node
     */
    T visitBinaryfunction(BinaryfunctionNode node);
    
        
    /**
     * Default implementation that visits all children and returns null.
     * Can be used as a base for visitors that don't need return values.
     */
    default T visitChildren(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        return null;
    }
}