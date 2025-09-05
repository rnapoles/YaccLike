# Expression Evaluator Sample

This is a sample Maven project demonstrating the use of the expression-evaluator grammar with a visitor pattern implementation for evaluating mathematical expressions.

## Features

- **AST (Abstract Syntax Tree)**: Complete node hierarchy for representing mathematical expressions
- **Visitor Pattern**: Clean separation of concerns for different operations on the AST
- **Expression Evaluation**: Full mathematical expression evaluator with support for:
  - Arithmetic operations: `+`, `-`, `*`, `/`, `%`, `^`
  - Mathematical functions: `sin`, `cos`, `tan`, `ln`, `log`, `sqrt`, `abs`
  - Constants: `pi`, `e`
  - Variables with runtime binding
- **Pretty Printing**: Formatted output of expressions for debugging and display
- **Comprehensive Testing**: Unit tests covering all functionality

## Project Structure

```
src/main/java/com/example/expression/
├── ast/                          # AST node definitions
│   ├── ASTNode.java             # Base interface for all AST nodes
│   ├── ASTVisitor.java          # Visitor interface
│   ├── BinaryOperationNode.java # Binary operations (+, -, *, etc.)
│   ├── UnaryOperationNode.java  # Unary operations (-, +)
│   ├── NumberNode.java          # Numeric literals
│   ├── IdentifierNode.java      # Variable references
│   ├── ConstantNode.java        # Mathematical constants (pi, e)
│   └── FunctionCallNode.java    # Function calls
├── evaluator/
│   └── ExpressionEvaluator.java # Visitor implementation for evaluation
├── printer/
│   └── PrettyPrinter.java       # Visitor implementation for formatting
└── ExpressionEvaluatorApp.java  # Main application with demos

src/test/java/                    # Unit tests
└── com/example/expression/
    ├── evaluator/
    │   └── ExpressionEvaluatorTest.java
    └── printer/
        └── PrettyPrinterTest.java

src/main/resources/
└── expression-evaluator.grammar  # Grammar definition
```

## Grammar Features

The expression-evaluator grammar supports:

### Operators (by precedence)
1. `^` (exponentiation, right-associative)
2. `*`, `/`, `%` (multiplication, division, modulo)
3. `+`, `-` (addition, subtraction)
4. Unary `-`, `+`

### Functions
- **Trigonometric**: `sin(x)`, `cos(x)`, `tan(x)`
- **Logarithmic**: `ln(x)`, `log(x)` (base 10), `log(base, value)`
- **Other**: `sqrt(x)`, `abs(x)`

### Constants
- `pi` (π ≈ 3.14159...)
- `e` (Euler's number ≈ 2.71828...)

### Variables
- Any identifier matching `[a-zA-Z][a-zA-Z0-9]*`
- Runtime binding through the evaluator

## Building and Running

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- The yacc-like-parser-generator project built and available

### Build the project
```bash
cd expression-evaluator-sample
mvn clean compile
```

### Run tests
```bash
mvn test
```

### Generate parser (if needed)
```bash
mvn exec:java@generate-parser
```

### Run the demo application
```bash
mvn exec:java -Dexec.mainClass="com.example.expression.ExpressionEvaluatorApp"
```

## Usage Examples

### Basic Evaluation
```java
ExpressionEvaluator evaluator = new ExpressionEvaluator();

// Set variables
evaluator.setVariable("x", 5.0);
evaluator.setVariable("y", 3.0);

// Create expression: sqrt(x^2 + y^2)
ASTNode expr = new FunctionCallNode("sqrt", Arrays.asList(
    new BinaryOperationNode(
        new BinaryOperationNode(new IdentifierNode("x"), "^", new NumberNode(2)),
        "+",
        new BinaryOperationNode(new IdentifierNode("y"), "^", new NumberNode(2))
    )
));

// Evaluate
double result = evaluator.evaluate(expr); // Result: 5.830951895...
```

### Pretty Printing
```java
PrettyPrinter printer = new PrettyPrinter();
String formatted = printer.format(expr); // "sqrt(x ^ 2 + y ^ 2)"
```

### Error Handling
The evaluator handles various error conditions:
- Division by zero
- Invalid function arguments (e.g., `sqrt(-1)`, `ln(0)`)
- Undefined variables
- Unknown functions or operators

## Visitor Pattern Implementation

The project demonstrates the visitor pattern with two concrete visitors:

1. **ExpressionEvaluator**: Computes numerical results
2. **PrettyPrinter**: Generates formatted string representations

Adding new operations is straightforward:
1. Implement the `ASTVisitor<T>` interface
2. Provide implementations for each `visit*` method
3. Use `node.accept(visitor)` to traverse the AST

## Testing

The project includes comprehensive unit tests covering:
- Individual AST node evaluation
- All mathematical operations and functions
- Error conditions and edge cases
- Complex nested expressions
- Variable binding and scoping
- Pretty printing functionality

Run tests with: `mvn test`

## Integration with Parser Generator

This sample project is designed to work with the yacc-like parser generator. In a complete implementation:

1. The grammar file would be processed by the parser generator
2. Generated lexer and parser classes would create AST nodes
3. The visitor implementations would operate on the generated AST

The current implementation manually constructs AST nodes for demonstration purposes, but the structure is designed to integrate seamlessly with generated parsers.

## Extension Points

The architecture supports easy extension:

- **New operators**: Add cases to `BinaryOperationNode` and `UnaryOperationNode` handling
- **New functions**: Extend the function call handling in `ExpressionEvaluator`
- **New node types**: Implement `ASTNode` and add corresponding visitor methods
- **New visitors**: Implement `ASTVisitor<T>` for operations like:
  - Symbolic differentiation
  - Expression optimization
  - Code generation
  - Dependency analysis