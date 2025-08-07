package com.parsergenerator.error;

import com.parsergenerator.lexer.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ErrorReporter class.
 */
class ErrorReporterTest {
    
    private ErrorReporter errorReporter;
    private Position testPosition;
    
    @BeforeEach
    void setUp() {
        errorReporter = new ErrorReporter();
        testPosition = new Position(10, 5, "test.grammar");
    }
    
    @Test
    @DisplayName("Should initialize with no errors")
    void shouldInitializeWithNoErrors() {
        assertFalse(errorReporter.hasErrors());
        assertFalse(errorReporter.hasWarnings());
        assertEquals(0, errorReporter.getErrorCount());
        assertEquals(0, errorReporter.getWarningCount());
        assertTrue(errorReporter.getErrors().isEmpty());
    }
    
    @Test
    @DisplayName("Should report basic error correctly")
    void shouldReportBasicError() {
        String message = "Test error message";
        
        errorReporter.reportError(ErrorReporter.Severity.ERROR, 
                                 ErrorReporter.ErrorType.SYNTAX, 
                                 message, testPosition);
        
        assertTrue(errorReporter.hasErrors());
        assertEquals(1, errorReporter.getErrorCount());
        assertEquals(1, errorReporter.getErrorCount(ErrorReporter.ErrorType.SYNTAX));
        
        List<ErrorReport> errors = errorReporter.getErrors();
        assertEquals(1, errors.size());
        
        ErrorReport error = errors.get(0);
        assertEquals(ErrorReporter.Severity.ERROR, error.getSeverity());
        assertEquals(ErrorReporter.ErrorType.SYNTAX, error.getType());
        assertEquals(message, error.getMessage());
        assertEquals(testPosition, error.getPosition());
    }
    
    @Test
    @DisplayName("Should report error with full details")
    void shouldReportErrorWithFullDetails() {
        String message = "Detailed error message";
        String context = "parsing production rule";
        String suggestion = "check syntax";
        
        errorReporter.reportError(ErrorReporter.Severity.ERROR,
                                 ErrorReporter.ErrorType.SEMANTIC,
                                 message, testPosition, context, suggestion);
        
        List<ErrorReport> errors = errorReporter.getErrors();
        assertEquals(1, errors.size());
        
        ErrorReport error = errors.get(0);
        assertEquals(message, error.getMessage());
        assertEquals(context, error.getContext());
        assertEquals(suggestion, error.getSuggestion());
        assertTrue(error.hasContext());
        assertTrue(error.hasSuggestion());
    }
    
    @Test
    @DisplayName("Should report warnings correctly")
    void shouldReportWarnings() {
        String message = "Test warning message";
        
        errorReporter.reportWarning(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, 
                                   message, testPosition, "Consider refactoring");
        
        assertTrue(errorReporter.hasWarnings());
        assertFalse(errorReporter.hasErrors());
        assertEquals(0, errorReporter.getErrorCount());
        assertEquals(1, errorReporter.getWarningCount());
        
        List<ErrorReport> warnings = errorReporter.getErrors(ErrorReporter.Severity.WARNING);
        assertEquals(1, warnings.size());
        
        ErrorReport warning = warnings.get(0);
        assertEquals(ErrorReporter.Severity.WARNING, warning.getSeverity());
        assertTrue(warning.isWarning());
        assertFalse(warning.isError());
    }
    
    @Test
    @DisplayName("Should report info messages correctly")
    void shouldReportInfoMessages() {
        String message = "Informational message";
        
        errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, message);
        
        assertFalse(errorReporter.hasErrors());
        assertFalse(errorReporter.hasWarnings());
        
        List<ErrorReport> infos = errorReporter.getErrors(ErrorReporter.Severity.INFO);
        assertEquals(1, infos.size());
        
