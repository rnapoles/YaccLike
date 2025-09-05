package com.parsergenerator;

import com.parsergenerator.analysis.AnalysisResult;
import com.parsergenerator.analysis.GrammarAnalyzer;
import com.parsergenerator.codegen.GeneratedCode;
import com.parsergenerator.codegen.JavaCodeGenerator;
import com.parsergenerator.error.ErrorReport;
import com.parsergenerator.error.ErrorReporter;
import com.parsergenerator.grammar.Grammar;
import com.parsergenerator.lalr.LALRTableBuilder;
import com.parsergenerator.lalr.ParsingTable;
import com.parsergenerator.lexer.GrammarLexer;
import com.parsergenerator.lexer.Token;
import com.parsergenerator.parser.GrammarParser;
import com.parsergenerator.parser.ParseException;

import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * Main application class for the Yacc-like parser generator.
 * Coordinates all components to transform grammar specifications into Java parsers.
 * 
 * This class provides the main entry point and orchestrates the complete pipeline:
 * 1. Grammar file parsing and lexical analysis
 * 2. Grammar validation and semantic analysis
 * 3. LALR parsing table construction
 * 4. Java code generation
 */
public class ParserGenerator {
    
    private final ErrorReporter errorReporter;
    private boolean verbose;
    private boolean debug;
    private boolean showWarnings;
    private boolean generateComments;
    private String outputDirectory;
    private String packageName;
    private String grammarName;
    
    /**
     * Creates a new ParserGenerator with default settings.
     */
    public ParserGenerator() {
        this.errorReporter = new ErrorReporter();
        this.verbose = false;
        this.debug = false;
        this.showWarnings = true;
        this.generateComments = true;
        this.outputDirectory = "generated";
        this.packageName = "com.generated.parser";
        this.grammarName = "GeneratedGrammar";
    }
    
