package com.example.expression.printer;

import com.example.expression.ast.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PrettyPrinterTest {
    
    private PrettyPrinter printer;
    private PrettyPrinter printerWithParens;
    
    @BeforeEach
    void setUp() {
        printer = new PrettyPrinter();
        printerWithParens = new PrettyPrinter(true);
    }
    
    @Test
    void testNumberNode() {
        NumberNode intNode = new NumberNode(42);
        NumberNode doubleNode = new NumberNode(3.14159);
        
        assertEquals("42", printer.format(intNode));
        assertEquals("3.14159", printer.format(doubleNode));
    }
    
    @Test
    void testIdentifierNode() {
        IdentifierNode node = new IdentifierNode("variable");
        assertEquals("variable", printer.format(node));
    }
    
    @Test
    void testConstantNode() {
        ConstantNode piNode = new ConstantNode("pi");
        ConstantNode eNode = new ConstantNode("e");
        
        assertEquals("pi", printer.format(piNode));
        assertEquals("e", printer.format(eNode));
    }
    
    @Test
    void testBinaryOperationNode() {
        BinaryOperationNode addition = new BinaryOperationNode(
            new NumberNode(2),
            "+",
            new NumberNode(3)
        );
        
        assertEquals("2 + 3", printer.format(addition));
        assertEquals("(2 + 3)", printerWithParens.format(addition));
    }
    
    @Test
    void testUnaryOperationNode() {
        UnaryOperationNode negation = new UnaryOperationNode(
            "-",
            new NumberNode(5)
        );
        
        assertEquals("-5", printer.format(negation));
    }
    
    @Test
    void testFunctionCallNode() {
        // sin(x)
        FunctionCallNode sinNode = new FunctionCallNode("sin", Arrays.asList(
            new IdentifierNode("x")
        ));
        assertEquals("sin(x)", printer.format(sinNode));
        
        // log(10, 100)
        FunctionCallNode logNode = new FunctionCallNode("log", Arrays.asList(
            new NumberNode(10),
            new NumberNode(100)
        ));
        assertEquals("log(10, 100)", printer.format(logNode));
    }
    
    @Test
    void testComplexExpression() {
        // sqrt(x^2 + y^2)
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
        
        assertEquals("sqrt(x ^ 2 + y ^ 2)", printer.format(expression));
    }
    
    @Test
    void testNestedExpression() {
        // (a + b) * (c - d)
        ASTNode expression = new BinaryOperationNode(
            new BinaryOperationNode(
                new IdentifierNode("a"),
                "+",
                new IdentifierNode("b")
            ),
            "*",
            new BinaryOperationNode(
                new IdentifierNode("c"),
                "-",
                new IdentifierNode("d")
            )
        );
        
        assertEquals("a + b * c - d", printer.format(expression));
        assertEquals("((a + b) * (c - d))", printerWithParens.format(expression));
    }
}