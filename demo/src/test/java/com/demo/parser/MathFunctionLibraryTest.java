package com.demo.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the MathFunctionLibrary class.
 * Tests all mathematical functions with various inputs including edge cases and error conditions.
 */
public class MathFunctionLibraryTest {

    private static final double DELTA = 1e-10; // Tolerance for floating-point comparisons

    @Test
    @DisplayName("Test trigonometric functions with known values")
    public void testTrigonometricFunctions() {
        // Test sin function
        assertEquals(0.0, MathFunctionLibrary.sin(0), DELTA);
        assertEquals(1.0, MathFunctionLibrary.sin(Math.PI / 2), DELTA);
        assertEquals(0.0, MathFunctionLibrary.sin(Math.PI), DELTA);
        assertEquals(-1.0, MathFunctionLibrary.sin(3 * Math.PI / 2), DELTA);
        assertEquals(Math.sqrt(2) / 2, MathFunctionLibrary.sin(Math.PI / 4), DELTA);

        // Test cos function
        assertEquals(1.0, MathFunctionLibrary.cos(0), DELTA);
        assertEquals(0.0, MathFunctionLibrary.cos(Math.PI / 2), DELTA);
        assertEquals(-1.0, MathFunctionLibrary.cos(Math.PI), DELTA);
        assertEquals(0.0, MathFunctionLibrary.cos(3 * Math.PI / 2), DELTA);
        assertEquals(Math.sqrt(2) / 2, MathFunctionLibrary.cos(Math.PI / 4), DELTA);

        // Test tan function
        assertEquals(0.0, MathFunctionLibrary.tan(0), DELTA);
        assertEquals(0.0, MathFunctionLibrary.tan(Math.PI), DELTA);
        assertEquals(1.0, MathFunctionLibrary.tan(Math.PI / 4), DELTA);
        assertEquals(-1.0, MathFunctionLibrary.tan(-Math.PI / 4), DELTA);
    }

    @Test
    @DisplayName("Test natural logarithm function")
    public void testNaturalLogarithm() throws FunctionException {
        assertEquals(0.0, MathFunctionLibrary.ln(1), DELTA);
        assertEquals(1.0, MathFunctionLibrary.ln(Math.E), DELTA);
        assertEquals(2.0, MathFunctionLibrary.ln(Math.E * Math.E), DELTA);
        assertEquals(-1.0, MathFunctionLibrary.ln(1.0 / Math.E), DELTA);
        
        // Test with other known values
        assertEquals(Math.log(2), MathFunctionLibrary.ln(2), DELTA);
        assertEquals(Math.log(10), MathFunctionLibrary.ln(10), DELTA);
    }