    /**
     * Main entry point for the parser generator application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        int exitCode = runMain(args);
        System.exit(exitCode);
    }
    
    /**
     * Main logic without System.exit() calls for testing.
     * 
     * @param args command line arguments
     * @return exit code (0 for success, non-zero for failure)
     */
    public static int runMain(String[] args) {
        ParserGenerator generator = new ParserGenerator();
        
        try {
            CLIOptions options = parseCommandLineArguments(args);
            generator.configure(options);
            
            if (options.showHelp) {
                generator.showHelp();
                return 0;
            }
            
            if (options.inputFile == null) {
                System.err.println("Error: No input grammar file specified.");
                generator.showUsage();
                return 1;
            }
            
            // Generate parser from grammar file
            boolean success = generator.generateParser(options.inputFile);
            
            if (success) {
                System.out.println("Parser generation completed successfully.");
                return 0;
            } else {
                System.err.println("Parser generation failed. See error messages above.");
                return 1;
            }
            
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            if (generator.debug || generator.verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }
    
    /**
     * Configures the parser generator with the given options.
     * 
     * @param options the CLI options to apply
     */
    public void configure(CLIOptions options) {
        if (options.verbose) {
            this.verbose = true;
        }
        
        if (options.debug) {
            this.debug = true;
            this.verbose = true; // Debug implies verbose
        }
        
        if (options.noWarnings) {
            this.showWarnings = false;
        }
        
        if (options.noComments) {
            this.generateComments = false;
        }
        
        if (options.outputDirectory != null) {
            this.outputDirectory = options.outputDirectory;
        }
        
        if (options.packageName != null) {
            this.packageName = options.packageName;
        }
        
        if (options.grammarName != null) {
            this.grammarName = options.grammarName;
        }
    }
    
    /**
     * Sets the verbose mode.
     * 
     * @param verbose true to enable verbose output
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    /**
     * Sets the output directory.
     * 
     * @param outputDirectory the output directory path
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    /**
     * Sets the package name for generated classes.
     * 
     * @param packageName the package name
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    /**
     * Sets the grammar name for generated classes.
     * 
     * @param grammarName the grammar name
     */
    public void setGrammarName(String grammarName) {
        this.grammarName = grammarName;
    }
    
    /**
     * Sets the debug mode.
     * 
     * @param debug true to enable debug output
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
        if (debug) {
            this.verbose = true; // Debug implies verbose
        }
    }
    
    /**
     * Sets whether to show warnings.
     * 
     * @param showWarnings true to show warnings
     */
    public void setShowWarnings(boolean showWarnings) {
        this.showWarnings = showWarnings;
    }
    
    /**
     * Sets whether to generate comments in output code.
     * 
     * @param generateComments true to generate comments
     */
    public void setGenerateComments(boolean generateComments) {
        this.generateComments = generateComments;
    }
    
    /**
     * Generates a parser from the specified grammar file.
     * 
     * @param grammarFile path to the grammar file
     * @return true if generation was successful, false otherwise
     */
    public boolean generateParser(String grammarFile) {
        try {
            logInfo("Starting parser generation for: " + grammarFile);
            
            // Step 1: Read and tokenize grammar file
            logInfo("Reading grammar file...");
            logDebug("Grammar file path: " + grammarFile);
            String grammarContent = readGrammarFile(grammarFile);
            logDebug("Grammar content length: " + grammarContent.length() + " characters");
            
            logInfo("Tokenizing grammar...");
            GrammarLexer lexer = new GrammarLexer(grammarContent, grammarFile, errorReporter);
            List<Token> tokens = lexer.tokenize();
            
            if (errorReporter.hasErrors()) {
                logError("Lexical analysis failed with errors:");
                printErrors();
                return false;
            }
            
            logInfo(String.format("Tokenization completed: %d tokens generated", tokens.size()));
            
            // Step 2: Parse grammar
            logInfo("Parsing grammar...");
            GrammarParser parser = new GrammarParser(tokens, errorReporter);
            Grammar grammar = parser.parse();
            
            if (errorReporter.hasErrors()) {
                logError("Grammar parsing failed with errors:");
                printErrors();
                return false;
            }
            
            logInfo(String.format("Grammar parsing completed: %d productions, %d terminals, %d non-terminals",
                    grammar.getProductions().size(), grammar.getTerminals().size(), grammar.getNonTerminals().size()));
            
            if (debug) {
                logDebug("Start symbol: " + (grammar.getStartSymbol() != null ? grammar.getStartSymbol().getName() : "none"));
                logDebug("Terminals: " + grammar.getTerminals().stream().map(t -> t.getName()).reduce((a, b) -> a + ", " + b).orElse("none"));
                logDebug("Non-terminals: " + grammar.getNonTerminals().stream().map(nt -> nt.getName()).reduce((a, b) -> a + ", " + b).orElse("none"));
            }
            
            // Step 3: Analyze grammar
            logInfo("Analyzing grammar...");
            GrammarAnalyzer analyzer = new GrammarAnalyzer(grammar, errorReporter);
            AnalysisResult analysisResult = analyzer.analyze();
            
            if (analysisResult.hasErrors()) {
                logError("Grammar analysis failed with errors:");
                printErrors();
                return false;
            }
            
            if (errorReporter.hasWarnings()) {
                if (showWarnings) {
                    logWarning("Grammar analysis completed with warnings:");
                    printWarnings();
                } else {
                    logInfo("Grammar analysis completed with warnings (use -w to show warnings)");
                }
            } else {
                logInfo("Grammar analysis completed successfully");
            }
            
            // Step 4: Build LALR parsing table
            logInfo("Building LALR parsing table...");
            LALRTableBuilder tableBuilder = new LALRTableBuilder(grammar, analysisResult, errorReporter);
            ParsingTable parsingTable = tableBuilder.buildTable();
            
            if (errorReporter.hasErrors()) {
                logError("LALR table construction failed with errors:");
                printErrors();
                return false;
            }
            
            logInfo(String.format("LALR table construction completed: %d states", parsingTable.getStateCount()));
            logDebug("Parsing table construction details available in verbose mode");
            
            // Step 5: Generate Java code
            logInfo("Generating Java parser code...");
            JavaCodeGenerator codeGenerator = new JavaCodeGenerator(packageName, grammarName);
            GeneratedCode generatedCode = codeGenerator.generate(grammar, parsingTable);
            
            if (errorReporter.hasErrors()) {
                logError("Code generation failed with errors:");
                printErrors();
                return false;
            }
            
            logInfo(String.format("Code generation completed: %d files generated", generatedCode.getFileCount()));
            
            if (debug) {
                logDebug("Generated files:");
                for (String fileName : generatedCode.getFileNames()) {
                    logDebug("  - " + fileName);
                }
            }
            
            // Step 6: Write generated files
            logInfo("Writing generated files...");
            writeGeneratedFiles(generatedCode);
            
            if (errorReporter.hasErrors()) {
                logError("File writing failed with errors:");
                printErrors();
                return false;
            }
            
            logInfo("Parser generation completed successfully");
            logInfo("Generated files written to: " + new File(outputDirectory).getAbsolutePath());
            
            return true;
            
        } catch (Exception e) {
            logError("Unexpected error during parser generation: " + e.getMessage());
            if (debug || verbose) {
                e.printStackTrace();
            }
            return false;
        }
    }
    
    /**
     * Reads the content of a grammar file.
     * 
     * @param grammarFile path to the grammar file
     * @return the file content as a string
     * @throws IOException if file reading fails
     */
    private String readGrammarFile(String grammarFile) throws IOException {
        Path path = Paths.get(grammarFile);
        
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Grammar file not found: " + grammarFile);
        }
        
        if (!Files.isReadable(path)) {
            throw new IOException("Grammar file is not readable: " + grammarFile);
        }
        
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IOException("Failed to read grammar file: " + grammarFile + " - " + e.getMessage(), e);
        }
    }
    
