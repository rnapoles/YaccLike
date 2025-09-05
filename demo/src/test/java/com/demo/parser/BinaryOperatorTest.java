package com.demo.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the BinaryOperator enum.
 */
public class BinaryOperatorTest {

    @Test
    public void testOperatorSymbols() {
        assertEquals("+", BinaryOperator.PLUS.getSymbol());
        assertEquals("-", BinaryOperator.MINUS.getSymbol());
        assertEquals("*", BinaryOperator.MULTIPLY.getSymbol());
        assertEquals("/", BinaryOperator.DIVIDE.getSymbol());
        assertEquals("%", BinaryOperator.MODULO.getSymbol());
        assertEquals("^", BinaryOperator.POWER.getSymbol());
    }

    @Test
    public void testOperatorPrecedence() {
        // Addition and subtraction have lowest precedence
        assertEquals(1, BinaryOperator.PLUS.getPrecedence());
        assertEquals(1, BinaryOperator.MINUS.getPrecedence());
        
        // Multiplication, division, and modulo have medium precedence
        assertEquals(2, BinaryOperator.MULTIPLY.getPrecedence());
        assertEquals(2, BinaryOperator.DIVIDE.getPrecedence());
        assertEquals(2, BinaryOperator.MODULO.getPrecedence());
        
        // Power has highest precedence
        assertEquals(3, BinaryOperator.POWER.getPrecedence());
    }

    @Test
    public void testPrecedenceOrdering() {
        assertTrue(BinaryOperator.POWER.getPrecedence() > BinaryOperator.MULTIPLY.getPrecedence());
        assertTrue(BinaryOperator.MULTIPLY.getPrecedence() > BinaryOperator.PLUS.getPrecedence());
        assertTrue(BinaryOperator.DIVIDE.getPrecedence() > BinaryOperator.MINUS.getPrecedence());
        assertEquals(BinaryOperator.MULTIPLY.getPrecedence(), BinaryOperator.DIVIDE.getPrecedence());
        assertEquals(BinaryOperator.PLUS.getPrecedence(), BinaryOperator.MINUS.getPrecedence());
    }

    @Test
    public void testFromSymbol() {
        assertEquals(BinaryOperator.PLUS, BinaryOperator.fromSymbol("+"));
        assertEquals(BinaryOperator.MINUS, BinaryOperator.fromSymbol("-"));
        assertEquals(BinaryOperator.MULTIPLY, BinaryOperator.fromSymbol("*"));
        assertEquals(BinaryOperator.DIVIDE, BinaryOperator.fromSymbol("/"));
        assertEquals(BinaryOperator.MODULO, BinaryOperator.fromSymbol("%"));
        assertEquals(BinaryOperator.POWER, BinaryOperator.fromSymbol("^"));
    }

    @Test
    public void testFromSymbolInvalid() {
        assertNull(BinaryOperator.fromSymbol("&"));
        assertNull(BinaryOperator.fromSymbol("|"));
        assertNull(BinaryOperator.fromSymbol(""));
        assertNull(BinaryOperator.fromSymbol(null));
        assertNull(BinaryOperator.fromSymbol("++"));
    }

    @Test
    public void testToString() {
        assertEquals("+", BinaryOperator.PLUS.toString());
        assertEquals("-", BinaryOperator.MINUS.toString());
        assertEquals("*", BinaryOperator.MULTIPLY.toString());
        assertEquals("/", BinaryOperator.DIVIDE.toString());
        assertEquals("%", BinaryOperator.MODULO.toString());
        assertEquals("^", BinaryOperator.POWER.toString());
    }

    @Test
    public void testAllOperatorsPresent() {
        BinaryOperator[] operators = BinaryOperator.values();
        assertEquals(6, operators.length);
        
        // Verify all expected operators are present
        boolean hasPlus = false, hasMinus = false, hasMultiply = false;
        boolean hasDivide = false, hasModulo = false, hasPower = false;
        
        for (BinaryOperator op : operators) {
            switch (op) {
                case PLUS: hasPlus = true; break;
                case MINUS: hasMinus = true; break;
                case MULTIPLY: hasMultiply = true; break;
                case DIVIDE: hasDivide = true; break;
                case MODULO: hasModulo = true; break;
                case POWER: hasPower = true; break;
            }
        }
        
        assertTrue(hasPlus && hasMinus && hasMultiply && hasDivide && hasModulo && hasPower);
    }
}