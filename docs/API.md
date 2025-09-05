# API Reference

This document provides detailed information about the public API of the Yacc-like Parser Generator.

## Core Classes

### ParserGenerator

Main entry point for parser generation.

```java
package com.parsergenerator;

public class ParserGenerator {
    public ParserGenerator()
    public boolean generateParser(String grammarFile)
}
```

**Usage:**
```java
ParserGenerator generator = new ParserGenerator();
boolean success = generator.generateParser("arithmetic.grammar");
```

### Grammar

Represents a parsed context-free grammar.

```java
package com.parsergenerator.grammar;

public class Grammar {
    public Set<Terminal> getTerminals()
    public Set<NonTerminal> getNonTerminals()
    public Set<Production> getProductions()
    public NonTerminal getStartSymbol()
    public boolean hasStartSymbol()
    public Symbol getSymbol(String name)
}
```

**Usage:**
```java
Grammar grammar = parser.parse();
Set<Terminal> terminals = grammar.getTerminals();
NonTerminal startSymbol = grammar.getStartSymbol();
```

### Production

Represents a grammar production rule.

```java
package com.parsergenerator.grammar;

public class Production {
    public NonTerminal getLeftSide()
    public List<Symbol> getRightSide()
    public int getId()
    public boolean isEmpty()
    public boolean equals(Object obj)
    public int hashCode()
    public String toString()
}
```

**Usage:**
```java
Production prod = new Production(leftSide, rightSide);
NonTerminal lhs = prod.getLeftSide();
List<Symbol> rhs = prod.getRightSide();
```

## Symbol Classes

### Symbol (Abstract Base Class)

Base class for all grammar symbols.

```java
package com.parsergenerator.symbol;

public abstract class Symbol {
    public String getName()
    public SymbolType getType()
    public boolean isTerminal()
    public boolean isNonTerminal()
    public boolean equals(Object obj)
    public int hashCode()
    public String toString()
}
```

### Terminal

Represents terminal symbols (tokens).

```java
package com.parsergenerator.symbol;

public class Terminal extends Symbol {
    public Terminal(String name, String pattern)
    public String getPattern()
}
```

**Usage:**
```java
Terminal number = new Terminal("NUMBER", "[0-9]+");
String pattern = number.getPattern();
```

### NonTerminal

Represents non-terminal symbols.

```java
package com.parsergenerator.symbol;

public class NonTerminal extends Symbol {
    public NonTerminal(String name)
    public Set<Production> getProductions()
    public void addProduction(Production production)
}
```

**Usage:**
```java
NonTerminal expr = new NonTerminal("Expression");
expr.addProduction(production);
```

## Lexer Classes

### GrammarLexer

Tokenizes grammar input files.

```java
package com.parsergenerator.lexer;

public class GrammarLexer {
    public GrammarLexer(String input, String fileName, ErrorReporter errorReporter)
    public List<Token> tokenize()
}
```

**Usage:**
```java
GrammarLexer lexer = new GrammarLexer(grammarText, "file.grammar", errorReporter);
List<Token> tokens = lexer.tokenize();
```

### Token

Represents a lexical token.

```java
package com.parsergenerator.lexer;

public class Token {
    public Token(TokenType type, String value, Position position)
    public TokenType getType()
    public String getValue()
    public Position getPosition()
    public String toString()
}
```

### TokenType

Enumeration of token types.

```java
package com.parsergenerator.lexer;

public enum TokenType {
    TERMINAL_NAME,
    NON_TERMINAL,
    EQUALS,
    PRODUCTION_ARROW,
    PIPE,
    LBRACE,
    RBRACE,
    PATTERN,
    START_KEYWORD,
    COMMENT,
    EOF
}
```

### Position

Tracks source position for error reporting.

```java
package com.parsergenerator.lexer;

public class Position {
    public Position(int line, int column)
    public int getLine()
    public int getColumn()
    public String toString()
}
```

## Parser Classes

### GrammarParser

Parses tokenized grammar into Grammar objects.

```java
package com.parsergenerator.parser;

public class GrammarParser {
    public GrammarParser(List<Token> tokens, ErrorReporter errorReporter)
    public Grammar parse() throws ParseException
}
```

**Usage:**
```java
GrammarParser parser = new GrammarParser(tokens, errorReporter);
try {
    Grammar grammar = parser.parse();
} catch (ParseException e) {
    System.err.println("Parse error: " + e.getMessage());
}
```

### ParseException

Exception thrown during parsing errors.

