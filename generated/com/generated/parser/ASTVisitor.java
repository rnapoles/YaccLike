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
     * Visits a Statement node.
     *
     * @param node the Statement node to visit
     * @return the result of visiting the node
     */
    T visitStatement(StatementNode node);
    
        
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