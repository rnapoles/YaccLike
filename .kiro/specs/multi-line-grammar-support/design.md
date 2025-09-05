# Design Document

## Overview

The multi-line grammar support enhancement extends the existing GrammarLexer to handle production rules that span multiple lines while maintaining full backward compatibility with single-line grammar files. The design focuses on minimal changes to the existing architecture, introducing a line continuation mechanism that recognizes when production rules extend across line boundaries.

The key insight is that multi-line production rules are primarily identified by the presence of the pipe (`|`) operator at the end of a line or at the beginning of a continuation line, indicating that additional alternatives follow. The lexer will implement a lookahead mechanism to detect these patterns and handle line continuation transparently.

## Architecture

### Current Architecture Analysis

The existing GrammarLexer follows a character-by-character scanning approach with these key components:

- **Position tracking**: Line and column tracking for error reporting
- **Token generation**: Single-pass tokenization with immediate token creation
- **Error handling**: Integrated error reporting with contextual messages
- **Whitespace handling**: Explicit whitespace skipping between tokens

### Enhanced Architecture

The multi-line support will be implemented through these architectural additions:

1. **Line Continuation Detection**: A new method to detect when a production rule continues on the next line
2. **Token Buffering**: Temporary buffering of tokens when line continuation is detected
3. **Whitespace Normalization**: Enhanced whitespace handling to normalize multi-line formatting
4. **State Management**: Tracking whether the lexer is currently processing a multi-line production rule

## Components and Interfaces

### Core Components

#### 1. Enhanced GrammarLexer Class

**New Methods:**
```java
private boolean isProductionContinuation()
private List<Token> handleMultiLineProduction(Position startPos)
private void skipLeadingWhitespaceOnContinuationLine()
private boolean isAtLineStart()
```

**Modified Methods:**
- `nextToken()`: Enhanced to detect and handle multi-line productions
- `skipWhitespace()`: Updated to handle continuation line whitespace

#### 2. Token Buffer Management

**New Internal State:**
```java
private List<Token> tokenBuffer = new ArrayList<>();
private boolean inMultiLineProduction = false;
private int multiLineStartLine = -1;
```

#### 3. Line Continuation Logic

The lexer will detect multi-line productions using these patterns:

1. **Trailing Pipe Pattern**: A line ending with `|` followed by whitespace and newline
2. **Leading Pipe Pattern**: A line starting with optional whitespace followed by `|`
3. **Incomplete Production Pattern**: A production rule that appears syntactically incomplete at line end

### Interface Compatibility

The public interface remains unchanged:
- `tokenize()` method signature and behavior unchanged
- `nextToken()` method signature unchanged
- All existing TokenType enums remain the same
- Error reporting interface unchanged

## Data Models

### Token Representation

No changes to existing Token class structure. Multi-line productions will generate the same token sequence as their single-line equivalents.

### Position Tracking Enhancement

The Position class will be enhanced to track multi-line production spans:

```java
// New optional fields for multi-line tracking
private int startLine = -1;  // For multi-line tokens
private int endLine = -1;    // For multi-line tokens
```

### Line Continuation State

```java
private enum ContinuationState {
    NONE,           // Not in a multi-line production
    EXPECTING_PIPE, // Expecting a pipe continuation
    IN_PRODUCTION   // Currently processing multi-line production
}
```

## Error Handling

### Multi-Line Error Scenarios

1. **Incomplete Continuation**: Line ends with `|` but no continuation follows
2. **Invalid Continuation**: Continuation line doesn't start with valid production syntax
3. **Nested Multi-Line**: Attempting to start a new production within an incomplete one
4. **Malformed Pipe**: Pipe operator in invalid position within multi-line context

### Error Message Enhancements

Error messages will be enhanced to provide multi-line context:

```java
// Example enhanced error messages
"Multi-line production rule incomplete - expected continuation after '|' on line 15"
"Invalid continuation on line 17 - expected production alternative or terminal"
"Multi-line production started on line 12 but never completed"
```

### Error Recovery

The lexer will implement graceful error recovery for multi-line productions:
- Reset to single-line mode when encountering syntax errors
- Continue processing subsequent lines normally
- Provide suggestions for fixing common multi-line formatting issues

## Testing Strategy

### Unit Test Categories

#### 1. Basic Multi-Line Support Tests
- Simple two-line production rules
- Multiple alternatives across lines
- Mixed single-line and multi-line in same grammar
- Whitespace variations in continuation lines

#### 2. Backward Compatibility Tests
- All existing test cases must pass unchanged
- Performance regression tests
- Token sequence equivalence tests (single-line vs multi-line)

#### 3. Error Handling Tests
- Incomplete multi-line productions
- Invalid continuation syntax
- Malformed pipe operators
- Error message accuracy and helpfulness

#### 4. Edge Case Tests
- Empty continuation lines
- Comments within multi-line productions
- Very long multi-line productions (10+ lines)
- Mixed line ending types (CRLF, LF)

#### 5. Integration Tests
- Complete grammar files with multi-line productions
- Parser integration with multi-line lexer output
- Real-world grammar examples

### Test Data Examples

#### Valid Multi-Line Productions
```
<Expression> ::= <Term> PLUS <Expression>
               | <Term> MINUS <Expression>
               | <Term>

<Factor> ::= <Base> POWER <Factor> |
             <Base>
```

#### Invalid Multi-Line Productions
```
<Expression> ::= <Term> PLUS <Expression> |
-- Missing continuation

<Term> ::= <Factor> MULTIPLY
| <Factor>  -- Invalid: incomplete previous line
```

### Performance Testing

- Benchmark single-line grammar processing (baseline)
- Benchmark multi-line grammar processing
- Memory usage analysis for token buffering
- Large grammar file processing tests

## Implementation Phases

### Phase 1: Core Multi-Line Detection
- Implement line continuation detection logic
- Add basic token buffering mechanism
- Handle simple two-line productions

### Phase 2: Advanced Multi-Line Support
- Support for multiple continuation lines
- Enhanced whitespace normalization
- Complex production rule patterns

### Phase 3: Error Handling Enhancement
- Comprehensive error detection for multi-line contexts
- Improved error messages with multi-line context
- Error recovery mechanisms

### Phase 4: Optimization and Polish
- Performance optimization for token buffering
- Memory usage optimization
- Code cleanup and documentation

## Backward Compatibility Guarantees

1. **Token Sequence Equivalence**: Multi-line and single-line versions of the same grammar produce identical token sequences
2. **API Compatibility**: No changes to public method signatures or return types
3. **Performance**: No performance degradation for single-line grammars
4. **Error Reporting**: Existing error types and formats remain unchanged for single-line cases

## Security Considerations

- **Input Validation**: Enhanced validation for multi-line input patterns
- **Memory Management**: Bounded token buffering to prevent memory exhaustion
- **Error Information**: Careful handling of error messages to avoid information leakage

## Future Extensibility

The design allows for future enhancements:
- **Nested Multi-Line**: Support for nested production rules across lines
- **Advanced Formatting**: Support for more complex indentation patterns
- **IDE Integration**: Enhanced position tracking for syntax highlighting
- **Macro Support**: Foundation for grammar macro expansion across lines