```java
package com.parsergenerator.parser;

public class ParseException extends Exception {
    public ParseException(String message)
    public ParseException(String message, Throwable cause)
}
```

## Analysis Classes

### GrammarAnalyzer

Performs semantic analysis on grammars.

```java
package com.parsergenerator.analysis;

public class GrammarAnalyzer {
    public GrammarAnalyzer(Grammar grammar, ErrorReporter errorReporter)
    public AnalysisResult analyze()
}
```

**Usage:**
```java
GrammarAnalyzer analyzer = new GrammarAnalyzer(grammar, errorReporter);
AnalysisResult result = analyzer.analyze();
```

### AnalysisResult

Contains results of grammar analysis.

```java
package com.parsergenerator.analysis;

public class AnalysisResult {
    public List<String> getErrors()
    public List<String> getWarnings()
    public boolean hasErrors()
    public boolean hasWarnings()
    public int getErrorCount()
    public int getWarningCount()
    public boolean isSuccessful()
}
```

**Usage:**
```java
AnalysisResult result = analyzer.analyze();
if (result.hasErrors()) {
    for (String error : result.getErrors()) {
        System.err.println("Error: " + error);
    }
}
```

## LALR Classes

### LALRTableBuilder

Constructs LALR parsing tables.

```java
package com.parsergenerator.lalr;

public class LALRTableBuilder {
    public LALRTableBuilder(Grammar grammar, AnalysisResult analysis, ErrorReporter errorReporter)
    public ParsingTable buildTable()
}
```

**Usage:**
```java
LALRTableBuilder builder = new LALRTableBuilder(grammar, analysis, errorReporter);
ParsingTable table = builder.buildTable();
```

### ParsingTable

Contains LALR parsing tables.

```java
package com.parsergenerator.lalr;

public class ParsingTable {
    public Map<StateSymbolPair, Action> getActionTable()
    public Map<StateSymbolPair, Integer> getGotoTable()
    public List<LRItemSet> getStates()
    public int getStateCount()
}
```

### Action

Represents parser actions (shift, reduce, accept, error).

```java
package com.parsergenerator.lalr;

public class Action {
    public ActionType getType()
    public int getParameter()
    public String toString()
}
```

### ActionType

Enumeration of parser action types.

```java
package com.parsergenerator.lalr;

public enum ActionType {
    SHIFT,
    REDUCE,
    ACCEPT,
    ERROR
}
```

## Code Generation Classes

### JavaCodeGenerator

Generates Java parser code.

```java
package com.parsergenerator.codegen;

public class JavaCodeGenerator {
    public JavaCodeGenerator(String packageName, String grammarName)
    public GeneratedCode generate(Grammar grammar, ParsingTable table) throws TemplateException
}
```

**Usage:**
```java
JavaCodeGenerator generator = new JavaCodeGenerator("com.myapp.parser", "MyGrammar");
GeneratedCode code = generator.generate(grammar, table);
```

### GeneratedCode

Container for generated code files.

```java
package com.parsergenerator.codegen;

public class GeneratedCode {
    public void addFile(String fileName, String content)
    public String getFileContent(String fileName)
    public boolean hasFile(String fileName)
    public Set<String> getFileNames()
    public Map<String, String> getAllFiles()
    public int getFileCount()
    public boolean isEmpty()
    public int getTotalSize()
}
```

**Usage:**
```java
GeneratedCode code = generator.generate(grammar, table);
for (String fileName : code.getFileNames()) {
    String content = code.getFileContent(fileName);
    // Write to file system
}
```

## Error Handling Classes

### ErrorReporter

Centralized error reporting system.

```java
package com.parsergenerator.error;

public class ErrorReporter {
    public void reportError(ErrorType type, String message, Position position)
    public void reportWarning(String message, Position position)
    public boolean hasErrors()
    public boolean hasWarnings()
    public int getErrorCount()
    public int getWarningCount()
    public List<ErrorReport> getErrors()
    public List<ErrorReport> getWarnings()
    public String generateSummary()
    public String generateDetailedReport()
    public void clear()
}
```

**Usage:**
```java
ErrorReporter reporter = new ErrorReporter();
reporter.reportError(ErrorType.SYNTAX, "Invalid token", position);
if (reporter.hasErrors()) {
    System.err.println(reporter.generateDetailedReport());
}
```

### ErrorReport

Individual error report.

```java
package com.parsergenerator.error;

public class ErrorReport {
    public ErrorType getType()
    public Severity getSeverity()
    public String getMessage()
    public Position getPosition()
    public String getFileName()
    public String getSuggestion()
    public String toString()
}
```