    /**
     * Writes the generated code files to the output directory.
     * 
     * @param generatedCode the generated code to write
     * @throws IOException if file writing fails
     */
    private void writeGeneratedFiles(GeneratedCode generatedCode) throws IOException {
        Path outputPath = Paths.get(outputDirectory);
        
        // Create output directory if it doesn't exist
        if (!Files.exists(outputPath)) {
            logInfo("Creating output directory: " + outputPath.toAbsolutePath());
            Files.createDirectories(outputPath);
        }
        
        // Create package directory structure
        String[] packageParts = packageName.split("\\.");
        Path packagePath = outputPath;
        for (String part : packageParts) {
            packagePath = packagePath.resolve(part);
        }
        
        if (!Files.exists(packagePath)) {
            logInfo("Creating package directory: " + packagePath.toAbsolutePath());
            Files.createDirectories(packagePath);
        }
        
        // Write each generated file
        for (String fileName : generatedCode.getFileNames()) {
            String content = generatedCode.getFileContent(fileName);
            Path filePath = packagePath.resolve(fileName);
            
            logInfo("Writing file: " + filePath.toAbsolutePath());
            
            try {
                Files.writeString(filePath, content);
            } catch (IOException e) {
                String message = "Failed to write file: " + filePath + " - " + e.getMessage();
                errorReporter.reportError(ErrorReporter.Severity.ERROR,
                        ErrorReporter.ErrorType.IO_ERROR,
                        message,
                        null,
                        "File: " + filePath,
                        "Check file permissions and disk space");
                throw new IOException(message, e);
            }
        }
        
        logInfo(String.format("Successfully wrote %d files to %s", 
                generatedCode.getFileCount(), packagePath.toAbsolutePath()));
    }
    
    /**
     * Parses command line arguments into a CLIOptions object.
     * 
     * @param args the command line arguments
     * @return parsed CLI options
     */
    private static CLIOptions parseCommandLineArguments(String[] args) {

        CLIOptions options = new CLIOptions();

        options.showHelp = args.length == 0;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "-h":
                case "--help":
                    options.showHelp = true;
                    break;
                    
                case "-v":
                case "--verbose":
                    options.verbose = true;
                    break;
                    
                case "-d":
                case "--debug":
                    options.debug = true;
                    break;
                    
                case "-w":
                case "--warnings":
                    options.noWarnings = false;
                    break;
                    
                case "--no-warnings":
                    options.noWarnings = true;
                    break;
                    
                case "--no-comments":
                    options.noComments = true;
                    break;
                    
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        options.outputDirectory = args[++i];
                    } else {
                        throw new IllegalArgumentException("Option " + arg + " requires a value");
                    }
                    break;
                    
                case "-p":
                case "--package":
                    if (i + 1 < args.length) {
                        options.packageName = args[++i];
                    } else {
                        throw new IllegalArgumentException("Option " + arg + " requires a value");
                    }
                    break;
                    
                case "-n":
                case "--name":
                    if (i + 1 < args.length) {
                        options.grammarName = args[++i];
                    } else {
                        throw new IllegalArgumentException("Option " + arg + " requires a value");
                    }
                    break;
                    
