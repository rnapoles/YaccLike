package com.generated.parser;

/**
 * Represents a position in the source file with line and column information.
 * Used for error reporting and debugging purposes.
 */
public class Position {
    private final int line;
    private final int column;
    private final String filename;
    
    /**
     * Creates a new position with the specified line and column.
     * 
     * @param line the line number (1-based)
     * @param column the column number (1-based)
     */
    public Position(int line, int column) {
        this(line, column, null);
    }
    
    /**
     * Creates a new position with the specified line, column, and filename.
     * 
     * @param line the line number (1-based)
     * @param column the column number (1-based)
     * @param filename the source filename (optional)
     */
    public Position(int line, int column, String filename) {
        this.line = line;
        this.column = column;
        this.filename = filename;
    }
    
    /**
     * Gets the line number.
     * 
     * @return the line number (1-based)
     */
    public int getLine() {
        return line;
    }
    
    /**
     * Gets the column number.
     * 
     * @return the column number (1-based)
     */
    public int getColumn() {
        return column;
    }
    
    /**
     * Gets the filename.
     * 
     * @return the filename, or null if not specified
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * Creates a new position advanced by the specified number of columns.
     * 
     * @param columns the number of columns to advance
     * @return a new Position object
     */
    public Position advanceColumn(int columns) {
        return new Position(line, column + columns, filename);
    }
    
    /**
     * Creates a new position advanced to the next line.
     * 
     * @return a new Position object at the beginning of the next line
     */
    public Position nextLine() {
        return new Position(line + 1, 1, filename);
    }
    
    @Override
    public String toString() {
        if (filename != null) {
            return String.format("%s:%d:%d", filename, line, column);
        } else {
            return String.format("%d:%d", line, column);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Position position = (Position) obj;
        return line == position.line && 
               column == position.column &&
               (filename != null ? filename.equals(position.filename) : position.filename == null);
    }
    
    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + column;
        result = 31 * result + (filename != null ? filename.hashCode() : 0);
        return result;
    }
}