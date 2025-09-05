# Example Grammars

This directory contains example grammars demonstrating various features of the Yacc-like Parser Generator.

## Grammar Files

- `arithmetic.grammar` - Basic arithmetic expressions with operator precedence
- `calculator.grammar` - Extended calculator with more operators and functions
- `simple-language.grammar` - Simple programming language subset
- `json.grammar` - JSON parser grammar
- `expression-evaluator.grammar` - Mathematical expression evaluator

## Usage

To generate a parser from any of these grammars:

```bash
java -jar yacc-like-parser-generator.jar <grammar-file>
```

For example:
```bash
java -jar yacc-like-parser-generator.jar examples/arithmetic.grammar
```

This will generate Java parser classes in the `generated/` directory.

## Grammar Format

All grammars follow the same format:
- Terminal definitions: `TerminalName = {Pattern}`
- Start symbol: `Start = <NonTerminal>`
- Production rules: `<NonTerminal> ::= <Rule1> | <Rule2> | ...`
- Comments: `-- comment text`

See the individual grammar files for specific examples.