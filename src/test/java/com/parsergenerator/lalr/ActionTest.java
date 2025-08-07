package com.parsergenerator.lalr;

import com.parsergenerator.grammar.Production;
import com.parsergenerator.symbol.NonTerminal;
import com.parsergenerator.symbol.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ActionTest {
    
    private Production testProduction;
    private NonTerminal A;
    private Terminal a;
    
    @BeforeEach
    void setUp() {
        Production.resetIdCounter();
        A = new NonTerminal("A");
        a = new Terminal("a", "a");
        testProduction = new Production(A, Arrays.asList(a));
    }
    
    @Test
    void testShiftAction() {
        Action shiftAction = Action.shift(5);
        
        assertTrue(shiftAction.isShift());
        assertFalse(shiftAction.isReduce());
        assertFalse(shiftAction.isAccept());
        assertFalse(shiftAction.isError());
        
        assertEquals(ActionType.SHIFT, shiftAction.getType());
        assertEquals(5, shiftAction.getParameter());
        assertNull(shiftAction.getProduction());
        
        assertEquals("s5", shiftAction.toString());
        assertEquals("SHIFT 5", shiftAction.toDetailedString());
    }
    
    @Test
    void testReduceAction() {
        Action reduceAction = Action.reduce(testProduction);
        
        assertFalse(reduceAction.isShift());
        assertTrue(reduceAction.isReduce());
        assertFalse(reduceAction.isAccept());
        assertFalse(reduceAction.isError());
        
        assertEquals(ActionType.REDUCE, reduceAction.getType());
        assertEquals(testProduction.getId(), reduceAction.getParameter());
        assertEquals(testProduction, reduceAction.getProduction());
        
        assertEquals("r" + testProduction.getId(), reduceAction.toString());
        assertTrue(reduceAction.toDetailedString().contains("REDUCE"));
        assertTrue(reduceAction.toDetailedString().contains(testProduction.toString()));
    }
    
    @Test
    void testReduceActionValidation() {
        assertThrows(IllegalArgumentException.class, () -> Action.reduce(null));
    }
    
    @Test
    void testAcceptAction() {
        Action acceptAction = Action.accept();
        
        assertFalse(acceptAction.isShift());
        assertFalse(acceptAction.isReduce());
        assertTrue(acceptAction.isAccept());
        assertFalse(acceptAction.isError());
        
        assertEquals(ActionType.ACCEPT, acceptAction.getType());
        assertEquals(0, acceptAction.getParameter());
        assertNull(acceptAction.getProduction());
        
        assertEquals("acc", acceptAction.toString());
        assertEquals("ACCEPT", acceptAction.toDetailedString());
    }
    
    @Test
    void testErrorAction() {
        Action errorAction = Action.error();
        
        assertFalse(errorAction.isShift());
        assertFalse(errorAction.isReduce());
        assertFalse(errorAction.isAccept());
        assertTrue(errorAction.isError());
        
        assertEquals(ActionType.ERROR, errorAction.getType());
        assertEquals(0, errorAction.getParameter());
        assertNull(errorAction.getProduction());
        
        assertEquals("err", errorAction.toString());
        assertEquals("ERROR", errorAction.toDetailedString());
    }
    
    @Test
    void testEquals() {
        Action shift1 = Action.shift(5);
        Action shift2 = Action.shift(5);
        Action shift3 = Action.shift(3);
        
        Action reduce1 = Action.reduce(testProduction);
        Action reduce2 = Action.reduce(testProduction);
        
        Action accept1 = Action.accept();
        Action accept2 = Action.accept();
        
        Action error1 = Action.error();
        Action error2 = Action.error();
        
        // Test equality for same actions
        assertEquals(shift1, shift2);
        assertEquals(reduce1, reduce2);
        assertEquals(accept1, accept2);
        assertEquals(error1, error2);
        
        // Test inequality for different actions
        assertNotEquals(shift1, shift3);
        assertNotEquals(shift1, reduce1);
        assertNotEquals(shift1, accept1);
        assertNotEquals(shift1, error1);
        
        // Test self equality
        assertEquals(shift1, shift1);
        
        // Test null and different class
        assertNotEquals(shift1, null);
        assertNotEquals(shift1, "not an action");
    }
    
    @Test
    void testHashCode() {
        Action shift1 = Action.shift(5);
        Action shift2 = Action.shift(5);
        Action reduce1 = Action.reduce(testProduction);
        Action reduce2 = Action.reduce(testProduction);
        
        assertEquals(shift1.hashCode(), shift2.hashCode());
        assertEquals(reduce1.hashCode(), reduce2.hashCode());
    }
}