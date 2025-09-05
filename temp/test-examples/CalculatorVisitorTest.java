package com.parsergenerator.examples;

import com.generated.parser.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the CalculatorVisitor demonstrating AST evaluation.
 */
public class CalculatorVisitorTest {
    
    private CalculatorVisitor calculator;
    
    @BeforeEach
    void setUp() {
        calculator = new CalculatorVisitor();
    }
    
    @Test
    void testBasicArithmetic() {
        // Test basic arithmetic operations by creating AST nodes manually
        // This simulates what the generated parser would create
        
        // Create terminal nodes for numbers
        Token num2 = new Token(TokenType.NUMBER, "2", new Position(1, 1));
        Token num3 = new Token(TokenType.NUMBER, "3", new Position(1, 5));
        
        TerminalNode term2 = new TerminalNode(num2);
        TerminalNode term3 = new TerminalNode(num3);
        
        // Test terminal evaluation
        assertEquals(2.0, calculator.visitTerminal(term2), 0.001);
        assertEquals(3.0, calculator.visitTerminal(term3), 0.001);
    }
    
    @Test
    void testVariableOperations() {
        // Test variable setting and getting
        calculator.setVariable("x", 10.0);
        calculator.setVariable("y", 5.0);
        
        assertEquals(10.0, calculator.getVariable("x"), 0.001);
        assertEquals(5.0, calculator.getVariable("y"), 0.001);
        assertEquals(0.0, calculator.getVariable("undefined"), 0.001);
        
        // Test built-in constants
        assertEquals(Math.PI, calculator.getVariable("PI"), 0.001);
        assertEquals(Math.E, calculator.getVariable("E"), 0.001);
    }
    
    @Test
    void testVariableIdentifierEvaluation() {
        // Test evaluating identifier terminals
        calculator.setVariable("myVar", 42.0);
        
        Token idToken = new Token(TokenType.IDENTIFIER, "myVar", new Position(1, 1));
        TerminalNode idNode = new TerminalNode(idToken);
        
        assertEquals(42.0, calculator.visitTerminal(idNode), 0.001);
    }
    
    @Test
    void testUndefinedVariableError() {
        Token undefinedToken = new Token(TokenType.IDENTIFIER, "undefined", new Position(1, 1));
        TerminalNode undefinedNode = new TerminalNode(undefinedToken);
        
        assertThrows(RuntimeException.class, () -> calculator.visitTerminal(undefinedNode));
    }
    
    @Test
    void testMathematicalFunctions() {
        // Test that mathematical functions are available
        assertTrue(calculator.getVariables().containsKey("PI"));
        assertTrue(calculator.getVariables().containsKey("E"));
        
        // Test constants
        assertEquals(Math.PI, calculator.getVariable("PI"), 0.001);
        assertEquals(Math.E, calculator.getVariable("E"), 0.001);
    }
    
    @Test
    void testExpressionEvaluation() {
        // Create a simple expression AST: 2 + 3
        // This demonstrates how the visitor would work with generated AST nodes
        
        Token num2 = new Token(TokenType.NUMBER, "2", new Position(1, 1));
        Token plus = new Token(TokenType.PLUS, "+", new Position(1, 3));
        Token num3 = new Token(TokenType.NUMBER, "3", new Position(1, 5));
        
        TerminalNode term2 = new TerminalNode(num2);
        TerminalNode termPlus = new TerminalNode(plus);
        TerminalNode term3 = new TerminalNode(num3);
        
        // Create Base nodes for the numbers
        BaseNode base2 = new BaseNode(0, List.of(term2)); // NUMBER production
        BaseNode base3 = new BaseNode(0, List.of(term3)); // NUMBER production
        
        // Create Factor nodes
        FactorNode factor2 = new FactorNode(1, List.of(base2)); // <Base> production
        FactorNode factor3 = new FactorNode(1, List.of(base3)); // <Base> production
        
        // Create Term nodes
        TermNode termNode2 = new TermNode(2, List.of(factor2)); // <Factor> production
        TermNode termNode3 = new TermNode(2, List.of(factor3)); // <Factor> production
        
        // Create Expression node for addition: <Term> PLUS <Expression>
        ExpressionNode expr3 = new ExpressionNode(2, List.of(termNode3)); // <Term> production
        ExpressionNode addExpr = new ExpressionNode(0, List.of(termNode2, termPlus, expr3));
        
        // Evaluate the expression
        Double result = calculator.visitExpression(addExpr);
        assertEquals(5.0, result, 0.001);
    }
    
    @Test
    void testUnaryMinusEvaluation() {
        // Test unary minus: -5
        Token minus = new Token(TokenType.MINUS, "-", new Position(1, 1));
        Token num5 = new Token(TokenType.NUMBER, "5", new Position(1, 2));
        
        TerminalNode minusNode = new TerminalNode(minus);
        TerminalNode num5Node = new TerminalNode(num5);
        
        // Create Base node for the number 5
        BaseNode base5 = new BaseNode(0, List.of(num5Node)); // NUMBER production
        
        // Create Base node for unary minus: MINUS <Base>
        BaseNode unaryMinus = new BaseNode(4, List.of(minusNode, base5)); // MINUS <Base> production
        
        Double result = calculator.visitBase(unaryMinus);
        assertEquals(-5.0, result, 0.001);
    }
    