                default:
                    if (arg.startsWith("-")) {
                        throw new IllegalArgumentException("Unknown option: " + arg);
                    } else {
                        // Assume it's the input file
                        if (options.inputFile == null) {
                            options.inputFile = arg;
                        } else {
                            throw new IllegalArgumentException("Multiple input files specified: " + options.inputFile + ", " + arg);
                        }
                    }
                    break;
            }
        }

        return options;
    }
    
    /**
     * Shows the help message with usage information and examples.
     */
    private void showHelp() {
        System.out.println("Yacc-like Parser Generator");
        System.out.println("==========================");
        System.out.println();
        System.out.println("A tool for generating LALR(1) parsers from context-free grammar specifications.");
        System.out.println();
        showUsage();
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -h, --help              Show this help message and exit");
        System.out.println("  -v, --verbose           Enable verbose output");
        System.out.println("  -d, --debug             Enable debug output (implies verbose)");
        System.out.println("  -w, --warnings          Show warnings (default)");
        System.out.println("      --no-warnings       Suppress warning messages");
        System.out.println("      --no-comments        Don't generate comments in output code");
        System.out.println("  -o, --output DIR        Output directory for generated files (default: generated)");
        System.out.println("  -p, --package PACKAGE   Package name for generated classes (default: com.generated.parser)");
        System.out.println("  -n, --name NAME         Grammar name for generated classes (default: GeneratedGrammar)");
        System.out.println();
        System.out.println("Grammar File Format:");
        System.out.println("  Terminal definitions:   TerminalName = {Pattern}+");
        System.out.println("  Production rules:       <NonTerminal> ::= <Rule1> | <Rule2> | ...");
        System.out.println("  Start symbol:           Start = <NonTerminal>");
        System.out.println("  Comments:               -- comment text");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  " + getExecutableName() + " arithmetic.grammar");
        System.out.println("  " + getExecutableName() + " -v -o src/main/java -p com.myapp.parser arithmetic.grammar");
        System.out.println("  " + getExecutableName() + " --package com.example --name Calculator calculator.grammar");
        System.out.println("  " + getExecutableName() + " --debug --no-warnings -o build/generated calculator.grammar");
        System.out.println("  " + getExecutableName() + " -d --no-comments -p com.calc -n CalcParser calc.grammar");
        System.out.println();
        System.out.println("Example Grammar (arithmetic.grammar):");
        System.out.println("  -- Arithmetic expression grammar");
        System.out.println("  NUMBER = {[0-9]+}");
        System.out.println("  PLUS = {\\+}");
        System.out.println("  MULTIPLY = {\\*}");
        System.out.println("  LPAREN = {\\(}");
        System.out.println("  RPAREN = {\\)}");
        System.out.println();
        System.out.println("  Start = <Expression>");
        System.out.println();
        System.out.println("  <Expression> ::= <Term> PLUS <Expression> | <Term>");
        System.out.println("  <Term> ::= <Factor> MULTIPLY <Term> | <Factor>");
        System.out.println("  <Factor> ::= NUMBER | LPAREN <Expression> RPAREN");
        System.out.println();
        System.out.println("Grammar Format Notes:");
        System.out.println("  - Terminal patterns are enclosed in braces: {pattern}");
        System.out.println("  - Non-terminals are enclosed in angle brackets: <NonTerminal>");
        System.out.println("  - Use '::=' for production rules, '=' for terminal definitions");
        System.out.println("  - Comments start with '--' and continue to end of line");
        System.out.println("  - Avoid left recursion for LALR(1) compatibility");
    }
    
    /**
     * Shows the basic usage information.
     */
    private void showUsage() {
        System.out.println("Usage: " + getExecutableName() + " [OPTIONS] <grammar-file>");
    }
    
    /**
     * Gets the executable name for help messages.
     * 
     * @return the executable name
     */
    private String getExecutableName() {
        return "parser-generator";
    }
    
    /**
     * Logs an informational message.
     * 
     * @param message the message to log
     */
    private void logInfo(String message) {
        if (verbose) {
            System.out.println("[INFO] " + message);
        }
    }
    
    /**
     * Logs a warning message.
     * 
     * @param message the message to log
     */
    private void logWarning(String message) {
        System.out.println("[WARNING] " + message);
    }
    
    /**
     * Logs an error message.
     * 
     * @param message the message to log
     */
    private void logError(String message) {
        System.err.println("[ERROR] " + message);
    }
    
    /**
     * Logs a debug message.
     * 
     * @param message the message to log
     */
    private void logDebug(String message) {
        if (debug) {
            System.out.println("[DEBUG] " + message);
        }
    }
    
    /**
     * Prints all errors from the error reporter.
     */
    private void printErrors() {
        for (ErrorReport error : errorReporter.getErrors(ErrorReporter.Severity.ERROR)) {
            System.err.println("  " + error.toString());
        }
    }
    
    /**
     * Prints all warnings from the error reporter.
     */
    private void printWarnings() {
        for (ErrorReport warning : errorReporter.getErrors(ErrorReporter.Severity.WARNING)) {
            System.out.println("  " + warning.toString());
        }
    }
    
    /**
     * Data class for holding CLI options.
     */
    private static class CLIOptions {
        boolean showHelp = false;
        boolean verbose = false;
        boolean debug = false;
        boolean noWarnings = false;
        boolean noComments = false;
        String inputFile = null;
        String outputDirectory = null;
        String packageName = null;
        String grammarName = null;
    }
}