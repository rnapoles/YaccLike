package com.parsergenerator.error;

import com.parsergenerator.lexer.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ErrorReport class.
 */
class ErrorReportTest {
    
    private Position testPosition;
    private ErrorReport errorReport;
    private ErrorReport warningReport;
    private ErrorReport infoReport;
    
    @BeforeEach
    void setUp() {
        testPosition = new Position(10, 5, "test.grammar");
        
        errorReport = new ErrorReport(
            ErrorReporter.Severity.ERROR,
            ErrorReporter.ErrorType.SYNTAX,
            "Test error message",
            testPosition,
            "parsing context",
            "fix suggestion"
        );
        
        warningReport = new ErrorReport(
            ErrorReporter.Severity.WARNING,
            ErrorReporter.ErrorType.SEMANTIC,
            "Test warning message",
            null,
            null,
            null
        );
        
        infoReport = new ErrorReport(
            ErrorReporter.Severity.INFO,
            ErrorReporter.ErrorType.GRAMMAR_VALIDATION,
            "Test info message",
            testPosition,
            "info context",
            null
        );
    }
    
    @Test
    @DisplayName("Should create error report with all details")
    void shouldCreateErrorReportWithAllDetails() {
        assertEquals(ErrorReporter.Severity.ERROR, errorReport.getSeverity());
        assertEquals(ErrorReporter.ErrorType.SYNTAX, errorReport.getType());
        assertEquals("Test error message", errorReport.getMessage());
        assertEquals(testPosition, errorReport.getPosition());
        assertEquals("parsing context", errorReport.getContext());
        assertEquals("fix suggestion", errorReport.getSuggestion());
        
        assertTrue(errorReport.hasPosition());
        assertTrue(errorReport.hasContext());
        assertTrue(errorReport.hasSuggestion());
        assertTrue(errorReport.isError());
        assertFalse(errorReport.isWarning());
        assertFalse(errorReport.isInfo());
    }
    
    @Test
    @DisplayName("Should handle optional fields correctly")
    void shouldHandleOptionalFieldsCorrectly() {
        assertNull(warningReport.getPosition());
        assertNull(warningReport.getContext());
        assertNull(warningReport.getSuggestion());
        
        assertFalse(warningReport.hasPosition());
        assertFalse(warningReport.hasContext());
        assertFalse(warningReport.hasSuggestion());
        assertFalse(warningReport.isError());
        assertTrue(warningReport.isWarning());
        assertFalse(warningReport.isInfo());
    }
    
    @Test
    @DisplayName("Should identify info reports correctly")
    void shouldIdentifyInfoReportsCorrectly() {
        assertFalse(infoReport.isError());
        assertFalse(infoReport.isWarning());
        assertTrue(infoReport.isInfo());
        
        assertTrue(infoReport.hasPosition());
        assertTrue(infoReport.hasContext());
        assertFalse(infoReport.hasSuggestion());
    }
    
    @Test
    @DisplayName("Should handle empty context and suggestion strings")
    void shouldHandleEmptyContextAndSuggestionStrings() {
        ErrorReport emptyReport = new ErrorReport(
            ErrorReporter.Severity.ERROR,
            ErrorReporter.ErrorType.LEXICAL,
            "Test message",
            testPosition,
            "   ",  // whitespace only
            ""      // empty string
        );
        
        assertFalse(emptyReport.hasContext());
        assertFalse(emptyReport.hasSuggestion());
    }
    
    @Test
    @DisplayName("Should generate summary correctly")
    void shouldGenerateSummaryCorrectly() {
        String summary = errorReport.toSummary();
        
        assertTrue(summary.contains("[Error:Syntax Error]"));
        assertTrue(summary.contains("at " + testPosition.toString()));
        assertTrue(summary.contains("Test error message"));
    }
    
    @Test
    @DisplayName("Should generate summary without position")
    void shouldGenerateSummaryWithoutPosition() {
        String summary = warningReport.toSummary();
        
        assertTrue(summary.contains("[Warning:Semantic Error]"));
        assertFalse(summary.contains("at "));
        assertTrue(summary.contains("Test warning message"));
    }
    
