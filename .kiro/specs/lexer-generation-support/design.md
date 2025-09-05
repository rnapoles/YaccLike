# Design Document

## Overview

This design adds lexer generation capabilities to the existing LALR(1) parser generator. The system will generate dedicated lexer classes from grammar terminal definitions, providing clean separation between lexical and syntactic analysis. The generated lexers will integrate seamlessly with the existing generated parsers while using the same Pebble Template Engine infrastructure.

The design extends the current architecture by adding lexer generation components alongside the existing parser generation pipeline. The generated lexers will handle tokenization of input text according to terminal patterns defined in grammar files, producing tokens that match the parser's expectations.

## Architecture

### High-Level Architecture

The lexer generation feature integrates into the existing parser generation pipeline:

```
Grammar File → Grammar Parser → Grammar Analysis → LALR Table Builder → Code Generation
                                                                            ↓
                                                                    Parser + Lexer Generation
```

### Component Integration

The lexer generation extends the existing `JavaCodeGenerator` class and adds new components:

1. **LexerCodeGenerator**: New class responsible for generating lexer-specific code
2. **LexerTemplateManager**: Extension of template management for lexer templates
3. **TokenizerEngine**: Runtime component for pattern matching in generated lexers
4. **Enhanced JavaCodeGenerator**: Modified to coordinate both parser and lexer generation

### Package Structure

```
com.parsergenerator.codegen
├── JavaCodeGenerator (modified)
├── LexerCodeGenerator (new)
└── LexerTemplateManager (new)

src/main/resources/templates
├── lexer_class.peb (new)
├── tokenizer_engine.peb (new)
└── lexer_exception.peb (new)
```

## Components and Interfaces

### LexerCodeGenerator

```java
public class LexerCodeGenerator {
    private final TemplateManager templateManager;
    private final String packageName;
    private final String grammarName;
    
    public GeneratedCode generateLexer(Grammar grammar)
    public String generateLexerClass(Grammar grammar)
    public String generateTokenizerEngine(Grammar grammar)
    public String generateLexicalException()
}
```

**Responsibilities:**
- Generate lexer class from grammar terminal definitions
- Create tokenizer engine with pattern matching logic
- Generate lexical error handling classes
- Coordinate with existing code generation infrastructure

### Enhanced JavaCodeGenerator

The existing `JavaCodeGenerator` will be modified to:

```java
public class JavaCodeGenerator {
    private final LexerCodeGenerator lexerGenerator; // new field
    
    // Modified method
    public GeneratedCode generate(Grammar grammar, ParsingTable parsingTable) {
        // Existing parser generation logic
        // + New lexer generation logic
    }
    
    // New method
    public GeneratedCode generateWithLexer(Grammar grammar, ParsingTable parsingTable)
}
```

### Generated Lexer Class Structure

The generated lexer will have this interface:

```java
public class [GrammarName]Lexer {
    public [GrammarName]Lexer(String input)
    public [GrammarName]Lexer(String input, String filename)
    public List<Token> tokenize()
    public Token nextToken()
    public boolean hasMoreTokens()
    public Position getCurrentPosition()
    public boolean hasErrors()
}
```

### TokenizerEngine

A runtime component embedded in generated lexers:

```java
public class TokenizerEngine {
    private final Map<String, Pattern> terminalPatterns;
    private final List<String> terminalOrder;
    
    public TokenMatch findNextToken(String input, int position)
    public boolean matches(String terminalName, String input, int position)
}
```

## Data Models

### TokenMatch

```java
public class TokenMatch {
    private final String terminalName;
    private final String matchedText;
    private final int startPosition;
    private final int endPosition;
    private final boolean isMatch;
}
```

### LexerConfiguration

```java
public class LexerConfiguration {
    private final Map<String, String> terminalPatterns;
    private final boolean ignoreWhitespace;
    private final boolean trackPositions;
    private final String commentPattern;
}
```

### Enhanced Terminal Class

The existing `Terminal` class already supports patterns, but we'll ensure it provides all needed information:

```java
public class Terminal extends Symbol {
    private final String pattern; // existing
    
    // Ensure these methods exist
    public boolean hasPattern()
    public String getPattern()
    public boolean isValidPattern()
}
```

## Error Handling

### Lexical Error Types

1. **Invalid Pattern Errors**: When terminal patterns contain invalid regex
2. **Tokenization Errors**: When input cannot be tokenized
3. **Template Generation Errors**: When lexer code generation fails
4. **Integration Errors**: When lexer and parser token types don't match

### Error Reporting Strategy

- Extend existing `ErrorReporter` to handle lexical generation errors
- Provide detailed error messages with position information
- Include suggestions for fixing common pattern issues
- Validate terminal patterns during grammar analysis phase

