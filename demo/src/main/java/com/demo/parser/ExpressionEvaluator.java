package com.demo.parser;

import com.parsergenerator.demo.generated.*;

/**
 * Expression evaluator using the visitor pattern for AST traversal.
 * Evaluates mathematical expressions by visiting AST nodes and computing results.
 */
public class ExpressionEvaluator implements com.parsergenerator.demo.generated.ASTVisitor<Double> {
    
    private final VariableContext variableContext;
    
    /**
     * Creates a new ExpressionEvaluator with a default variable context.
     */
    public ExpressionEvaluator() {
        this.variableContext = VariableContext.createDefault();
    }
    
    /**
     * Creates a new ExpressionEvaluator with the specified variable context.
     * 
     * @param variableContext the variable context to use for variable resolution
     */
    public ExpressionEvaluator(VariableContext variableContext) {
        this.variableContext = variableContext != null ? variableContext : VariableContext.createDefault();
    }
    
    /**
     * Evaluates an expression AST node.
     * 
     * @param node the root AST node to evaluate
     * @return the numeric result of the expression
     * @throws EvaluationException if evaluation fails
     */
    public double evaluate(com.parsergenerator.demo.generated.ASTNode node) throws EvaluationException {
        try {
            return node.accept(this);
        } catch (Exception e) {
            if (e instanceof EvaluationException) {
                throw e;
            }
            throw new EvaluationException("Failed to evaluate expression: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Double visitTerminal(com.parsergenerator.demo.generated.TerminalNode node) {
        com.parsergenerator.demo.generated.Token token = node.getToken();
        com.parsergenerator.demo.generated.TokenType type = token.getType();
        String value = token.getValue();
        
        switch (type) {
            case NUMBER:
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new RuntimeException(new EvaluationException("Invalid number format: " + value));
                }
                
            case IDENTIFIER:
                try {
                    return variableContext.getVariable(value);
                } catch (VariableNotFoundException e) {
                    throw new RuntimeException(new EvaluationException("Undefined variable: " + value));
                }
                
            case PI:
                return Math.PI;
                
            case E:
                return Math.E;
                
            default:
                throw new RuntimeException(new EvaluationException("Cannot evaluate terminal token: " + type));
        }
    }
    
    @Override
    public Double visitExpression(com.parsergenerator.demo.generated.ExpressionNode node) {
        int productionNumber = node.getProductionNumber();
        
        switch (productionNumber) {
            case 0: // <Expression> ::= <Term> PLUS <Expression>
                return node.getChild(0).accept(this) + node.getChild(2).accept(this);
                
            case 1: // <Expression> ::= <Term> MINUS <Expression>
                return node.getChild(0).accept(this) - node.getChild(2).accept(this);
                
            case 2: // <Expression> ::= <Term>
                return node.getChild(0).accept(this);
                
            default:
                throw new RuntimeException(new EvaluationException("Unknown Expression production: " + productionNumber));
        }
    }
    
    @Override
    public Double visitTerm(com.parsergenerator.demo.generated.TermNode node) {
        int productionNumber = node.getProductionNumber();
        
        switch (productionNumber) {
            case 3: // <Term> ::= <Factor> MULTIPLY <Term>
                return node.getChild(0).accept(this) * node.getChild(2).accept(this);
                
            case 4: // <Term> ::= <Factor> DIVIDE <Term>
                double divisor = node.getChild(2).accept(this);
                if (divisor == 0.0) {
                    throw new RuntimeException(new EvaluationException("Division by zero"));
                }
                return node.getChild(0).accept(this) / divisor;
                
            case 5: // <Term> ::= <Factor> MODULO <Term>
                double modDivisor = node.getChild(2).accept(this);
                if (modDivisor == 0.0) {
                    throw new RuntimeException(new EvaluationException("Modulo by zero"));
                }
                return node.getChild(0).accept(this) % modDivisor;
                
            case 6: // <Term> ::= <Factor>
                return node.getChild(0).accept(this);
                
            default:
                throw new RuntimeException(new EvaluationException("Unknown Term production: " + productionNumber));
        }
    }
    
    @Override
    public Double visitFactor(com.parsergenerator.demo.generated.FactorNode node) {
        int productionNumber = node.getProductionNumber();
        
        switch (productionNumber) {
            case 7: // <Factor> ::= <Base> POWER <Factor>
                double base = node.getChild(0).accept(this);
                double exponent = node.getChild(2).accept(this);
                return Math.pow(base, exponent);
                
            case 8: // <Factor> ::= <Base>
                return node.getChild(0).accept(this);
                
            default:
                throw new RuntimeException(new EvaluationException("Unknown Factor production: " + productionNumber));
        }
    }
    
    @Override
    public Double visitBase(com.parsergenerator.demo.generated.BaseNode node) {
        int productionNumber = node.getProductionNumber();
        
        switch (productionNumber) {
            case 9:  // <Base> ::= NUMBER
            case 10: // <Base> ::= IDENTIFIER
            case 11: // <Base> ::= PI
            case 12: // <Base> ::= E
                return node.getChild(0).accept(this);
                
            case 13: // <Base> ::= <Function>
                return node.getChild(0).accept(this);
                
            case 14: // <Base> ::= LPAREN <Expression> RPAREN
                return node.getChild(1).accept(this);
                
            case 15: // <Base> ::= MINUS <Base>  -- Unary minus
                return -node.getChild(1).accept(this);
                
            default:
                throw new RuntimeException(new EvaluationException("Unknown Base production: " + productionNumber));
        }
    }
    
    @Override
    public Double visitFunctioncall(com.parsergenerator.demo.generated.FunctioncallNode node) {
        int productionNumber = node.getProductionNumber();
        
        switch (productionNumber) {
            case 16: // SIN LPAREN <Expression> RPAREN
                return evaluateUnaryFunction(MathFunction.SIN, node.getChild(2).accept(this));
                
            case 17: // COS LPAREN <Expression> RPAREN
                return evaluateUnaryFunction(MathFunction.COS, node.getChild(2).accept(this));
                
            case 18: // TAN LPAREN <Expression> RPAREN
                return evaluateUnaryFunction(MathFunction.TAN, node.getChild(2).accept(this));
                
            case 19: // LOG LPAREN <Expression> RPAREN (base 10)
                return evaluateUnaryFunction(MathFunction.LOG, node.getChild(2).accept(this));
                
            case 20: // LN LPAREN <Expression> RPAREN
                return evaluateUnaryFunction(MathFunction.LN, node.getChild(2).accept(this));
                
            case 21: // SQRT LPAREN <Expression> RPAREN
                return evaluateUnaryFunction(MathFunction.SQRT, node.getChild(2).accept(this));
                
            case 22: // ABS LPAREN <Expression> RPAREN
                return evaluateUnaryFunction(MathFunction.ABS, node.getChild(2).accept(this));
                
            default:
                throw new RuntimeException(new EvaluationException("Unknown Functioncall production: " + productionNumber));
        }
    }
    
    /**
     * Evaluates a unary mathematical function.
     * 
     * @param function the function to evaluate
     * @param argument the function argument
     * @return the result of the function evaluation
     * @throws EvaluationException if function evaluation fails
     */
    private double evaluateUnaryFunction(MathFunction function, double argument) {
        try {
            return MathFunctionLibrary.evaluate(function, argument);
        } catch (FunctionException e) {
            throw new RuntimeException(new EvaluationException("Function evaluation failed: " + e.getMessage(), e));
        }
    }
    
    /**
     * Gets the variable context used by this evaluator.
     * 
     * @return the variable context
     */
    public VariableContext getVariableContext() {
        return variableContext;
    }
}