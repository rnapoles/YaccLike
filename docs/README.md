# Yacc-like Parser Generator Documentation

A comprehensive LALR(1) parser generator that transforms context-free grammar specifications into Java parsers.

## Table of Contents

- [Quick Start](#quick-start)
- [Grammar Syntax](#grammar-syntax)
- [Command Line Usage](#command-line-usage)
- [Generated Code](#generated-code)
- [Examples](#examples)
- [Troubleshooting](#troubleshooting)
- [API Reference](#api-reference)

## Quick Start

1. **Create a grammar file** (e.g., `arithmetic.grammar`):
```
NUMBER = {[0-9]+}
PLUS = {\+}
MULTIPLY = {\*}
LPAREN = {\(}
RPAREN = {\)}

Start = <Expression>

<Expression> ::= <Term> PLUS <Expression> | <Term>
<Term> ::= <Factor> MULTIPLY <Term> | <Factor>
<Factor> ::= NUMBER | LPAREN <Expression> RPAREN
```

2. **Generate the parser**:
```bash
java -jar yacc-like-parser-generator.jar arithmetic.grammar
```

3. **Use the generated parser** in your Java code:
```java
// Generated parser will be in com.generated.parser package
ArithmeticParser parser = new ArithmeticParser();
ASTNode result = parser.parse("2 + 3 * 4");
```

## Grammar Syntax

### Terminal Definitions

Define terminal symbols (tokens) using regular expressions:

```
TerminalName = {RegularExpression}
```

Examples:
```
NUMBER = {[0-9]+}                    -- Integer numbers
IDENTIFIER = {[a-zA-Z][a-zA-Z0-9]*}  -- Variable names
STRING = {"[^"]*"}                   -- Quoted strings
PLUS = {\+}                          -- Literal plus sign (escaped)
```

### Start Symbol

Define the grammar's start symbol:
```
Start = <NonTerminalName>
```

### Production Rules

Define grammar productions using BNF-like syntax:
```
<NonTerminal> ::= <Rule1> | <Rule2> | <Rule3>
```

Examples:
```
<Expression> ::= <Term> PLUS <Expression> | <Term>
<Statement> ::= <Assignment> | <IfStatement> | <Block>
<OptionalItem> ::=  -- Empty production (epsilon)
```

### Comments

Add comments using double dashes:
```
-- This is a comment
<Rule> ::= TERMINAL  -- End-of-line comment
```

### Special Characters

Escape special regex characters in terminal patterns:
- `\+` for literal `+`
- `\*` for literal `*`
- `\(` and `\)` for literal parentheses
- `\{` and `\}` for literal braces
- `\\` for literal backslash

## Command Line Usage

### Basic Usage
```bash
java -jar yacc-like-parser-generator.jar <grammar-file>
```

### Options
```bash
java -jar yacc-like-parser-generator.jar [OPTIONS] <grammar-file>

Options:
  -h, --help              Show help message
  -v, --verbose           Enable verbose output
  -d, --debug             Enable debug output
  -o, --output DIR        Output directory (default: generated)
  -p, --package PACKAGE   Package name (default: com.generated.parser)
  -n, --name NAME         Grammar name (default: GeneratedGrammar)
```

### Examples
```bash
# Basic generation
java -jar parser-generator.jar arithmetic.grammar

# Custom output directory and package
java -jar parser-generator.jar -o src/main/java -p com.myapp.parser arithmetic.grammar

# Verbose output for debugging
java -jar parser-generator.jar -v -d arithmetic.grammar
```

## Generated Code

The parser generator creates several Java files:

### Main Parser Class
- `<GrammarName>Parser.java` - Main parser with `parse()` method
- Contains embedded LALR parsing tables
- Provides error recovery and reporting

### Token Classes
- `TokenType.java` - Enum of all terminal symbols
- `Token.java` - Token representation with type and value
- `Position.java` - Source position tracking

### AST Node Classes
- `ASTNode.java` - Base class for all AST nodes
- `<NonTerminal>Node.java` - Node class for each non-terminal
- `TerminalNode.java` - Node class for terminal symbols
- `ASTVisitor.java` - Visitor pattern interface

### Exception Classes
- `ParseException.java` - Thrown on parsing errors

### Example Usage
```java
import com.generated.parser.*;

public class ParserExample {
    public static void main(String[] args) {
        try {
            ArithmeticParser parser = new ArithmeticParser();
            ASTNode ast = parser.parse("2 + 3 * 4");
            
            // Use visitor pattern to process AST
            MyVisitor visitor = new MyVisitor();
            ast.accept(visitor);
            
        } catch (ParseException e) {
            System.err.println("Parse error: " + e.getMessage());
        }
    }
}
```

## Examples

See the `examples/` directory for complete grammar examples:

- **arithmetic.grammar** - Basic arithmetic with precedence
- **calculator.grammar** - Extended calculator with functions
- **simple-language.grammar** - Simple programming language
- **json.grammar** - JSON parser
- **expression-evaluator.grammar** - Mathematical expressions

## Troubleshooting

### Common Grammar Issues

#### Left Recursion
**Problem**: Grammar has left recursion
```
<Expr> ::= <Expr> PLUS <Term>  -- Left recursive
```

**Solution**: Convert to right recursion
```
<Expr> ::= <Term> PLUS <Expr> | <Term>  -- Right recursive
```

#### Undefined Symbols
**Problem**: Using undefined non-terminals or terminals
```
<Expr> ::= <UndefinedSymbol>  -- Error: UndefinedSymbol not defined
```

**Solution**: Define all symbols used in productions

#### LALR Conflicts
**Problem**: Grammar has shift/reduce or reduce/reduce conflicts

**Solutions**:
- Restructure grammar to remove ambiguity
- Add precedence rules (future feature)
- Use different parsing technique

#### Invalid Terminal Patterns
**Problem**: Invalid regular expressions in terminal definitions
```
INVALID = {[}  -- Unclosed bracket
```

**Solution**: Use valid regex patterns and escape special characters

### Error Messages

The parser generator provides detailed error messages:

- **Lexical errors**: Invalid characters or malformed tokens
- **Syntax errors**: Grammar doesn't follow expected format
- **Semantic errors**: Undefined symbols, left recursion
- **LALR conflicts**: Grammar not suitable for LALR parsing

### Debugging Tips

1. **Use verbose mode** (`-v`) to see detailed processing information
2. **Check terminal patterns** - ensure regex is valid and escaped properly
3. **Verify grammar structure** - all symbols defined, no left recursion
4. **Test with simple inputs** - start with minimal grammar and expand
5. **Review generated code** - examine parsing tables and AST structure

## API Reference

### ParserGenerator Class

Main class for programmatic parser generation:

```java
ParserGenerator generator = new ParserGenerator();
boolean success = generator.generateParser("grammar.txt");
```

### Grammar Classes

- `Grammar` - Represents parsed grammar structure
- `Production` - Individual grammar production rule
- `Symbol`, `Terminal`, `NonTerminal` - Grammar symbols

### Analysis Classes

- `GrammarAnalyzer` - Performs grammar validation and analysis
- `AnalysisResult` - Contains analysis results and errors
- `FirstFollowCalculator` - Computes FIRST and FOLLOW sets

### LALR Classes

- `LALRTableBuilder` - Constructs LALR parsing tables
- `ParsingTable` - Contains ACTION and GOTO tables
- `LRItem`, `LRItemSet` - LR items and item sets

### Code Generation Classes

- `JavaCodeGenerator` - Generates Java parser code
- `GeneratedCode` - Container for generated code files
- `TemplateManager` - Manages code generation templates

### Error Handling

- `ErrorReporter` - Centralized error reporting
- `ErrorReport` - Individual error report
- Various exception classes for different error types

For detailed API documentation, see the JavaDoc comments in the source code.

## Grammar Format Reference

### Complete Grammar Structure
```
-- Comments start with double dash

-- Terminal definitions (required)
TERMINAL1 = {regex_pattern}
TERMINAL2 = {another_pattern}

-- Start symbol definition (required)
Start = <StartNonTerminal>

-- Production rules (required)
<NonTerminal1> ::= <Rule1> | <Rule2>
<NonTerminal2> ::= TERMINAL1 <NonTerminal1>
<NonTerminal3> ::=  -- Empty production
```

### Regular Expression Patterns

Supported regex features:
- Character classes: `[a-zA-Z]`, `[0-9]`
- Quantifiers: `+` (one or more), `*` (zero or more), `?` (optional)
- Grouping: `(pattern)`
- Alternation: `pattern1|pattern2`
- Escape sequences: `\+`, `\*`, `\(`, `\)`, etc.

### Production Rule Syntax

- `::=` separates left-hand side from right-hand side
- `|` separates alternative productions
- `<NonTerminal>` for non-terminal symbols
- `TERMINAL` for terminal symbols
- Empty right-hand side creates epsilon production

This documentation provides comprehensive guidance for using the Yacc-like Parser Generator effectively.