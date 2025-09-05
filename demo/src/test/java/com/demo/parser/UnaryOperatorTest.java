package com.demo.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the UnaryOperator enum.
 */
public class UnaryOperatorTest {

    @Test
    public void testOperatorSymbol() {
        assertEquals("-", UnaryOperator.MINUS.getSymbol());
    }

    @Test
    public void testFromSymbol() {
        assertEquals(UnaryOperator.MINUS, UnaryOperator.fromSymbol("-"));
    }

    @Test
    public void testFromSymbolInvalid() {
        assertNull(UnaryOperator.fromSymbol("+"));
        assertNull(UnaryOperator.fromSymbol("*"));
        assertNull(UnaryOperator.fromSymbol(""));
        assertNull(UnaryOperator.fromSymbol(null));
        assertNull(UnaryOperator.fromSymbol("--"));
    }

    @Test
    public void testToString() {
        assertEquals("-", UnaryOperator.MINUS.toString());
    }

    @Test
    public void testAllOperatorsPresent() {
        UnaryOperator[] operators = UnaryOperator.values();
        assertEquals(1, operators.length);
        assertEquals(UnaryOperator.MINUS, operators[0]);
    }

    @Test
    public void testEnumConsistency() {
        // Verify that the enum is consistent with its methods
        for (UnaryOperator op : UnaryOperator.values()) {
            assertEquals(op, UnaryOperator.fromSymbol(op.getSymbol()));
            assertEquals(op.getSymbol(), op.toString());
        }
    }
}