### Generated Lexer Error Handling

```java
public class LexicalException extends Exception {
    private final Position position;
    private final String unexpectedInput;
    private final String suggestion;
}
```

## Testing Strategy

### Unit Tests

1. **LexerCodeGenerator Tests**
   - Test lexer class generation with various terminal patterns
   - Test tokenizer engine generation
   - Test error handling during generation

2. **Generated Lexer Tests**
   - Test tokenization with simple patterns
   - Test complex regex patterns (character classes, quantifiers)
   - Test error handling for invalid input
   - Test position tracking accuracy

3. **Integration Tests**
   - Test lexer-parser integration with complete grammars
   - Test with existing example grammars (arithmetic, calculator, etc.)
   - Test error propagation between lexer and parser

### Template Tests

1. **Template Rendering Tests**
   - Test lexer template with various grammar configurations
   - Test template variable substitution
   - Test generated code compilation

2. **Template Validation Tests**
   - Validate generated lexer code syntax
   - Test generated code against style guidelines
   - Verify template error handling

### Regression Tests

1. **Existing Functionality Tests**
   - Ensure all existing parser generation tests pass
   - Verify no performance regression in parser generation
   - Test backward compatibility with existing grammars

2. **End-to-End Tests**
   - Generate and test complete parser+lexer for each example grammar
   - Test generated code compilation and execution
   - Verify parsing results match expected outcomes

## Implementation Phases

### Phase 1: Core Lexer Generation
- Implement `LexerCodeGenerator` class
- Create basic lexer template (`lexer_class.peb`)
- Add lexer generation to `JavaCodeGenerator`
- Implement basic tokenizer engine generation

### Phase 2: Pattern Processing
- Implement regex pattern validation
- Add support for all terminal pattern features
- Create tokenizer engine template
- Add comprehensive error handling

### Phase 3: Integration and Testing
- Integrate lexer generation with existing pipeline
- Add comprehensive test suite
- Test with all example grammars
- Performance optimization and validation

### Phase 4: Documentation and Polish
- Update documentation and examples
- Add lexer-specific CLI options if needed
- Final testing and bug fixes
- Code review and cleanup

## Template Design

### Lexer Class Template (lexer_class.peb)

```java
package {{ packageName }};

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class {{ grammarName }}Lexer {
    private final String input;
    private final String filename;
    private int position;
    private int line;
    private int column;
    
    // Terminal patterns
    {% for terminal in terminals %}
    private static final Pattern {{ terminal.constantName }} = Pattern.compile("{{ terminal.pattern }}");
    {% endfor %}
    
    // Pattern matching order (longest match first)
    private static final String[] TERMINAL_ORDER = {
        {% for terminal in terminalOrder %}
        "{{ terminal.name }}"{% if not loop.last %},{% endif %}
        {% endfor %}
    };
    
    public {{ grammarName }}Lexer(String input) {
        this(input, null);
    }
    
    public {{ grammarName }}Lexer(String input, String filename) {
        this.input = input != null ? input : "";
        this.filename = filename;
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }
    
    public List<Token> tokenize() throws LexicalException {
        // Implementation generated from template
    }
    
    public Token nextToken() throws LexicalException {
        // Implementation generated from template
    }
    
    // Additional methods...
}
```

### Integration Points

1. **CLI Integration**: Add lexer generation options to command line interface
2. **Maven Integration**: Ensure generated lexers compile with existing build process
3. **Example Integration**: Update example grammars to demonstrate lexer usage
4. **Documentation Integration**: Update API documentation to include lexer classes

## Performance Considerations

### Pattern Compilation
- Compile regex patterns once during lexer initialization
- Cache compiled patterns for reuse
- Optimize pattern matching order (longest match first)

### Memory Usage
- Minimize object creation during tokenization
- Reuse token objects where possible
- Efficient string handling for large inputs

### Tokenization Speed
- Use efficient regex matching algorithms
- Minimize backtracking in pattern matching
- Provide fast path for common token types

## Security Considerations

### Pattern Validation
- Validate terminal patterns for regex injection attacks
- Limit pattern complexity to prevent ReDoS attacks
- Sanitize user-provided patterns

### Generated Code Security
- Ensure generated lexer code doesn't introduce vulnerabilities
- Validate template inputs to prevent code injection
- Follow secure coding practices in generated code

## Backward Compatibility

### Existing Grammar Support
- All existing grammars must continue to work without modification
- Generated parsers must maintain same interface
- No breaking changes to existing APIs

### Migration Path
- Provide clear upgrade path for users wanting lexer generation
- Maintain support for parser-only generation
- Document differences between old and new approaches