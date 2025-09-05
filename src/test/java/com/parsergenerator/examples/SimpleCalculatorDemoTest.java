package com.parsergenerator.examples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the SimpleCalculatorDemo demonstrating expression evaluation concepts.
 */
public class SimpleCalculatorDemoTest {
    
    private SimpleCalculatorDemo calculator;
    
    @BeforeEach
    void setUp() {
        calculator = new SimpleCalculatorDemo();
    }
    
    @Test
    void testBasicArithmetic() {
        assertEquals(7.0, calculator.evaluate("3 + 4"), 0.001);
        assertEquals(-1.0, calculator.evaluate("3 - 4"), 0.001);
        assertEquals(12.0, calculator.evaluate("3 * 4"), 0.001);
        assertEquals(0.75, calculator.evaluate("3 / 4"), 0.001);
        assertEquals(81.0, calculator.evaluate("3^4"), 0.001);
    }
    
    @Test
    void testOperatorPrecedence() {
        assertEquals(14.0, calculator.evaluate("2 + 3 * 4"), 0.001);
        assertEquals(20.0, calculator.evaluate("(2 + 3) * 4"), 0.001);
        assertEquals(256.0, calculator.evaluate("2^2^3"), 0.001); // Right associative
    }
    
    @Test
    void testUnaryMinus() {
        assertEquals(-5.0, calculator.evaluate("-5"), 0.001);
        assertEquals(-2.0, calculator.evaluate("3 + -5"), 0.001);
        assertEquals(8.0, calculator.evaluate("-(-8)"), 0.001);
    }
    
    @Test
    void testVariables() {
        calculator.evaluate("x = 10");
        assertEquals(10.0, calculator.getVariable("x"), 0.001);
        
        calculator.evaluate("y = x * 2");
        assertEquals(20.0, calculator.getVariable("y"), 0.001);
        
        assertEquals(30.0, calculator.evaluate("x + y"), 0.001);
    }
    
    @Test
    void testConstants() {
        assertEquals(Math.PI, calculator.getVariable("PI"), 0.001);
        assertEquals(Math.E, calculator.getVariable("E"), 0.001);
        assertEquals(Math.PI * 2, calculator.evaluate("2 * PI"), 0.001);
    }
    
    @Test
    void testFunctions() {
        assertEquals(Math.sin(Math.PI/2), calculator.evaluate("sin(PI/2)"), 0.001);
        assertEquals(Math.cos(0), calculator.evaluate("cos(0)"), 0.001);
        assertEquals(Math.tan(Math.PI/4), calculator.evaluate("tan(PI/4)"), 0.001);
        assertEquals(4.0, calculator.evaluate("sqrt(16)"), 0.001);
        assertEquals(2.0, calculator.evaluate("log(100)"), 0.001);
        assertEquals(5.0, calculator.evaluate("abs(-5)"), 0.001);
    }
    
    @Test
    void testComplexExpressions() {
        calculator.evaluate("radius = 5");
        // Area of circle
        Double area = calculator.evaluate("PI * radius^2");
        assertEquals(Math.PI * 25, area, 0.001);
        
        // Pythagorean theorem
        calculator.evaluate("a = 3");
        calculator.evaluate("b = 4");
        Double hypotenuse = calculator.evaluate("sqrt(a^2 + b^2)");
        assertEquals(5.0, hypotenuse, 0.001);
        
        // Compound expression
        Double result = calculator.evaluate("sin(PI/6) + cos(PI/3) + tan(PI/4)");
        assertEquals(Math.sin(Math.PI/6) + Math.cos(Math.PI/3) + Math.tan(Math.PI/4), result, 0.001);
    }
    
    @Test
    void testErrorHandling() {
        // Division by zero
        assertThrows(RuntimeException.class, () -> calculator.evaluate("5 / 0"));
        
        // Invalid expression
        assertThrows(RuntimeException.class, () -> calculator.evaluate("5 +"));
    }
    
    @Test
    void testFloatingPointNumbers() {
        assertEquals(3.14, calculator.evaluate("3.14"), 0.001);
        assertEquals(7.64, calculator.evaluate("3.14 + 4.5"), 0.001);
        assertEquals(14.13, calculator.evaluate("3.14 * 4.5"), 0.001);
    }
    
    @Test
    void testVariableManagement() {
        assertTrue(calculator.getVariables().containsKey("PI"));
        assertTrue(calculator.getVariables().containsKey("E"));
        
        calculator.setVariable("test", 123.45);
        assertTrue(calculator.getVariables().containsKey("test"));
        assertEquals(123.45, calculator.getVariable("test"), 0.001);
        
        // Test that constants are preserved
        assertEquals(Math.PI, calculator.getVariable("PI"), 0.001);
        assertEquals(Math.E, calculator.getVariable("E"), 0.001);
    }
    
    @Test
    void testVariableReassignment() {
        calculator.evaluate("x = 5");
        assertEquals(5.0, calculator.getVariable("x"), 0.001);
        
        calculator.evaluate("x = x * 2");
        assertEquals(10.0, calculator.getVariable("x"), 0.001);
        
        calculator.evaluate("x = x + 1");
        assertEquals(11.0, calculator.getVariable("x"), 0.001);
    }
    
    @Test
    void testNestedFunctions() {
        assertEquals(Math.sqrt(Math.abs(-16)), calculator.evaluate("sqrt(abs(-16))"), 0.001);
        assertEquals(Math.sin(Math.cos(0)), calculator.evaluate("sin(cos(0))"), 0.001);
    }
    
    @Test
    void testParenthesesHandling() {
        assertEquals(9.0, calculator.evaluate("(2 + 1) * 3"), 0.001);
        assertEquals(7.0, calculator.evaluate("2 + (1 * 3) + 2"), 0.001);
        assertEquals(14.0, calculator.evaluate("((2 + 1) * 3) + 5"), 0.001);
    }
}