    @Test
    @DisplayName("Should generate detailed string representation")
    void shouldGenerateDetailedStringRepresentation() {
        String detailed = errorReport.toString();
        
        assertTrue(detailed.contains("[Error:Syntax Error]"));
        assertTrue(detailed.contains("at " + testPosition.toString()));
        assertTrue(detailed.contains("Message: Test error message"));
        assertTrue(detailed.contains("Context: parsing context"));
        assertTrue(detailed.contains("Suggestion: fix suggestion"));
    }
    
    @Test
    @DisplayName("Should generate detailed string without optional fields")
    void shouldGenerateDetailedStringWithoutOptionalFields() {
        String detailed = warningReport.toString();
        
        assertTrue(detailed.contains("[Warning:Semantic Error]"));
        assertTrue(detailed.contains("Message: Test warning message"));
        assertFalse(detailed.contains("Context:"));
        assertFalse(detailed.contains("Suggestion:"));
        assertFalse(detailed.contains("at "));
    }
    
    @Test
    @DisplayName("Should have timestamp")
    void shouldHaveTimestamp() {
        long beforeCreation = System.currentTimeMillis();
        ErrorReport report = new ErrorReport(
            ErrorReporter.Severity.ERROR,
            ErrorReporter.ErrorType.INTERNAL,
            "Test",
            null,
            null,
            null
        );
        long afterCreation = System.currentTimeMillis();
        
        assertTrue(report.getTimestamp() >= beforeCreation);
        assertTrue(report.getTimestamp() <= afterCreation);
    }
    
    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        ErrorReport identical = new ErrorReport(
            ErrorReporter.Severity.ERROR,
            ErrorReporter.ErrorType.SYNTAX,
            "Test error message",
            testPosition,
            "parsing context",
            "fix suggestion"
        );
        
        ErrorReport different = new ErrorReport(
            ErrorReporter.Severity.WARNING,
            ErrorReporter.ErrorType.SYNTAX,
            "Test error message",
            testPosition,
            "parsing context",
            "fix suggestion"
        );
        
        assertEquals(errorReport, identical);
        assertNotEquals(errorReport, different);
        assertNotEquals(errorReport, null);
        assertNotEquals(errorReport, "not an ErrorReport");
        assertEquals(errorReport, errorReport); // reflexive
    }
    
    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        ErrorReport identical = new ErrorReport(
            ErrorReporter.Severity.ERROR,
            ErrorReporter.ErrorType.SYNTAX,
            "Test error message",
            testPosition,
            "parsing context",
            "fix suggestion"
        );
        
        assertEquals(errorReport.hashCode(), identical.hashCode());
        
        // Different objects should likely have different hash codes
        assertNotEquals(errorReport.hashCode(), warningReport.hashCode());
    }
    
    @Test
    @DisplayName("Should handle null position in equals")
    void shouldHandleNullPositionInEquals() {
        ErrorReport withoutPosition1 = new ErrorReport(
            ErrorReporter.Severity.ERROR,
            ErrorReporter.ErrorType.SYNTAX,
            "Test message",
            null,
            null,
            null
        );
        
        ErrorReport withoutPosition2 = new ErrorReport(
            ErrorReporter.Severity.ERROR,
            ErrorReporter.ErrorType.SYNTAX,
            "Test message",
            null,
            null,
            null
        );
        
        assertEquals(withoutPosition1, withoutPosition2);
    }
    
    @Test
    @DisplayName("Should handle null context and suggestion in equals")
    void shouldHandleNullContextAndSuggestionInEquals() {
        ErrorReport report1 = new ErrorReport(
            ErrorReporter.Severity.ERROR,
            ErrorReporter.ErrorType.SYNTAX,
            "Test message",
            testPosition,
            null,
            null
        );
        
        ErrorReport report2 = new ErrorReport(
            ErrorReporter.Severity.ERROR,
            ErrorReporter.ErrorType.SYNTAX,
            "Test message",
            testPosition,
            null,
            null
        );
        
        assertEquals(report1, report2);
    }
}