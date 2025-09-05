package com.demo.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MathFunction enum.
 */
public class MathFunctionTest {

    @Test
    public void testFunctionNames() {
        assertEquals("sin", MathFunction.SIN.getName());
        assertEquals("cos", MathFunction.COS.getName());
        assertEquals("tan", MathFunction.TAN.getName());
        assertEquals("log", MathFunction.LOG.getName());
        assertEquals("ln", MathFunction.LN.getName());
        assertEquals("sqrt", MathFunction.SQRT.getName());
        assertEquals("abs", MathFunction.ABS.getName());
    }

    @Test
    public void testSingleArgumentFunctions() {
        // Test functions that take exactly one argument
        assertEquals(1, MathFunction.SIN.getMinArgs());
        assertEquals(1, MathFunction.SIN.getMaxArgs());
        assertTrue(MathFunction.SIN.isValidArgCount(1));
        assertFalse(MathFunction.SIN.isValidArgCount(0));
        assertFalse(MathFunction.SIN.isValidArgCount(2));

        assertEquals(1, MathFunction.COS.getMinArgs());
        assertEquals(1, MathFunction.COS.getMaxArgs());
        assertTrue(MathFunction.COS.isValidArgCount(1));

        assertEquals(1, MathFunction.TAN.getMinArgs());
        assertEquals(1, MathFunction.TAN.getMaxArgs());
        assertTrue(MathFunction.TAN.isValidArgCount(1));

        assertEquals(1, MathFunction.LN.getMinArgs());
        assertEquals(1, MathFunction.LN.getMaxArgs());
        assertTrue(MathFunction.LN.isValidArgCount(1));

        assertEquals(1, MathFunction.SQRT.getMinArgs());
        assertEquals(1, MathFunction.SQRT.getMaxArgs());
        assertTrue(MathFunction.SQRT.isValidArgCount(1));

        assertEquals(1, MathFunction.ABS.getMinArgs());
        assertEquals(1, MathFunction.ABS.getMaxArgs());
        assertTrue(MathFunction.ABS.isValidArgCount(1));
    }

    @Test
    public void testVariableArgumentFunction() {
        // Test LOG function which can take 1 or 2 arguments
        assertEquals(1, MathFunction.LOG.getMinArgs());
        assertEquals(2, MathFunction.LOG.getMaxArgs());
        assertTrue(MathFunction.LOG.isValidArgCount(1));
        assertTrue(MathFunction.LOG.isValidArgCount(2));
        assertFalse(MathFunction.LOG.isValidArgCount(0));
        assertFalse(MathFunction.LOG.isValidArgCount(3));
    }

    @Test
    public void testFromName() {
        assertEquals(MathFunction.SIN, MathFunction.fromName("sin"));
        assertEquals(MathFunction.COS, MathFunction.fromName("cos"));
        assertEquals(MathFunction.TAN, MathFunction.fromName("tan"));
        assertEquals(MathFunction.LOG, MathFunction.fromName("log"));
        assertEquals(MathFunction.LN, MathFunction.fromName("ln"));
        assertEquals(MathFunction.SQRT, MathFunction.fromName("sqrt"));
        assertEquals(MathFunction.ABS, MathFunction.fromName("abs"));
    }

    @Test
    public void testFromNameCaseInsensitive() {
        assertEquals(MathFunction.SIN, MathFunction.fromName("SIN"));
        assertEquals(MathFunction.COS, MathFunction.fromName("COS"));
        assertEquals(MathFunction.TAN, MathFunction.fromName("TaN"));
        assertEquals(MathFunction.LOG, MathFunction.fromName("LOG"));
        assertEquals(MathFunction.LN, MathFunction.fromName("LN"));
        assertEquals(MathFunction.SQRT, MathFunction.fromName("SQRT"));
        assertEquals(MathFunction.ABS, MathFunction.fromName("ABS"));
    }

    @Test
    public void testFromNameInvalid() {
        assertNull(MathFunction.fromName("invalid"));
        assertNull(MathFunction.fromName(""));
        assertNull(MathFunction.fromName(null));
        assertNull(MathFunction.fromName("sine"));
        assertNull(MathFunction.fromName("cosine"));
    }

    @Test
    public void testToString() {
        assertEquals("sin", MathFunction.SIN.toString());
        assertEquals("cos", MathFunction.COS.toString());
        assertEquals("tan", MathFunction.TAN.toString());
        assertEquals("log", MathFunction.LOG.toString());
        assertEquals("ln", MathFunction.LN.toString());
        assertEquals("sqrt", MathFunction.SQRT.toString());
        assertEquals("abs", MathFunction.ABS.toString());
    }

    @Test
    public void testAllFunctionsPresent() {
        MathFunction[] functions = MathFunction.values();
        assertEquals(7, functions.length);
        
        // Verify all expected functions are present
        boolean hasSin = false, hasCos = false, hasTan = false;
        boolean hasLog = false, hasLn = false, hasSqrt = false, hasAbs = false;
        
        for (MathFunction func : functions) {
            switch (func) {
                case SIN: hasSin = true; break;
                case COS: hasCos = true; break;
                case TAN: hasTan = true; break;
                case LOG: hasLog = true; break;
                case LN: hasLn = true; break;
                case SQRT: hasSqrt = true; break;
                case ABS: hasAbs = true; break;
            }
        }
        
        assertTrue(hasSin && hasCos && hasTan && hasLog && hasLn && hasSqrt && hasAbs);
    }

    @Test
    public void testEnumConsistency() {
        // Verify that the enum is consistent with its methods
        for (MathFunction func : MathFunction.values()) {
            assertEquals(func, MathFunction.fromName(func.getName()));
            assertEquals(func.getName(), func.toString());
            assertTrue(func.getMinArgs() <= func.getMaxArgs());
            assertTrue(func.getMinArgs() > 0);
        }
    }
}