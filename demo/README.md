# Expression Evaluator Demo

This is a demonstration Maven project that showcases the yacc-like-parser-generator tool by creating a complete mathematical expression evaluator.

## Project Structure

```
demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/parsergenerator/demo/
│   │   │       ├── generated/     # Generated parser classes
│   │   │       ├── evaluator/     # Expression evaluator and visitor classes
│   │   │       ├── context/       # Variable context and function library
│   │   │       └── exception/     # Custom exception classes
│   │   └── resources/
│   │       └── expression-evaluator.grammar  # Grammar file for parser generation
│   └── test/
│       └── java/
│           └── com/parsergenerator/demo/  # Test classes
├── pom.xml                        # Maven configuration
└── README.md                      # This file
```

## Building the Project

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Package the project
mvn package
```

## Features

The expression evaluator will support:
- Basic arithmetic operations (+, -, *, /, %, ^)
- Mathematical functions (sin, cos, tan, log, ln, sqrt, abs)
- Mathematical constants (pi, e)
- Variable assignment and lookup
- Proper operator precedence
- Parentheses for grouping

## Development Status

This project is currently under development as part of the yacc-like-parser-generator demonstration.