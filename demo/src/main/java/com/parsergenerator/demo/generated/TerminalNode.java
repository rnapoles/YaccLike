package com.parsergenerator.demo.generated;

import java.util.Collections;
import java.util.List;

/**
 * AST node representing a terminal symbol (leaf node).
 * Contains the actual token from the input stream.
 */
public class TerminalNode extends ASTNode {
    private final Token token;
    
    /**
     * Creates a new terminal node.
     *
     * @param token the token this node represents
     */
    public TerminalNode(Token token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        this.token = token;
    }
    
    /**
     * Returns the token represented by this node.
     */
    public Token getToken() {
        return token;
    }
    
    /**
     * Returns the token's value.
     */
    public String getValue() {
        return token.getValue();
    }
    
    /**
     * Returns the token's position in the source.
     */
    public Position getPosition() {
        return token.getPosition();
    }
    
    @Override
    public String getNodeType() {
        return token.getType().name();
    }
    
    @Override
    public List<ASTNode> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean isTerminal() {
        return true;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTerminal(this);
    }
    
    @Override
    public String toString() {
        return token.getType().name() + "(" + token.getValue() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TerminalNode other = (TerminalNode) obj;
        return token.equals(other.token);
    }
    
    @Override
    public int hashCode() {
        return token.hashCode();
    }
}