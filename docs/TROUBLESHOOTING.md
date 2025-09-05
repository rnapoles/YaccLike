# Troubleshooting Guide

This guide helps you diagnose and fix common issues when using the Yacc-like Parser Generator.

## Common Grammar Issues

### 1. Left Recursion Errors

**Error Message:**
```
Left recursion detected: Expression -> Expression
Context: Production: Expression ::= Expression PLUS Term
```

**Cause:** Direct or indirect left recursion in grammar productions.

**Solutions:**

**Direct Left Recursion:**
```
-- WRONG: Left recursive
<Expression> ::= <Expression> PLUS <Term>

-- CORRECT: Right recursive
<Expression> ::= <Term> PLUS <Expression> | <Term>
```

**Indirect Left Recursion:**
```
-- WRONG: Indirect left recursion
<A> ::= <B> TERMINAL
<B> ::= <A> TERMINAL | TERMINAL

-- CORRECT: Eliminate the cycle
<A> ::= TERMINAL <A_Rest>
<A_Rest> ::= TERMINAL <A_Rest> | 
<B> ::= TERMINAL
```

### 2. Undefined Symbol Errors

**Error Message:**
```
Undefined symbol: UndefinedNonTerminal
Context: Production: Expression ::= UndefinedNonTerminal PLUS Term
```

**Cause:** Using symbols that haven't been defined.

**Solutions:**
- Define all non-terminals used in productions
- Check spelling of symbol names
- Ensure terminal symbols are defined with patterns

```
-- WRONG: UndefinedSymbol not defined
<Expression> ::= <UndefinedSymbol>

-- CORRECT: Define all symbols
TERMINAL = {pattern}
<Expression> ::= TERMINAL
```

### 3. LALR Conflicts

**Error Message:**
```
Shift/reduce conflict detected in state 5
Context: On lookahead ELSE, can shift or reduce by production 3
```

**Cause:** Grammar ambiguity that cannot be resolved by LALR(1) parsing.

**Common Cases and Solutions:**

**Dangling Else Problem:**
```
-- WRONG: Ambiguous if-else
<Statement> ::= IF <Expr> THEN <Statement>
             | IF <Expr> THEN <Statement> ELSE <Statement>

-- CORRECT: Restructure to remove ambiguity
<Statement> ::= <IfStatement>
<IfStatement> ::= IF <Expr> THEN <Statement> ELSE <Statement>
               | IF <Expr> THEN <MatchedStatement>
<MatchedStatement> ::= IF <Expr> THEN <MatchedStatement> ELSE <MatchedStatement>
                    | <OtherStatement>
```

**Expression Ambiguity:**
```
-- WRONG: Ambiguous precedence
<Expression> ::= <Expression> PLUS <Expression>
              | <Expression> MULTIPLY <Expression>
              | NUMBER

-- CORRECT: Use precedence levels
<Expression> ::= <Expression> PLUS <Term> | <Term>
<Term> ::= <Term> MULTIPLY <Factor> | <Factor>
<Factor> ::= NUMBER
```

### 4. Invalid Terminal Patterns

**Error Message:**
```
Invalid regular expression in terminal definition
Context: INVALID = {[}
```

**Cause:** Malformed regular expressions in terminal definitions.

**Common Issues and Fixes:**

**Unescaped Special Characters:**
```
-- WRONG: Special characters not escaped
PLUS = {+}        -- + has special meaning in regex
LPAREN = {(}      -- ( has special meaning in regex

-- CORRECT: Escape special characters
PLUS = {\+}       -- Literal plus sign
LPAREN = {\(}     -- Literal left parenthesis
```

**Invalid Character Classes:**
```
-- WRONG: Unclosed bracket
INVALID = {[a-z}

-- CORRECT: Properly closed bracket
VALID = {[a-z]}
```

**Invalid Quantifiers:**
```
-- WRONG: Quantifier without preceding element
INVALID = {+}

-- CORRECT: Quantifier after element
VALID = {[0-9]+}
```

### 5. Missing Start Symbol

**Error Message:**
```
No start symbol defined. Use 'Start = <NonTerminal>' to define one.
```

**Cause:** Grammar doesn't specify which non-terminal is the start symbol.

**Solution:**
```
-- Add start symbol definition
Start = <Program>

<Program> ::= <Statement>
<Statement> ::= IDENTIFIER
```

### 6. Empty Grammar

**Error Message:**
```
Grammar file is empty or contains no valid productions
```

**Cause:** Grammar file has no content or only comments.

**Solution:**
Ensure grammar has at least:
- One terminal definition
- Start symbol definition  
- One production rule

```
-- Minimal valid grammar
TERMINAL = {pattern}
Start = <Rule>
<Rule> ::= TERMINAL
```

## Lexical Analysis Issues

### 1. Unexpected Characters

**Error Message:**
```
Unexpected character '@' at line 5, column 10
Context: Character code: 64
```

**Cause:** Invalid characters in grammar file.

**Solutions:**
- Remove invalid characters
- Use only ASCII characters in grammar definitions
- Check for hidden Unicode characters

