package com.demo.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the FunctionException class.
 */
public class FunctionExceptionTest {

    @Test
    public void testBasicConstructor() {
        String message = "Invalid function call";
        FunctionException ex = new FunctionException(message);
        
        assertEquals(message, ex.getMessage());
        assertNull(ex.getFunctionName());
        assertEquals(-1, ex.getArgumentCount());
        assertEquals(message, ex.getDetailedMessage());
    }

    @Test
    public void testConstructorWithFunctionName() {
        String message = "Function error";
        String functionName = "sqrt";
        FunctionException ex = new FunctionException(message, functionName);
        
        assertEquals(message, ex.getMessage());
        assertEquals(functionName, ex.getFunctionName());
        assertEquals(-1, ex.getArgumentCount());
        assertEquals("Function error in function: sqrt", ex.getDetailedMessage());
    }

    @Test
    public void testConstructorWithFunctionNameAndArgumentCount() {
        String message = "Wrong number of arguments";
        String functionName = "log";
        int argumentCount = 3;
        FunctionException ex = new FunctionException(message, functionName, argumentCount);
        
        assertEquals(message, ex.getMessage());
        assertEquals(functionName, ex.getFunctionName());
        assertEquals(argumentCount, ex.getArgumentCount());
        assertEquals("Wrong number of arguments in function: log with 3 argument(s)", ex.getDetailedMessage());
    }

    @Test
    public void testConstructorWithPosition() {
        String message = "Function evaluation failed";
        String functionName = "sin";
        Position position = new Position(2, 10);
        FunctionException ex = new FunctionException(message, functionName, position);
        
        assertEquals(message, ex.getMessage());
        assertEquals(functionName, ex.getFunctionName());
        assertEquals(position, ex.getPosition());
        assertEquals(-1, ex.getArgumentCount());
        assertEquals("Function evaluation failed at line 2, column 10 in function: sin", ex.getDetailedMessage());
    }

    @Test
    public void testDetailedMessageWithoutFunctionName() {
        String message = "Generic function error";
        FunctionException ex = new FunctionException(message, null);
        
        assertEquals("Generic function error", ex.getDetailedMessage());
    }

    @Test
    public void testDetailedMessageWithEmptyFunctionName() {
        String message = "Function error";
        FunctionException ex = new FunctionException(message, "");
        
        assertEquals("Function error", ex.getDetailedMessage());
    }

    @Test
    public void testDetailedMessageWithZeroArguments() {
        String message = "No arguments provided";
        String functionName = "abs";
        FunctionException ex = new FunctionException(message, functionName, 0);
        
        assertEquals("No arguments provided in function: abs with 0 argument(s)", ex.getDetailedMessage());
    }

    @Test
    public void testDetailedMessageWithOneArgument() {
        String message = "Single argument error";
        String functionName = "cos";
        FunctionException ex = new FunctionException(message, functionName, 1);
        
        assertEquals("Single argument error in function: cos with 1 argument(s)", ex.getDetailedMessage());
    }

    @Test
    public void testInheritanceFromExpressionException() {
        FunctionException ex = new FunctionException("Test");
        assertTrue(ex instanceof ExpressionException);
    }

    @Test
    public void testComplexDetailedMessage() {
        String message = "Domain error";
        String functionName = "sqrt";
        Position position = new Position(5, 12);
        FunctionException ex = new FunctionException(message, functionName, position);
        
        String detailedMessage = ex.getDetailedMessage();
        assertTrue(detailedMessage.contains("Domain error"));
        assertTrue(detailedMessage.contains("line 5"));
        assertTrue(detailedMessage.contains("column 12"));
        assertTrue(detailedMessage.contains("function: sqrt"));
    }
}