        ErrorReport info = infos.get(0);
        assertEquals(ErrorReporter.Severity.INFO, info.getSeverity());
        assertTrue(info.isInfo());
        assertFalse(info.isError());
        assertFalse(info.isWarning());
    }
    
    @Test
    @DisplayName("Should use convenience methods for specific error types")
    void shouldUseConvenienceMethodsForSpecificErrorTypes() {
        // Test lexical error
        errorReporter.reportLexicalError("Invalid character", testPosition);
        
        // Test syntax error
        errorReporter.reportSyntaxError("Missing semicolon", testPosition, "Add semicolon");
        
        // Test semantic error
        errorReporter.reportSemanticError("Undefined symbol", "in production rule", "Define symbol first");
        
        // Test grammar validation error
        errorReporter.reportGrammarValidationError("Left recursion detected", "in rule A", "Eliminate left recursion");
        
        // Test LALR conflict
        errorReporter.reportLALRConflict("Shift/reduce conflict", "state 5", "Add precedence rules");
        
        // Test code generation error
        errorReporter.reportCodeGenerationError("Template error", "generating parser class");
        
        assertEquals(6, errorReporter.getErrorCount());
        assertEquals(1, errorReporter.getErrorCount(ErrorReporter.ErrorType.LEXICAL));
        assertEquals(1, errorReporter.getErrorCount(ErrorReporter.ErrorType.SYNTAX));
        assertEquals(1, errorReporter.getErrorCount(ErrorReporter.ErrorType.SEMANTIC));
        assertEquals(1, errorReporter.getErrorCount(ErrorReporter.ErrorType.GRAMMAR_VALIDATION));
        assertEquals(1, errorReporter.getErrorCount(ErrorReporter.ErrorType.LALR_CONFLICT));
        assertEquals(1, errorReporter.getErrorCount(ErrorReporter.ErrorType.CODE_GENERATION));
    }
    
    @Test
    @DisplayName("Should filter errors by type")
    void shouldFilterErrorsByType() {
        errorReporter.reportLexicalError("Lexical error 1", testPosition);
        errorReporter.reportLexicalError("Lexical error 2", testPosition);
        errorReporter.reportSyntaxError("Syntax error", testPosition, null);
        
        List<ErrorReport> lexicalErrors = errorReporter.getErrors(ErrorReporter.ErrorType.LEXICAL);
        List<ErrorReport> syntaxErrors = errorReporter.getErrors(ErrorReporter.ErrorType.SYNTAX);
        
        assertEquals(2, lexicalErrors.size());
        assertEquals(1, syntaxErrors.size());
        
        for (ErrorReport error : lexicalErrors) {
            assertEquals(ErrorReporter.ErrorType.LEXICAL, error.getType());
        }
        
        for (ErrorReport error : syntaxErrors) {
            assertEquals(ErrorReporter.ErrorType.SYNTAX, error.getType());
        }
    }
    
    @Test
    @DisplayName("Should filter errors by severity")
    void shouldFilterErrorsBySeverity() {
        errorReporter.reportError(ErrorReporter.Severity.ERROR, ErrorReporter.ErrorType.SYNTAX, "Error", testPosition);
        errorReporter.reportWarning(ErrorReporter.ErrorType.SEMANTIC, "Warning", testPosition, null);
        errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, "Info");
        
        List<ErrorReport> errors = errorReporter.getErrors(ErrorReporter.Severity.ERROR);
        List<ErrorReport> warnings = errorReporter.getErrors(ErrorReporter.Severity.WARNING);
        List<ErrorReport> infos = errorReporter.getErrors(ErrorReporter.Severity.INFO);
        
        assertEquals(1, errors.size());
        assertEquals(1, warnings.size());
        assertEquals(1, infos.size());
        
        assertEquals(ErrorReporter.Severity.ERROR, errors.get(0).getSeverity());
        assertEquals(ErrorReporter.Severity.WARNING, warnings.get(0).getSeverity());
        assertEquals(ErrorReporter.Severity.INFO, infos.get(0).getSeverity());
    }
    
    @Test
    @DisplayName("Should clear all errors")
    void shouldClearAllErrors() {
        errorReporter.reportError(ErrorReporter.Severity.ERROR, ErrorReporter.ErrorType.SYNTAX, "Error", testPosition);
        errorReporter.reportWarning(ErrorReporter.ErrorType.SEMANTIC, "Warning", testPosition, null);
        
        assertTrue(errorReporter.hasErrors());
        assertTrue(errorReporter.hasWarnings());
        assertEquals(2, errorReporter.getErrors().size());
        
        errorReporter.clear();
        
        assertFalse(errorReporter.hasErrors());
        assertFalse(errorReporter.hasWarnings());
        assertEquals(0, errorReporter.getErrorCount());
        assertEquals(0, errorReporter.getWarningCount());
        assertTrue(errorReporter.getErrors().isEmpty());
        
        // Verify all error type counts are reset
        for (ErrorReporter.ErrorType type : ErrorReporter.ErrorType.values()) {
            assertEquals(0, errorReporter.getErrorCount(type));
        }
    }
    
    @Test
    @DisplayName("Should generate summary correctly")
    void shouldGenerateSummaryCorrectly() {
        // Test empty summary
        String emptySummary = errorReporter.generateSummary();
        assertEquals("No errors or warnings reported.", emptySummary);
        
        // Add some errors
        errorReporter.reportLexicalError("Lexical error", testPosition);
        errorReporter.reportSyntaxError("Syntax error", testPosition, null);
        errorReporter.reportWarning(ErrorReporter.ErrorType.SEMANTIC, "Warning", testPosition, null);
        
        String summary = errorReporter.generateSummary();
        
        assertTrue(summary.contains("Error Summary:"));
        assertTrue(summary.contains("Total: 2 error(s), 1 warning(s)"));
        assertTrue(summary.contains("Lexical Error: 1"));
        assertTrue(summary.contains("Syntax Error: 1"));
    }
    
    @Test
    @DisplayName("Should generate detailed report correctly")
    void shouldGenerateDetailedReportCorrectly() {
        // Test empty report
        String emptyReport = errorReporter.generateDetailedReport();
        assertEquals("No errors or warnings reported.", emptyReport);
        
        // Add an error with full details
        errorReporter.reportError(ErrorReporter.Severity.ERROR,
                                 ErrorReporter.ErrorType.SEMANTIC,
                                 "Undefined symbol 'x'",
                                 testPosition,
                                 "in production rule A ::= x y",
                                 "Define symbol 'x' as terminal or non-terminal");
        
        String report = errorReporter.generateDetailedReport();
        
        assertTrue(report.contains("Detailed Error Report:"));
        assertTrue(report.contains("1. [Error:Semantic Error]"));
        assertTrue(report.contains("Message: Undefined symbol 'x'"));
        assertTrue(report.contains("Context: in production rule A ::= x y"));
        assertTrue(report.contains("Suggestion: Define symbol 'x' as terminal or non-terminal"));
        assertTrue(report.contains("Error Summary:"));
    }
    
    @Test
    @DisplayName("Should handle multiple errors of same type")
    void shouldHandleMultipleErrorsOfSameType() {
        errorReporter.reportLexicalError("Error 1", testPosition);
        errorReporter.reportLexicalError("Error 2", testPosition);
        errorReporter.reportLexicalError("Error 3", testPosition);
        
        assertEquals(3, errorReporter.getErrorCount());
        assertEquals(3, errorReporter.getErrorCount(ErrorReporter.ErrorType.LEXICAL));
        assertEquals(0, errorReporter.getErrorCount(ErrorReporter.ErrorType.SYNTAX));
        
        List<ErrorReport> lexicalErrors = errorReporter.getErrors(ErrorReporter.ErrorType.LEXICAL);
        assertEquals(3, lexicalErrors.size());
    }
    
    @Test
    @DisplayName("Should maintain error order")
    void shouldMaintainErrorOrder() {
        errorReporter.reportLexicalError("First error", testPosition);
        errorReporter.reportSyntaxError("Second error", testPosition, null);
        errorReporter.reportSemanticError("Third error", null, null);
        
        List<ErrorReport> errors = errorReporter.getErrors();
        assertEquals(3, errors.size());
        
        assertEquals("First error", errors.get(0).getMessage());
        assertEquals("Second error", errors.get(1).getMessage());
        assertEquals("Third error", errors.get(2).getMessage());
        
        assertEquals(ErrorReporter.ErrorType.LEXICAL, errors.get(0).getType());
        assertEquals(ErrorReporter.ErrorType.SYNTAX, errors.get(1).getType());
        assertEquals(ErrorReporter.ErrorType.SEMANTIC, errors.get(2).getType());
    }
    
    @Test
    @DisplayName("Should handle errors without position")
    void shouldHandleErrorsWithoutPosition() {
        errorReporter.reportError(ErrorReporter.Severity.ERROR,
                                 ErrorReporter.ErrorType.INTERNAL,
                                 "Internal error");
        
        List<ErrorReport> errors = errorReporter.getErrors();
        assertEquals(1, errors.size());
        
        ErrorReport error = errors.get(0);
        assertNull(error.getPosition());
        assertFalse(error.hasPosition());
    }
    
    @Test
    @DisplayName("Should handle errors without context or suggestion")
    void shouldHandleErrorsWithoutContextOrSuggestion() {
        errorReporter.reportError(ErrorReporter.Severity.ERROR,
                                 ErrorReporter.ErrorType.IO_ERROR,
                                 "File not found",
                                 testPosition,
                                 null,
                                 null);
        
        List<ErrorReport> errors = errorReporter.getErrors();
        assertEquals(1, errors.size());
        
        ErrorReport error = errors.get(0);
        assertNull(error.getContext());
        assertNull(error.getSuggestion());
        assertFalse(error.hasContext());
        assertFalse(error.hasSuggestion());
    }
    
    @Test
    @DisplayName("Should validate severity priorities")
    void shouldValidateSeverityPriorities() {
        assertTrue(ErrorReporter.Severity.ERROR.getPriority() > ErrorReporter.Severity.WARNING.getPriority());
        assertTrue(ErrorReporter.Severity.WARNING.getPriority() > ErrorReporter.Severity.INFO.getPriority());
        
        assertEquals("Error", ErrorReporter.Severity.ERROR.getDisplayName());
        assertEquals("Warning", ErrorReporter.Severity.WARNING.getDisplayName());
        assertEquals("Info", ErrorReporter.Severity.INFO.getDisplayName());
    }
    
    @Test
    @DisplayName("Should validate error type display names")
    void shouldValidateErrorTypeDisplayNames() {
        assertEquals("Lexical Error", ErrorReporter.ErrorType.LEXICAL.getDisplayName());
        assertEquals("Syntax Error", ErrorReporter.ErrorType.SYNTAX.getDisplayName());
        assertEquals("Semantic Error", ErrorReporter.ErrorType.SEMANTIC.getDisplayName());
        assertEquals("Grammar Validation Error", ErrorReporter.ErrorType.GRAMMAR_VALIDATION.getDisplayName());
        assertEquals("LALR Conflict", ErrorReporter.ErrorType.LALR_CONFLICT.getDisplayName());
        assertEquals("Code Generation Error", ErrorReporter.ErrorType.CODE_GENERATION.getDisplayName());
        assertEquals("I/O Error", ErrorReporter.ErrorType.IO_ERROR.getDisplayName());
        assertEquals("Internal Error", ErrorReporter.ErrorType.INTERNAL.getDisplayName());
    }}