### 2. Malformed Terminal Definitions

**Error Message:**
```
Expected '{' after terminal name
Context: TERMINAL = pattern
```

**Cause:** Missing braces around terminal pattern.

**Solution:**
```
-- WRONG: Missing braces
TERMINAL = pattern

-- CORRECT: Pattern in braces
TERMINAL = {pattern}
```

## Parsing Issues

### 1. Invalid Production Syntax

**Error Message:**
```
Expected '::=' in production rule
Context: <NonTerminal> = TERMINAL
```

**Cause:** Using wrong operator in production rules.

**Solution:**
```
-- WRONG: Using = instead of ::=
<NonTerminal> = TERMINAL

-- CORRECT: Use ::= for productions
<NonTerminal> ::= TERMINAL
```

### 2. Missing Alternatives

**Error Message:**
```
Empty production rule
Context: <NonTerminal> ::=
```

**Cause:** Production rule with no right-hand side alternatives.

**Solutions:**
```
-- For epsilon production (empty):
<Optional> ::= 

-- For non-empty production:
<NonTerminal> ::= TERMINAL | <OtherRule>
```

## Code Generation Issues

### 1. Invalid Package Names

**Error Message:**
```
Invalid package name: 123invalid
```

**Cause:** Package name doesn't follow Java naming conventions.

**Solution:**
- Use valid Java identifiers
- Start with letter, use letters/digits/underscores
- Use lowercase with dots for separation

```bash
# WRONG
java -jar parser-generator.jar -p 123invalid grammar.txt

# CORRECT  
java -jar parser-generator.jar -p com.myapp.parser grammar.txt
```

### 2. File Permission Issues

**Error Message:**
```
Cannot write to output directory: /protected/path
```

**Cause:** Insufficient permissions to write generated files.

**Solutions:**
- Choose writable output directory
- Run with appropriate permissions
- Check disk space availability

## Performance Issues

### 1. Large Grammar Processing

**Symptoms:** Very slow processing or out of memory errors.

**Solutions:**
- Reduce grammar complexity
- Increase JVM heap size: `java -Xmx2g -jar parser-generator.jar`
- Simplify production rules
- Remove unnecessary non-terminals

### 2. Generated Parser Performance

**Symptoms:** Generated parser is slow or uses too much memory.

**Solutions:**
- Optimize grammar structure
- Reduce number of states in LALR table
- Consider alternative parsing techniques for complex grammars

## Debugging Strategies

### 1. Use Verbose Mode

Enable verbose output to see detailed processing:
```bash
java -jar parser-generator.jar -v grammar.txt
```

This shows:
- Tokenization progress
- Grammar parsing steps
- Analysis results
- Table construction details

### 2. Use Debug Mode

Enable debug mode for maximum detail:
```bash
java -jar parser-generator.jar -d grammar.txt
```

### 3. Incremental Testing

Start with minimal grammar and gradually add complexity:

1. **Basic grammar:**
```
TERMINAL = {pattern}
Start = <Rule>
<Rule> ::= TERMINAL
```

2. **Add more terminals:**
```
TERM1 = {pattern1}
TERM2 = {pattern2}
Start = <Rule>
<Rule> ::= TERM1 | TERM2
```

3. **Add non-terminals:**
```
TERM1 = {pattern1}
TERM2 = {pattern2}
Start = <Rule>
<Rule> ::= <SubRule> TERM1
<SubRule> ::= TERM2
```

### 4. Validate Regular Expressions

Test terminal patterns separately:
- Use online regex testers
- Verify patterns match expected input
- Check for proper escaping

### 5. Check Grammar Properties

Verify grammar characteristics:
- No left recursion
- All symbols defined
- Reasonable complexity
- Clear precedence rules

## Getting Help

If you encounter issues not covered here:

1. **Check error messages carefully** - they often contain specific guidance
2. **Review example grammars** - see working examples in `examples/` directory
3. **Examine generated code** - understand what the tool produces
4. **Test with simpler grammars** - isolate the problematic parts
5. **Consult the main documentation** - see `docs/README.md` for complete reference

## Error Message Reference

### Lexical Errors
- `Unexpected character 'X'` - Invalid character in grammar
- `Unterminated string` - Missing closing quote
- `Invalid escape sequence` - Bad escape in terminal pattern

### Syntax Errors  
- `Expected 'X' but found 'Y'` - Grammar syntax violation
- `Missing start symbol` - No Start = definition
- `Empty production rule` - Production with no alternatives

### Semantic Errors
- `Undefined symbol 'X'` - Symbol used but not defined
- `Left recursion detected` - Grammar has left recursion
- `Duplicate definition` - Symbol defined multiple times

### LALR Errors
- `Shift/reduce conflict` - Grammar ambiguity
- `Reduce/reduce conflict` - Multiple reductions possible
- `Grammar not LALR(1)` - Cannot build LALR table

### Generation Errors
- `Cannot write file` - File system permission issue
- `Invalid package name` - Bad Java package name
- `Template error` - Code generation template issue