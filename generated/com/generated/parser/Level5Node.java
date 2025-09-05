package com.generated.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AST node representing the non-terminal Level5.
 * Generated from production rules in the grammar.
 */
public class Level5Node extends ASTNode {
    private final List<ASTNode> children;
    private final int productionNumber;
    
    /**
     * Creates a new Level5 node.
     *
     * @param productionNumber the production rule number used to create this node
     * @param children the child nodes
     */
    public Level5Node(int productionNumber, List<ASTNode> children) {
        this.productionNumber = productionNumber;
        this.children = new ArrayList<>(children);
        
        // Set parent references for all children
        for (ASTNode child : this.children) {
            if (child != null) {
                child.setParent(this);
            }
        }
    }
    
    /**
     * Returns the production number used to create this node.
     */
    public int getProductionNumber() {
        return productionNumber;
    }
    
    /**
     * Returns a specific child by index.
     *
     * @param index the child index
     * @return the child node
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public ASTNode getChild(int index) {
        return children.get(index);
    }
    
    /**
     * Returns the number of children.
     */
    public int getChildCount() {
        return children.size();
    }
    
    @Override
    public String getNodeType() {
        return "Level5";
    }
    
    @Override
    public List<ASTNode> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    @Override
    public boolean isTerminal() {
        return false;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLevel5(this);
    }
    
    @Override
    public String toString() {
        return "Level5(prod=" + productionNumber + ", children=" + children.size() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Level5Node other = (Level5Node) obj;
        return productionNumber == other.productionNumber && 
               children.equals(other.children);
    }
    
    @Override
    public int hashCode() {
        return 31 * productionNumber + children.hashCode();
    }
}