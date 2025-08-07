package com.parsergenerator.lexer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PositionTest {
    
    @Test
    void testConstructorWithLineAndColumn() {
        Position pos = new Position(5, 10);
        assertEquals(5, pos.getLine());
        assertEquals(10, pos.getColumn());
        assertNull(pos.getFilename());
    }
    
    @Test
    void testConstructorWithFilename() {
        Position pos = new Position(3, 7, "test.grammar");
        assertEquals(3, pos.getLine());
        assertEquals(7, pos.getColumn());
        assertEquals("test.grammar", pos.getFilename());
    }
    
    @Test
    void testAdvanceColumn() {
        Position pos = new Position(2, 5);
        Position advanced = pos.advanceColumn(3);
        
        assertEquals(2, advanced.getLine());
        assertEquals(8, advanced.getColumn());
        assertNull(advanced.getFilename());
        
        // Original position should be unchanged
        assertEquals(5, pos.getColumn());
    }
    
    @Test
    void testAdvanceColumnWithFilename() {
        Position pos = new Position(2, 5, "test.grammar");
        Position advanced = pos.advanceColumn(3);
        
        assertEquals(2, advanced.getLine());
        assertEquals(8, advanced.getColumn());
        assertEquals("test.grammar", advanced.getFilename());
    }
    
    @Test
    void testNextLine() {
        Position pos = new Position(2, 15);
        Position nextLine = pos.nextLine();
        
        assertEquals(3, nextLine.getLine());
        assertEquals(1, nextLine.getColumn());
        assertNull(nextLine.getFilename());
        
        // Original position should be unchanged
        assertEquals(2, pos.getLine());
        assertEquals(15, pos.getColumn());
    }
    
    @Test
    void testNextLineWithFilename() {
        Position pos = new Position(2, 15, "test.grammar");
        Position nextLine = pos.nextLine();
        
        assertEquals(3, nextLine.getLine());
        assertEquals(1, nextLine.getColumn());
        assertEquals("test.grammar", nextLine.getFilename());
    }
    
    @Test
    void testToStringWithoutFilename() {
        Position pos = new Position(10, 25);
        assertEquals("10:25", pos.toString());
    }
    
    @Test
    void testToStringWithFilename() {
        Position pos = new Position(10, 25, "grammar.txt");
        assertEquals("grammar.txt:10:25", pos.toString());
    }
    
    @Test
    void testEquals() {
        Position pos1 = new Position(5, 10);
        Position pos2 = new Position(5, 10);
        Position pos3 = new Position(5, 11);
        Position pos4 = new Position(6, 10);
        
        assertEquals(pos1, pos2);
        assertNotEquals(pos1, pos3);
        assertNotEquals(pos1, pos4);
        assertNotEquals(pos1, null);
        assertNotEquals(pos1, "not a position");
    }
    
    @Test
    void testEqualsWithFilename() {
        Position pos1 = new Position(5, 10, "test.grammar");
        Position pos2 = new Position(5, 10, "test.grammar");
        Position pos3 = new Position(5, 10, "other.grammar");
        Position pos4 = new Position(5, 10);
        
        assertEquals(pos1, pos2);
        assertNotEquals(pos1, pos3);
        assertNotEquals(pos1, pos4);
    }
    
    @Test
    void testHashCode() {
        Position pos1 = new Position(5, 10);
        Position pos2 = new Position(5, 10);
        Position pos3 = new Position(5, 10, "test.grammar");
        
        assertEquals(pos1.hashCode(), pos2.hashCode());
        assertNotEquals(pos1.hashCode(), pos3.hashCode());
    }
}