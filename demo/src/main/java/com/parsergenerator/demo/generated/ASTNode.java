package com.parsergenerator.demo.generated;

import java.util.List;

/**
 * Base class for all AST nodes in the  grammar.
 * Provides common functionality for AST traversal and manipulation.
 */
public abstract class ASTNode {
    private ASTNode parent;
    
    /**
     * Returns the type of this AST node.
     */
    public abstract String getNodeType();
    
    /**
     * Returns the children of this node.
     */
    public abstract List<ASTNode> getChildren();
    
    /**
     * Checks if this is a terminal node.
     */
    public abstract boolean isTerminal();
    
    /**
     * Accepts a visitor for traversing the AST.
     */
    public abstract <T> T accept(ASTVisitor<T> visitor);
    
    /**
     * Returns the parent node, or null if this is the root.
     */
    public ASTNode getParent() {
        return parent;
    }
    
    /**
     * Sets the parent node.
     */
    public void setParent(ASTNode parent) {
        this.parent = parent;
    }
    
    /**
     * Returns true if this is the root node.
     */
    public boolean isRoot() {
        return parent == null;
    }
    
    /**
     * Returns the depth of this node in the tree.
     */
    public int getDepth() {
        int depth = 0;
        ASTNode current = parent;
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }
    
    /**
     * Returns a string representation of the subtree rooted at this node.
     */
    public String toTreeString() {
        return toTreeString(0);
    }
    
    /**
     * Returns a string representation with indentation.
     */
    protected String toTreeString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        sb.append(getNodeType());
        if (isTerminal()) {
            sb.append(": ").append(toString());
        }
        sb.append("\n");
        
        for (ASTNode child : getChildren()) {
            sb.append(child.toTreeString(indent + 1));
        }
        
        return sb.toString();
    }
}