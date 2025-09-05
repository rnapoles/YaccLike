package com.generated.parser;

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
     * Visits a Program node.
     *
     * @param node the Program node to visit
     * @return the result of visiting the node
     */
    T visitProgram(ProgramNode node);
    
        /**
     * Visits a Statement node.
     *
     * @param node the Statement node to visit
     * @return the result of visiting the node
     */
    T visitStatement(StatementNode node);
    
        /**
     * Visits a Assignment node.
     *
     * @param node the Assignment node to visit
     * @return the result of visiting the node
     */
    T visitAssignment(AssignmentNode node);
    
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
     * Visits a Functioncall node.
     *
     * @param node the Functioncall node to visit
     * @return the result of visiting the node
     */
    T visitFunctioncall(FunctioncallNode node);
    
        /**
     * Visits a Argumentlist node.
     *
     * @param node the Argumentlist node to visit
     * @return the result of visiting the node
     */
    T visitArgumentlist(ArgumentlistNode node);
    
        
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