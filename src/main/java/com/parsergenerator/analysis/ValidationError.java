package com.parsergenerator.analysis;

/**
 * Represents a validation error with severity level and detailed information.
 */
public class ValidationError {
    
    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }
    
    public enum Category {
        STRUCTURAL,
        SEMANTIC,
        LEFT_RECURSION,
        REACHABILITY,
        SYMBOL_DEFINITION,
        CIRCULAR_DEPENDENCY
    }
    
    private final Severity severity;
    private final Category category;
    private final String message;
    private final String suggestion;
    private final String context;
    
    /**
     * Constructs a ValidationError with the specified properties.
     * 
     * @param severity the severity level
     * @param category the error category
     * @param message the error message
     * @param suggestion optional suggestion for fixing the error
     * @param context optional context information
     */
    public ValidationError(Severity severity, Category category, String message, 
                          String suggestion, String context) {
        this.severity = severity;
        this.category = category;
        this.message = message;
        this.suggestion = suggestion;
        this.context = context;
    }
    
    /**
     * Constructs a ValidationError with message only.
     * 
     * @param severity the severity level
     * @param category the error category
     * @param message the error message
     */
    public ValidationError(Severity severity, Category category, String message) {
        this(severity, category, message, null, null);
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getSuggestion() {
        return suggestion;
    }
    
    public String getContext() {
        return context;
    }
    
    public boolean hasSuggestion() {
        return suggestion != null && !suggestion.trim().isEmpty();
    }
    
    public boolean hasContext() {
        return context != null && !context.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(severity).append(":").append(category).append("] ");
        sb.append(message);
        
        if (hasContext()) {
            sb.append(" (Context: ").append(context).append(")");
        }
        
        if (hasSuggestion()) {
            sb.append(" Suggestion: ").append(suggestion);
        }
        
        return sb.toString();
    }
}