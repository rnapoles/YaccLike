package com.example.expression.evaluator;

import com.example.expression.ast.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionEvaluatorTest {
    
    private ExpressionEvaluator evaluator;
    
    @BeforeEach
    void setUp() {
        evaluator = new ExpressionEvaluator();
    }
    
    @Test
    void testNumberNode() {
        NumberNode node = new NumberNode(42.5);
        assertEquals(42.5, evaluator.evaluate(node), 1e-10);
    }
    
    @Test
    void testConstantNodes() {
        ConstantNode piNode = new ConstantNode("pi");
        ConstantNode eNode = new ConstantNode("e");
        
        assertEquals(Math.PI, evaluator.evaluate(piNode), 1e-10);
        assertEquals(Math.E, evaluator.evaluate(eNode), 1e-10);
    }
    
    @Test
    void testIdentifierNode() {
        evaluator.setVariable("x", 10.0);
        IdentifierNode node = new IdentifierNode("x");
        
        assertEquals(10.0, evaluator.evaluate(node), 1e-10);
    }
    
    @Test
    void testUndefinedVariable() {
        IdentifierNode node = new IdentifierNode("undefined");
        
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(node));
    }
    
    @Test
    void testBinaryOperations() {
        // 5 + 3 = 8
        BinaryOperationNode addition = new BinaryOperationNode(
            new NumberNode(5), "+", new NumberNode(3)
        );
        assertEquals(8.0, evaluator.evaluate(addition), 1e-10);
        
        // 5 - 3 = 2
        BinaryOperationNode subtraction = new BinaryOperationNode(
            new NumberNode(5), "-", new NumberNode(3)
        );
        assertEquals(2.0, evaluator.evaluate(subtraction), 1e-10);
        
        // 5 * 3 = 15
        BinaryOperationNode multiplication = new BinaryOperationNode(
            new NumberNode(5), "*", new NumberNode(3)
        );
        assertEquals(15.0, evaluator.evaluate(multiplication), 1e-10);
        
        // 6 / 3 = 2
        BinaryOperationNode division = new BinaryOperationNode(
            new NumberNode(6), "/", new NumberNode(3)
        );
        assertEquals(2.0, evaluator.evaluate(division), 1e-10);
        
        // 7 % 3 = 1
        BinaryOperationNode modulo = new BinaryOperationNode(
            new NumberNode(7), "%", new NumberNode(3)
        );
        assertEquals(1.0, evaluator.evaluate(modulo), 1e-10);
        
        // 2 ^ 3 = 8
        BinaryOperationNode power = new BinaryOperationNode(
            new NumberNode(2), "^", new NumberNode(3)
        );
        assertEquals(8.0, evaluator.evaluate(power), 1e-10);
    }
    
    @Test
    void testDivisionByZero() {
        BinaryOperationNode division = new BinaryOperationNode(
            new NumberNode(5), "/", new NumberNode(0)
        );
        
        assertThrows(ArithmeticException.class, () -> evaluator.evaluate(division));
    }
    
    @Test
    void testUnaryOperations() {
        // -5 = -5
        UnaryOperationNode negation = new UnaryOperationNode("-", new NumberNode(5));
        assertEquals(-5.0, evaluator.evaluate(negation), 1e-10);
        
        // +5 = 5
        UnaryOperationNode positive = new UnaryOperationNode("+", new NumberNode(5));
        assertEquals(5.0, evaluator.evaluate(positive), 1e-10);
    }
    
    @Test
    void testTrigonometricFunctions() {
        // sin(pi/2) = 1
        FunctionCallNode sinNode = new FunctionCallNode("sin", Arrays.asList(
            new BinaryOperationNode(
                new ConstantNode("pi"),
                "/",
                new NumberNode(2)
            )
        ));
        assertEquals(1.0, evaluator.evaluate(sinNode), 1e-10);
        
        // cos(0) = 1
        FunctionCallNode cosNode = new FunctionCallNode("cos", Arrays.asList(
            new NumberNode(0)
        ));
        assertEquals(1.0, evaluator.evaluate(cosNode), 1e-10);
        
        // tan(0) = 0
        FunctionCallNode tanNode = new FunctionCallNode("tan", Arrays.asList(
            new NumberNode(0)
        ));
        assertEquals(0.0, evaluator.evaluate(tanNode), 1e-10);
    }
    
    @Test
    void testLogarithmicFunctions() {
        // ln(e) = 1
        FunctionCallNode lnNode = new FunctionCallNode("ln", Arrays.asList(
            new ConstantNode("e")
        ));
        assertEquals(1.0, evaluator.evaluate(lnNode), 1e-10);
        
        // log(100) = 2 (base 10)
        FunctionCallNode logNode = new FunctionCallNode("log", Arrays.asList(
            new NumberNode(100)
        ));
        assertEquals(2.0, evaluator.evaluate(logNode), 1e-10);
        
        // log(10, 100) = 2 (base 10)
        FunctionCallNode logBaseNode = new FunctionCallNode("log", Arrays.asList(
            new NumberNode(10),
            new NumberNode(100)
        ));
        assertEquals(2.0, evaluator.evaluate(logBaseNode), 1e-10);
    }
    
    @Test
    void testOtherFunctions() {
        // sqrt(16) = 4
        FunctionCallNode sqrtNode = new FunctionCallNode("sqrt", Arrays.asList(
            new NumberNode(16)
        ));
        assertEquals(4.0, evaluator.evaluate(sqrtNode), 1e-10);
        
        // abs(-5) = 5
        FunctionCallNode absNode = new FunctionCallNode("abs", Arrays.asList(
            new NumberNode(-5)
        ));
        assertEquals(5.0, evaluator.evaluate(absNode), 1e-10);
    }
    
    @Test
    void testComplexExpression() {
        // sqrt(3^2 + 4^2) = 5 (Pythagorean theorem)
        ASTNode expression = new FunctionCallNode("sqrt", Arrays.asList(
            new BinaryOperationNode(
                new BinaryOperationNode(
                    new NumberNode(3),
                    "^",
                    new NumberNode(2)
                ),
                "+",
                new BinaryOperationNode(
                    new NumberNode(4),
                    "^",
                    new NumberNode(2)
                )
            )
        ));
        
        assertEquals(5.0, evaluator.evaluate(expression), 1e-10);
    }
    
    @Test
    void testVariablesInExpression() {
        evaluator.setVariable("x", 3.0);
        evaluator.setVariable("y", 4.0);
        
        // sqrt(x^2 + y^2) with x=3, y=4 should equal 5
        ASTNode expression = new FunctionCallNode("sqrt", Arrays.asList(
            new BinaryOperationNode(
                new BinaryOperationNode(
                    new IdentifierNode("x"),
                    "^",
                    new NumberNode(2)
                ),
                "+",
                new BinaryOperationNode(
                    new IdentifierNode("y"),
                    "^",
                    new NumberNode(2)
                )
            )
        ));
        
        assertEquals(5.0, evaluator.evaluate(expression), 1e-10);
    }
    
    @Test
    void testEvaluatorWithInitialVariables() {
        Map<String, Double> initialVars = new HashMap<>();
        initialVars.put("a", 2.0);
        initialVars.put("b", 3.0);
        
        ExpressionEvaluator evaluatorWithVars = new ExpressionEvaluator(initialVars);
        
        // a * b = 6
        BinaryOperationNode expression = new BinaryOperationNode(
            new IdentifierNode("a"),
            "*",
            new IdentifierNode("b")
        );
        
        assertEquals(6.0, evaluatorWithVars.evaluate(expression), 1e-10);
    }
}