package com.parsergenerator.analysis;

import java.util.Collections;
import java.util.List;

/**
 * Represents the result of grammar analysis performed by GrammarAnalyzer.
 * Contains errors, warnings, and overall analysis status.
 */
public class AnalysisResult {
    private final List<String> errors;
    private final List<String> warnings;
    private final boolean hasErrors;
    
    /**
     * Constructs an AnalysisResult with the given errors, warnings, and error status.
     * 
     * @param errors list of error messages
     * @param warnings list of warning messages
     * @param hasErrors true if there are errors, false otherwise
     */
    public AnalysisResult(List<String> errors, List<String> warnings, boolean hasErrors) {
        this.errors = errors != null ? List.copyOf(errors) : Collections.emptyList();
        this.warnings = warnings != null ? List.copyOf(warnings) : Collections.emptyList();
        this.hasErrors = hasErrors;
    }
    
    /**
     * Returns an unmodifiable list of error messages.
     * 
     * @return list of errors
     */
    public List<String> getErrors() {
        return errors;
    }
    
    /**
     * Returns an unmodifiable list of warning messages.
     * 
     * @return list of warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }
    
    /**
     * Checks if the analysis found any errors.
     * 
     * @return true if errors were found, false otherwise
     */
    public boolean hasErrors() {
        return hasErrors;
    }
    
    /**
     * Checks if the analysis found any warnings.
     * 
     * @return true if warnings were found, false otherwise
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * Returns the total number of errors.
     * 
     * @return error count
     */
    public int getErrorCount() {
        return errors.size();
    }
    
    /**
     * Returns the total number of warnings.
     * 
     * @return warning count
     */
    public int getWarningCount() {
        return warnings.size();
    }
    
    /**
     * Checks if the analysis was successful (no errors).
     * 
     * @return true if no errors were found, false otherwise
     */
    public boolean isSuccessful() {
        return !hasErrors;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AnalysisResult {\n");
        sb.append("  Errors: ").append(errors.size()).append("\n");
        sb.append("  Warnings: ").append(warnings.size()).append("\n");
        sb.append("  Successful: ").append(isSuccessful()).append("\n");
        
        if (!errors.isEmpty()) {
            sb.append("  Error messages:\n");
            for (String error : errors) {
                sb.append("    - ").append(error).append("\n");
            }
        }
        
        if (!warnings.isEmpty()) {
            sb.append("  Warning messages:\n");
            for (String warning : warnings) {
                sb.append("    - ").append(warning).append("\n");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
}