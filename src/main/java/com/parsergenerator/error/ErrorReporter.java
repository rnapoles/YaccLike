package com.parsergenerator.error;

import com.parsergenerator.lexer.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Centralized error reporting system for the parser generator.
 * Provides consistent error messaging, severity levels, and context-aware reporting.
 */
public class ErrorReporter {
    
    /**
     * Error severity levels.
     */
    public enum Severity {
        ERROR("Error", 3),
        WARNING("Warning", 2),
        INFO("Info", 1);
        
        private final String displayName;
        private final int priority;
        
        Severity(String displayName, int priority) {
            this.displayName = displayName;
            this.priority = priority;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getPriority() {
            return priority;
        }
    }
    
    /**
     * Error categories for different types of issues.
     */
    public enum ErrorType {
        LEXICAL("Lexical Error"),
        SYNTAX("Syntax Error"),
        SEMANTIC("Semantic Error"),
        GRAMMAR_VALIDATION("Grammar Validation Error"),
        LALR_CONFLICT("LALR Conflict"),
        CODE_GENERATION("Code Generation Error"),
        IO_ERROR("I/O Error"),
        INTERNAL("Internal Error");
        
        private final String displayName;
        
        ErrorType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final List<ErrorReport> errors;
    private final Map<ErrorType, Integer> errorCounts;
    private boolean hasErrors;
    private boolean hasWarnings;
    
    /**
     * Creates a new ErrorReporter instance.
     */
    public ErrorReporter() {
        this.errors = new ArrayList<>();
        this.errorCounts = new HashMap<>();
        this.hasErrors = false;
        this.hasWarnings = false;
        
        // Initialize error counts
        for (ErrorType type : ErrorType.values()) {
            errorCounts.put(type, 0);
        }
    }
    
    /**
     * Reports an error with the specified details.
     * 
     * @param severity the error severity
     * @param type the error type
     * @param message the error message
     * @param position the position where the error occurred (optional)
     * @param context additional context information (optional)
     * @param suggestion suggested fix for the error (optional)
     */
    public void reportError(Severity severity, ErrorType type, String message, 
                           Position position, String context, String suggestion) {
        ErrorReport report = new ErrorReport(severity, type, message, position, context, suggestion);
        errors.add(report);
        
        // Only count actual errors and warnings in errorCounts, not INFO messages
        if (severity == Severity.ERROR || severity == Severity.WARNING) {
            errorCounts.put(type, errorCounts.get(type) + 1);
        }
        
        if (severity == Severity.ERROR) {
            hasErrors = true;
        } else if (severity == Severity.WARNING) {
            hasWarnings = true;
        }
    }
    
    /**
     * Reports an error with message and position only.
     * 
     * @param severity the error severity
     * @param type the error type
     * @param message the error message
     * @param position the position where the error occurred
     */
    public void reportError(Severity severity, ErrorType type, String message, Position position) {
        reportError(severity, type, message, position, null, null);
    }
    
    /**
     * Reports an error with message only.
     * 
     * @param severity the error severity
     * @param type the error type
     * @param message the error message
     */
    public void reportError(Severity severity, ErrorType type, String message) {
        reportError(severity, type, message, null, null, null);
    }
    
    /**
     * Reports a lexical error.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     */
    public void reportLexicalError(String message, Position position) {
        reportError(Severity.ERROR, ErrorType.LEXICAL, message, position);
    }
    
    /**
     * Reports a syntax error.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     * @param suggestion suggested fix for the error
     */
    public void reportSyntaxError(String message, Position position, String suggestion) {
        reportError(Severity.ERROR, ErrorType.SYNTAX, message, position, null, suggestion);
    }
    
    /**
     * Reports a semantic error.
     * 
     * @param message the error message
     * @param context additional context information
     * @param suggestion suggested fix for the error
     */
    public void reportSemanticError(String message, String context, String suggestion) {
        reportError(Severity.ERROR, ErrorType.SEMANTIC, message, null, context, suggestion);
    }
    
    /**
     * Reports a grammar validation error.
     * 
     * @param message the error message
     * @param context additional context information
     * @param suggestion suggested fix for the error
     */
    public void reportGrammarValidationError(String message, String context, String suggestion) {
        reportError(Severity.ERROR, ErrorType.GRAMMAR_VALIDATION, message, null, context, suggestion);
    }
    