## Utility Classes

### SymbolUtils

Utility methods for working with symbols.

```java
package com.parsergenerator.symbol;

public class SymbolUtils {
    public static boolean isValidTerminalName(String name)
    public static boolean isValidNonTerminalName(String name)
    public static String formatSymbolName(String name)
}
```

## Exception Classes

### TemplateException

Thrown during code generation template processing.

```java
package com.parsergenerator.codegen;

public class TemplateException extends Exception {
    public TemplateException(String message)
    public TemplateException(String message, Throwable cause)
}
```

### SyntaxErrorException

Thrown for grammar syntax errors.

```java
package com.parsergenerator.parser;

public class SyntaxErrorException extends ParseException {
    public SyntaxErrorException(String message, Position position)
    public Position getPosition()
}
```

### SemanticErrorException

Thrown for grammar semantic errors.

```java
package com.parsergenerator.parser;

public class SemanticErrorException extends ParseException {
    public SemanticErrorException(String message)
}
```

## Usage Examples

### Complete Parser Generation

```java
import com.parsergenerator.*;
import com.parsergenerator.grammar.*;
import com.parsergenerator.lexer.*;
import com.parsergenerator.parser.*;
import com.parsergenerator.analysis.*;
import com.parsergenerator.lalr.*;
import com.parsergenerator.codegen.*;
import com.parsergenerator.error.*;

public class ParserGenerationExample {
    public static void main(String[] args) {
        try {
            // Read grammar file
            String grammarText = Files.readString(Paths.get("arithmetic.grammar"));
            
            // Create error reporter
            ErrorReporter errorReporter = new ErrorReporter();
            
            // Tokenize
            GrammarLexer lexer = new GrammarLexer(grammarText, "arithmetic.grammar", errorReporter);
            List<Token> tokens = lexer.tokenize();
            
            if (errorReporter.hasErrors()) {
                System.err.println("Lexical errors:");
                System.err.println(errorReporter.generateDetailedReport());
                return;
            }
            
            // Parse
            GrammarParser parser = new GrammarParser(tokens, errorReporter);
            Grammar grammar = parser.parse();
            
            // Analyze
            GrammarAnalyzer analyzer = new GrammarAnalyzer(grammar, errorReporter);
            AnalysisResult analysis = analyzer.analyze();
            
            if (analysis.hasErrors()) {
                System.err.println("Grammar analysis errors:");
                for (String error : analysis.getErrors()) {
                    System.err.println(error);
                }
                return;
            }
            
            // Build LALR table
            LALRTableBuilder tableBuilder = new LALRTableBuilder(grammar, analysis, errorReporter);
            ParsingTable table = tableBuilder.buildTable();
            
            // Generate code
            JavaCodeGenerator codeGenerator = new JavaCodeGenerator("com.example.parser", "ArithmeticParser");
            GeneratedCode code = codeGenerator.generate(grammar, table);
            
            // Write files
            for (String fileName : code.getFileNames()) {
                Path outputPath = Paths.get("generated", fileName);
                Files.createDirectories(outputPath.getParent());
                Files.writeString(outputPath, code.getFileContent(fileName));
            }
            
            System.out.println("Parser generated successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

### Using Generated Parser

```java
import com.example.parser.*;

public class ParserUsageExample {
    public static void main(String[] args) {
        try {
            // Create parser instance
            ArithmeticParserParser parser = new ArithmeticParserParser();
            
            // Parse input
            String input = "2 + 3 * 4";
            ASTNode ast = parser.parse(input);
            
            // Process AST using visitor pattern
            MyASTVisitor visitor = new MyASTVisitor();
            ast.accept(visitor);
            
        } catch (ParseException e) {
            System.err.println("Parse error: " + e.getMessage());
        }
    }
    
    private static class MyASTVisitor implements ASTVisitor {
        @Override
        public void visit(ExpressionNode node) {
            System.out.println("Visiting expression: " + node);
            // Process expression node
        }
        
        @Override
        public void visit(TermNode node) {
            System.out.println("Visiting term: " + node);
            // Process term node
        }
        
        @Override
        public void visit(FactorNode node) {
            System.out.println("Visiting factor: " + node);
            // Process factor node
        }
        
        @Override
        public void visit(TerminalNode node) {
            System.out.println("Visiting terminal: " + node.getToken());
            // Process terminal node
        }
    }
}
```

This API reference provides the essential classes and methods for working with the Yacc-like Parser Generator programmatically. For complete details, refer to the JavaDoc comments in the source code.