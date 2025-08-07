package com.parsergenerator.lexer;

/**
 * Represents a token in the grammar specification with its type, value, and position.
 * Tokens are the basic units produced by the lexer and consumed by the parser.
 */
public class Token {
    private final TokenType type;
    private final String value;
    private final Position position;
    
    /**
     * Creates a new token with the specified type, value, and position.
     * 
     * @param type the token type
     * @param value the token value (lexeme)
     * @param position the position in the source file
     */
    public Token(TokenType type, String value, Position position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }
    
    /**
     * Creates a new token with the specified type and value at an unknown position.
     * 
     * @param type the token type
     * @param value the token value (lexeme)
     */
    public Token(TokenType type, String value) {
        this(type, value, new Position(0, 0));
    }
    
    /**
     * Gets the token type.
     * 
     * @return the token type
     */
    public TokenType getType() {
        return type;
    }
    
    /**
     * Gets the token value (lexeme).
     * 
     * @return the token value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Gets the token position in the source file.
     * 
     * @return the position
     */
    public Position getPosition() {
        return position;
    }
    
    /**
     * Checks if this token has the specified type.
     * 
     * @param tokenType the type to check
     * @return true if the token has the specified type
     */
    public boolean isType(TokenType tokenType) {
        return this.type == tokenType;
    }
    
    /**
     * Checks if this token has any of the specified types.
     * 
     * @param tokenTypes the types to check
     * @return true if the token has any of the specified types
     */
    public boolean isAnyType(TokenType... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if (this.type == tokenType) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if this token represents an EOF token.
     * 
     * @return true if this is an EOF token
     */
    public boolean isEOF() {
        return type == TokenType.EOF;
    }
    
    @Override
    public String toString() {
        return String.format("Token{type=%s, value='%s', position=%s}", 
                           type, value, position);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Token token = (Token) obj;
        return type == token.type &&
               (value != null ? value.equals(token.value) : token.value == null) &&
               (position != null ? position.equals(token.position) : token.position == null);
    }
    
    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        return result;
    }
}