    /**
     * Reports an LALR conflict.
     * 
     * @param message the error message
     * @param context additional context information
     * @param suggestion suggested fix for the conflict
     */
    public void reportLALRConflict(String message, String context, String suggestion) {
        reportError(Severity.ERROR, ErrorType.LALR_CONFLICT, message, null, context, suggestion);
    }
    
    /**
     * Reports a code generation error.
     * 
     * @param message the error message
     * @param context additional context information
     */
    public void reportCodeGenerationError(String message, String context) {
        reportError(Severity.ERROR, ErrorType.CODE_GENERATION, message, null, context, null);
    }
    
    /**
     * Reports a warning.
     * 
     * @param type the error type
     * @param message the warning message
     * @param position the position where the warning occurred (optional)
     * @param suggestion suggested improvement (optional)
     */
    public void reportWarning(ErrorType type, String message, Position position, String suggestion) {
        reportError(Severity.WARNING, type, message, position, null, suggestion);
    }
    
    /**
     * Reports an informational message.
     * 
     * @param type the error type
     * @param message the informational message
     */
    public void reportInfo(ErrorType type, String message) {
        reportError(Severity.INFO, type, message, null, null, null);
    }
    
    /**
     * Returns whether any errors have been reported.
     * 
     * @return true if errors have been reported
     */
    public boolean hasErrors() {
        return hasErrors;
    }
    
    /**
     * Returns whether any warnings have been reported.
     * 
     * @return true if warnings have been reported
     */
    public boolean hasWarnings() {
        return hasWarnings;
    }
    
    /**
     * Returns the total number of errors reported.
     * 
     * @return the error count
     */
    public int getErrorCount() {
        return (int) errors.stream().filter(e -> e.getSeverity() == Severity.ERROR).count();
    }
    
    /**
     * Returns the total number of warnings reported.
     * 
     * @return the warning count
     */
    public int getWarningCount() {
        return (int) errors.stream().filter(e -> e.getSeverity() == Severity.WARNING).count();
    }
    
    /**
     * Returns the number of errors of a specific type.
     * 
     * @param type the error type
     * @return the count of errors of that type
     */
    public int getErrorCount(ErrorType type) {
        return errorCounts.get(type);
    }
    
    /**
     * Returns all reported errors.
     * 
     * @return an unmodifiable list of error reports
     */
    public List<ErrorReport> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    /**
     * Returns errors of a specific severity level.
     * 
     * @param severity the severity level
     * @return a list of errors with the specified severity
     */
    public List<ErrorReport> getErrors(Severity severity) {
        return errors.stream()
                    .filter(e -> e.getSeverity() == severity)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Returns errors of a specific type.
     * 
     * @param type the error type
     * @return a list of errors of the specified type
     */
    public List<ErrorReport> getErrors(ErrorType type) {
        return errors.stream()
                    .filter(e -> e.getType() == type)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Clears all reported errors.
     */
    public void clear() {
        errors.clear();
        errorCounts.replaceAll((k, v) -> 0);
        hasErrors = false;
        hasWarnings = false;
    }
    
    /**
     * Generates a formatted summary of all errors.
     * 
     * @return a formatted error summary string
     */
    public String generateSummary() {
        int errorCount = getErrorCount();
        int warningCount = getWarningCount();
        
        if (errorCount == 0 && warningCount == 0) {
            return "No errors or warnings reported.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Error Summary:\n");
        sb.append("=============\n");
        
        sb.append(String.format("Total: %d error(s), %d warning(s)\n\n", errorCount, warningCount));
        
        // Group errors by type (only show types that have actual errors/warnings)
        for (ErrorType type : ErrorType.values()) {
            int count = errorCounts.get(type);
            if (count > 0) {
                sb.append(String.format("%s: %d\n", type.getDisplayName(), count));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Generates a detailed report of all errors.
     * 
     * @return a formatted detailed error report
     */
    public String generateDetailedReport() {
        if (errors.isEmpty()) {
            return "No errors or warnings reported.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Detailed Error Report:\n");
        sb.append("=====================\n\n");
        
        for (int i = 0; i < errors.size(); i++) {
            ErrorReport error = errors.get(i);
            sb.append(String.format("%d. %s\n", i + 1, error.toString()));
            sb.append("\n");
        }
        
        sb.append(generateSummary());
        
        return sb.toString();
    }
}