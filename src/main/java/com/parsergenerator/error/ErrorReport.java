package com.parsergenerator.error;

import com.parsergenerator.lexer.Position;

/**
 * Represents a single error report with all associated information.
 * Immutable class that contains error details, position, context, and suggestions.
 */
public class ErrorReport {
    private final ErrorReporter.Severity severity;
    private final ErrorReporter.ErrorType type;
    private final String message;
    private final Position position;
    private final String context;
    private final String suggestion;
    private final long timestamp;
    
    /**
     * Creates a new ErrorReport with the specified details.
     * 
     * @param severity the error severity
     * @param type the error type
     * @param message the error message
     * @param position the position where the error occurred (optional)
     * @param context additional context information (optional)
     * @param suggestion suggested fix for the error (optional)
     */
    public ErrorReport(ErrorReporter.Severity severity, ErrorReporter.ErrorType type, 
                      String message, Position position, String context, String suggestion) {
        this.severity = severity;
        this.type = type;
        this.message = message;
        this.position = position;
        this.context = context;
        this.suggestion = suggestion;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Returns the error severity.
     * 
     * @return the severity level
     */
    public ErrorReporter.Severity getSeverity() {
        return severity;
    }
    
    /**
     * Returns the error type.
     * 
     * @return the error type
     */
    public ErrorReporter.ErrorType getType() {
        return type;
    }
    
    /**
     * Returns the error message.
     * 
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Returns the position where the error occurred.
     * 
     * @return the error position, or null if not available
     */
    public Position getPosition() {
        return position;
    }
    
    /**
     * Returns additional context information.
     * 
     * @return the context string, or null if not provided
     */
    public String getContext() {
        return context;
    }
    
    /**
     * Returns the suggested fix for the error.
     * 
     * @return the suggestion string, or null if not provided
     */
    public String getSuggestion() {
        return suggestion;
    }
    
    /**
     * Returns the timestamp when the error was reported.
     * 
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Returns whether this error report has position information.
     * 
     * @return true if position is available
     */
    public boolean hasPosition() {
        return position != null;
    }
    
    /**
     * Returns whether this error report has context information.
     * 
     * @return true if context is available
     */
    public boolean hasContext() {
        return context != null && !context.trim().isEmpty();
    }
    
    /**
     * Returns whether this error report has a suggestion.
     * 
     * @return true if suggestion is available
     */
    public boolean hasSuggestion() {
        return suggestion != null && !suggestion.trim().isEmpty();
    }
    
    /**
     * Returns whether this is an error (as opposed to warning or info).
     * 
     * @return true if this is an error
     */
    public boolean isError() {
        return severity == ErrorReporter.Severity.ERROR;
    }
    
    /**
     * Returns whether this is a warning.
     * 
     * @return true if this is a warning
     */
    public boolean isWarning() {
        return severity == ErrorReporter.Severity.WARNING;
    }
    
    /**
     * Returns whether this is an informational message.
     * 
     * @return true if this is informational
     */
    public boolean isInfo() {
        return severity == ErrorReporter.Severity.INFO;
    }
    
    /**
     * Formats the error report as a single-line summary.
     * 
     * @return a formatted summary string
     */
    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        
        // Add severity and type
        sb.append("[").append(severity.getDisplayName()).append(":").append(type.getDisplayName()).append("] ");
        
        // Add position if available
        if (hasPosition()) {
            sb.append("at ").append(position).append(": ");
        }
        
        // Add message
        sb.append(message);
        
        return sb.toString();
    }
    
    /**
     * Formats the error report with full details.
     * 
     * @return a formatted detailed string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Header with severity and type
        sb.append("[").append(severity.getDisplayName()).append(":").append(type.getDisplayName()).append("]");
        
        // Add position if available
        if (hasPosition()) {
            sb.append(" at ").append(position);
        }
        
        sb.append("\n");
        
        // Message
        sb.append("  Message: ").append(message).append("\n");
        
        // Context if available
        if (hasContext()) {
            sb.append("  Context: ").append(context).append("\n");
        }
        
        // Suggestion if available
        if (hasSuggestion()) {
            sb.append("  Suggestion: ").append(suggestion).append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ErrorReport that = (ErrorReport) obj;
        
        return severity == that.severity &&
               type == that.type &&
               message.equals(that.message) &&
               (position != null ? position.equals(that.position) : that.position == null) &&
               (context != null ? context.equals(that.context) : that.context == null) &&
               (suggestion != null ? suggestion.equals(that.suggestion) : that.suggestion == null);
    }
    
    @Override
    public int hashCode() {
        int result = severity.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + message.hashCode();
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        result = 31 * result + (suggestion != null ? suggestion.hashCode() : 0);
        return result;
    }
}