    @Test
    void testAssignmentEvaluation() {
        // Test assignment: x = 42;
        Token idToken = new Token(TokenType.IDENTIFIER, "x", new Position(1, 1));
        Token assignToken = new Token(TokenType.ASSIGN, "=", new Position(1, 3));
        Token num42 = new Token(TokenType.NUMBER, "42", new Position(1, 5));
        Token semicolon = new Token(TokenType.SEMICOLON, ";", new Position(1, 7));
        
        TerminalNode idNode = new TerminalNode(idToken);
        TerminalNode assignNode = new TerminalNode(assignToken);
        TerminalNode num42Node = new TerminalNode(num42);
        TerminalNode semicolonNode = new TerminalNode(semicolon);
        
        // Create expression for 42
        BaseNode base42 = new BaseNode(0, List.of(num42Node));
        FactorNode factor42 = new FactorNode(1, List.of(base42));
        TermNode term42 = new TermNode(2, List.of(factor42));
        ExpressionNode expr42 = new ExpressionNode(2, List.of(term42));
        
        // Create assignment node: IDENTIFIER ASSIGN Expression SEMICOLON
        AssignmentNode assignment = new AssignmentNode(0, 
            List.of(idNode, assignNode, expr42, semicolonNode));
        
        Double result = calculator.visitAssignment(assignment);
        assertEquals(42.0, result, 0.001);
        assertEquals(42.0, calculator.getVariable("x"), 0.001);
    }
    
    @Test
    void testPowerOperationEvaluation() {
        // Test power operation: 2 ^ 3
        Token num2 = new Token(TokenType.NUMBER, "2", new Position(1, 1));
        Token power = new Token(TokenType.POWER, "^", new Position(1, 3));
        Token num3 = new Token(TokenType.NUMBER, "3", new Position(1, 5));
        
        TerminalNode term2 = new TerminalNode(num2);
        TerminalNode powerNode = new TerminalNode(power);
        TerminalNode term3 = new TerminalNode(num3);
        
        // Create Base nodes
        BaseNode base2 = new BaseNode(0, List.of(term2));
        BaseNode base3 = new BaseNode(0, List.of(term3));
        
        // Create Factor nodes
        FactorNode factor3 = new FactorNode(1, List.of(base3)); // <Base> production
        FactorNode powerFactor = new FactorNode(0, List.of(base2, powerNode, factor3)); // <Base> POWER <Factor>
        
        Double result = calculator.visitFactor(powerFactor);
        assertEquals(8.0, result, 0.001); // 2^3 = 8
    }
    
    @Test
    void testDivisionByZeroError() {
        // Test division by zero error handling
        Token num5 = new Token(TokenType.NUMBER, "5", new Position(1, 1));
        Token divide = new Token(TokenType.DIVIDE, "/", new Position(1, 3));
        Token num0 = new Token(TokenType.NUMBER, "0", new Position(1, 5));
        
        TerminalNode term5 = new TerminalNode(num5);
        TerminalNode divideNode = new TerminalNode(divide);
        TerminalNode term0 = new TerminalNode(num0);
        
        // Create the division expression
        BaseNode base5 = new BaseNode(0, List.of(term5));
        BaseNode base0 = new BaseNode(0, List.of(term0));
        FactorNode factor5 = new FactorNode(1, List.of(base5));
        FactorNode factor0 = new FactorNode(1, List.of(base0));
        TermNode termNode0 = new TermNode(2, List.of(factor0));
        
        // Create Term node for division: <Factor> DIVIDE <Term>
        TermNode divisionTerm = new TermNode(1, List.of(factor5, divideNode, termNode0));
        
        assertThrows(RuntimeException.class, () -> calculator.visitTerm(divisionTerm));
    }
    
    @Test
    void testResultFormatting() {
        // Test number formatting
        assertEquals("5", CalculatorVisitor.formatResult(5.0));
        assertEquals("5.5", CalculatorVisitor.formatResult(5.5));
        assertEquals("3.14159", CalculatorVisitor.formatResult(3.141592653589793));
        assertEquals("0", CalculatorVisitor.formatResult(0.0));
        assertEquals("-5", CalculatorVisitor.formatResult(-5.0));
    }
    
    @Test
    void testVariableManagement() {
        // Test variable operations
        assertTrue(calculator.getVariables().isEmpty() || 
                  calculator.getVariables().size() == 2); // PI and E are pre-defined
        
        calculator.setVariable("test", 123.45);
        assertTrue(calculator.getVariables().containsKey("test"));
        assertEquals(123.45, calculator.getVariable("test"), 0.001);
        
        // Test that constants are preserved
        assertEquals(Math.PI, calculator.getVariable("PI"), 0.001);
        assertEquals(Math.E, calculator.getVariable("E"), 0.001);
    }
}