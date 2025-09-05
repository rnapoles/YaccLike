package com.example.expression.ast;

import java.util.List;

/**
 * AST node representing a function call.
 */
public class FunctionCallNode implements ASTNode {
    private final String functionName;
    private final List<ASTNode> arguments;
    
    public FunctionCallNode(String functionName, List<ASTNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }
    
    public String getFunctionName() {
        return functionName;
    }
    
    public List<ASTNode> getArguments() {
        return arguments;
    }
    
    public int getArgumentCount() {
        return arguments.size();
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(functionName).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arguments.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}