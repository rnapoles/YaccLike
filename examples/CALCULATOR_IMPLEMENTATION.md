# Calculator Implementation with Yacc-like Parser Generator

This directory demonstrates how to implement a calculator using the Yacc-like Parser Generator. The implementation shows both the grammar definition and the visitor pattern for evaluating expressions.

## Files

### Grammar Definition
- `calculator.grammar` - Complete grammar definition for the calculator language

### Implementation Files
- `SimpleCalculatorDemo.java` - Standalone calculator demonstrating evaluation concepts
- `SimpleCalculatorDemoTest.java` - Comprehensive test suite

### Generated Parser Integration
The complete implementation would use the generated parser classes:
- `CalculatorVisitor.java` - Visitor implementation for the generated AST
- `CalculatorVisitorTest.java` - Tests for the visitor pattern
- `CalculatorExample.java` - Interactive demo using generated parser

## Grammar Features

The calculator grammar supports:

### Arithmetic Operations
- Addition (`+`) and Subtraction (`-`)
- Multiplication (`*`) and Division (`/`)
- Exponentiation (`^`) - right associative
- Unary minus (`-x`)

### Operator Precedence (from highest to lowest)
1. Parentheses `()`
2. Unary minus `-`
3. Exponentiation `^` (right associative)
4. Multiplication `*`, Division `/`
5. Addition `+`, Subtraction `-`

### Variables and Assignment
- Variable assignment: `variable = expression;`
- Variable usage in expressions
- Predefined constants: `PI`, `E`

### Function Calls
- `sin(x)`, `cos(x)`, `tan(x)` - Trigonometric functions
- `sqrt(x)` - Square root
- `abs(x)` - Absolute value
- `log(x)` - Base-10 logarithm
- `exp(x)` - Exponential function

### Program Structure
- Multiple statements separated by semicolons
- Expressions and assignments
- Support for complex nested expressions

## Grammar Definition

```grammar
-- Extended Calculator Grammar
-- Supports multiple operators, functions, and variables

NUMBER = {[0-9]+(\.[0-9]+)?}
IDENTIFIER = {[a-zA-Z][a-zA-Z0-9]*}
PLUS = {\+}
MINUS = {\-}
MULTIPLY = {\*}
DIVIDE = {/}
POWER = {\^}
LPAREN = {\(}
RPAREN = {\)}
COMMA = {,}
ASSIGN = {=}
SEMICOLON = {;}

Start = <Program>

-- Program can be a sequence of statements
<Program> ::= <Statement> | <Statement> <Program>

-- Statements can be assignments or expressions
<Statement> ::= <Assignment> | <Expression> SEMICOLON

-- Variable assignment
<Assignment> ::= IDENTIFIER ASSIGN <Expression> SEMICOLON

-- Expression with multiple operators and precedence
<Expression> ::= <Term> PLUS <Expression> | <Term> MINUS <Expression> | <Term>
<Term> ::= <Factor> MULTIPLY <Term> | <Factor> DIVIDE <Term> | <Factor>
<Factor> ::= <Base> POWER <Factor> | <Base>
<Base> ::= NUMBER | IDENTIFIER | <FunctionCall> | LPAREN <Expression> RPAREN | MINUS <Base>

-- Function calls with arguments
<FunctionCall> ::= IDENTIFIER LPAREN <ArgumentList> RPAREN
<ArgumentList> ::= <Expression> | <Expression> COMMA <ArgumentList>
```

## Usage Examples

### Basic Arithmetic
```
2 + 3 * 4        // Result: 14 (precedence: multiplication first)
(2 + 3) * 4      // Result: 20 (parentheses override precedence)
2^3^2            // Result: 512 (right associative: 2^(3^2))
-5 + 3           // Result: -2 (unary minus)
```

### Variables
```
x = 10;          // Assign 10 to variable x
y = x * 2 + 5;   // y = 25 (using variable x)
z = x + y;       // z = 35 (using both variables)
```

### Functions
```
sqrt(16);        // Result: 4.0
sin(PI/2);       // Result: 1.0 (using PI constant)
log(100);        // Result: 2.0 (base-10 logarithm)
abs(-5);         // Result: 5.0
```

### Complex Expressions
```
radius = 5;
area = PI * radius^2;           // Circle area
circumference = 2 * PI * radius; // Circle circumference
hypotenuse = sqrt(3^2 + 4^2);   // Pythagorean theorem
```

## Implementation Architecture

### 1. Grammar-to-Parser Generation
The Yacc-like Parser Generator processes the grammar file and generates:
- Lexer for tokenizing input
- LALR parser for syntax analysis
- AST node classes for each non-terminal
- Visitor interface for tree traversal

### 2. Visitor Pattern Implementation
The `CalculatorVisitor` implements the visitor interface to:
- Traverse the generated AST
- Evaluate expressions according to operator precedence
- Manage variable storage and function calls
- Handle error conditions (division by zero, undefined variables)

### 3. Expression Evaluation
The visitor evaluates expressions by:
- Visiting terminal nodes (numbers, identifiers)
- Applying operators based on AST structure
- Calling mathematical functions
- Managing variable assignments and lookups

## Testing Strategy

The implementation includes comprehensive tests for:
- Basic arithmetic operations
- Operator precedence rules
- Variable management
- Function calls
- Error handling
- Complex nested expressions

## Running the Calculator

### Interactive Mode
```bash
java com.parsergenerator.examples.SimpleCalculatorDemo
```

### Programmatic Usage
```java
SimpleCalculatorDemo calc = new SimpleCalculatorDemo();
double result = calc.evaluate("2 + 3 * 4");
calc.evaluate("x = 10");
double value = calc.getVariable("x");
```

## Integration with Generated Parser

The complete implementation would integrate with the generated parser:

1. **Parse Input**: Use generated lexer and parser to create AST
2. **Visit AST**: Apply calculator visitor to evaluate expressions
3. **Return Results**: Extract computed values and variable states

```java
// Pseudo-code for complete integration
CalculatorParser parser = new CalculatorParser();
ASTNode ast = parser.parse("x = 2 + 3 * 4;");
CalculatorVisitor visitor = new CalculatorVisitor();
Double result = ast.accept(visitor);
```

This demonstrates the power of the Yacc-like Parser Generator in creating domain-specific languages with proper syntax analysis and semantic evaluation.