    @Test
    @DisplayName("Test ln function throws exception for invalid arguments")
    public void testLnInvalidArguments() {
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.ln(0));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.ln(-1));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.ln(-10.5));
    }

    @Test
    @DisplayName("Test base-10 logarithm function")
    public void testBase10Logarithm() throws FunctionException {
        assertEquals(0.0, MathFunctionLibrary.log(1), DELTA);
        assertEquals(1.0, MathFunctionLibrary.log(10), DELTA);
        assertEquals(2.0, MathFunctionLibrary.log(100), DELTA);
        assertEquals(3.0, MathFunctionLibrary.log(1000), DELTA);
        assertEquals(-1.0, MathFunctionLibrary.log(0.1), DELTA);
        assertEquals(-2.0, MathFunctionLibrary.log(0.01), DELTA);
    }

    @Test
    @DisplayName("Test log function throws exception for invalid arguments")
    public void testLogInvalidArguments() {
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.log(0));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.log(-1));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.log(-5.5));
    }

    @Test
    @DisplayName("Test custom base logarithm function")
    public void testCustomBaseLogarithm() throws FunctionException {
        // log_2(8) = 3
        assertEquals(3.0, MathFunctionLibrary.log(2, 8), DELTA);
        // log_2(16) = 4
        assertEquals(4.0, MathFunctionLibrary.log(2, 16), DELTA);
        // log_3(27) = 3
        assertEquals(3.0, MathFunctionLibrary.log(3, 27), DELTA);
        // log_5(125) = 3
        assertEquals(3.0, MathFunctionLibrary.log(5, 125), DELTA);
        // log_10(100) = 2 (same as regular log)
        assertEquals(2.0, MathFunctionLibrary.log(10, 100), DELTA);
    }

    @Test
    @DisplayName("Test custom base log function throws exception for invalid base")
    public void testCustomBaseLogInvalidBase() {
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.log(0, 10));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.log(-2, 10));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.log(1, 10)); // base cannot be 1
    }

    @Test
    @DisplayName("Test custom base log function throws exception for invalid argument")
    public void testCustomBaseLogInvalidArgument() {
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.log(2, 0));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.log(2, -5));
    }

    @Test
    @DisplayName("Test square root function")
    public void testSquareRoot() throws FunctionException {
        assertEquals(0.0, MathFunctionLibrary.sqrt(0), DELTA);
        assertEquals(1.0, MathFunctionLibrary.sqrt(1), DELTA);
        assertEquals(2.0, MathFunctionLibrary.sqrt(4), DELTA);
        assertEquals(3.0, MathFunctionLibrary.sqrt(9), DELTA);
        assertEquals(4.0, MathFunctionLibrary.sqrt(16), DELTA);
        assertEquals(5.0, MathFunctionLibrary.sqrt(25), DELTA);
        assertEquals(Math.sqrt(2), MathFunctionLibrary.sqrt(2), DELTA);
        assertEquals(Math.sqrt(3), MathFunctionLibrary.sqrt(3), DELTA);
    }

    @Test
    @DisplayName("Test sqrt function throws exception for negative values")
    public void testSqrtNegativeValues() {
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.sqrt(-1));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.sqrt(-4));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.sqrt(-0.5));
    }

    @Test
    @DisplayName("Test absolute value function")
    public void testAbsoluteValue() {
        assertEquals(0.0, MathFunctionLibrary.abs(0), DELTA);
        assertEquals(5.0, MathFunctionLibrary.abs(5), DELTA);
        assertEquals(5.0, MathFunctionLibrary.abs(-5), DELTA);
        assertEquals(3.14, MathFunctionLibrary.abs(3.14), DELTA);
        assertEquals(3.14, MathFunctionLibrary.abs(-3.14), DELTA);
        assertEquals(Double.MAX_VALUE, MathFunctionLibrary.abs(-Double.MAX_VALUE), DELTA);
    }

    @Test
    @DisplayName("Test evaluate method with MathFunction enum - single argument functions")
    public void testEvaluateWithEnumSingleArg() throws FunctionException {
        assertEquals(0.0, MathFunctionLibrary.evaluate(MathFunction.SIN, 0), DELTA);
        assertEquals(1.0, MathFunctionLibrary.evaluate(MathFunction.COS, 0), DELTA);
        assertEquals(0.0, MathFunctionLibrary.evaluate(MathFunction.TAN, 0), DELTA);
        assertEquals(1.0, MathFunctionLibrary.evaluate(MathFunction.LN, Math.E), DELTA);
        assertEquals(2.0, MathFunctionLibrary.evaluate(MathFunction.LOG, 100), DELTA);
        assertEquals(4.0, MathFunctionLibrary.evaluate(MathFunction.SQRT, 16), DELTA);
        assertEquals(7.0, MathFunctionLibrary.evaluate(MathFunction.ABS, -7), DELTA);
    }

    @Test
    @DisplayName("Test evaluate method with MathFunction enum - log with two arguments")
    public void testEvaluateWithEnumLogTwoArgs() throws FunctionException {
        assertEquals(3.0, MathFunctionLibrary.evaluate(MathFunction.LOG, 2, 8), DELTA);
        assertEquals(4.0, MathFunctionLibrary.evaluate(MathFunction.LOG, 3, 81), DELTA);
    }

    @Test
    @DisplayName("Test evaluate method throws exception for wrong argument count")
    public void testEvaluateWrongArgumentCount() {
        // Too few arguments
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.evaluate(MathFunction.SIN));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.evaluate(MathFunction.LOG));
        
        // Too many arguments
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.evaluate(MathFunction.SIN, 1, 2));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.evaluate(MathFunction.ABS, 1, 2, 3));
        assertThrows(FunctionException.class, () -> MathFunctionLibrary.evaluate(MathFunction.LOG, 1, 2, 3));
    }

    @Test
    @DisplayName("Test edge cases with very small and very large numbers")
    public void testEdgeCases() throws FunctionException {
        // Very small positive numbers
        double verySmall = 1e-100;
        assertTrue(MathFunctionLibrary.ln(verySmall) < 0);
        assertTrue(MathFunctionLibrary.log(verySmall) < 0);
        assertEquals(Math.sqrt(verySmall), MathFunctionLibrary.sqrt(verySmall), DELTA);
        assertEquals(verySmall, MathFunctionLibrary.abs(verySmall), DELTA);
        
        // Very large numbers
        double veryLarge = 1e100;
        assertTrue(MathFunctionLibrary.ln(veryLarge) > 0);
        assertTrue(MathFunctionLibrary.log(veryLarge) > 0);
        assertEquals(Math.sqrt(veryLarge), MathFunctionLibrary.sqrt(veryLarge), DELTA);
        assertEquals(veryLarge, MathFunctionLibrary.abs(veryLarge), DELTA);
    }

    @Test
    @DisplayName("Test function exception messages contain function names")
    public void testFunctionExceptionMessages() {
        try {
            MathFunctionLibrary.ln(-1);
            fail("Expected FunctionException");
        } catch (FunctionException e) {
            assertTrue(e.getMessage().contains("ln"));
            assertEquals("ln", e.getFunctionName());
        }

        try {
            MathFunctionLibrary.sqrt(-4);
            fail("Expected FunctionException");
        } catch (FunctionException e) {
            assertTrue(e.getMessage().contains("sqrt"));
            assertEquals("sqrt", e.getFunctionName());
        }

        try {
            MathFunctionLibrary.evaluate(MathFunction.SIN, 1, 2);
            fail("Expected FunctionException");
        } catch (FunctionException e) {
            assertTrue(e.getMessage().contains("sin"));
            assertEquals("sin", e.getFunctionName());
            assertEquals(2, e.getArgumentCount());
        }
    }
}