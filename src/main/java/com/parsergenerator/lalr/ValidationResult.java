package com.parsergenerator.lalr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of parsing table validation.
 * Contains lists of errors and warnings found during validation.
 */
public class ValidationResult {
    private final List<String> errors;
    private final List<String> warnings;

    /**
     * Creates a new empty validation result.
     */
    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    /**
     * Adds an error to the validation result.
     *
     * @param error the error message to add
     */
    public void addError(String error) {
        if (error != null && !error.trim().isEmpty()) {
            errors.add(error);
        }
    }

    /**
     * Adds a warning to the validation result.
     *
     * @param warning the warning message to add
     */
    public void addWarning(String warning) {
        if (warning != null && !warning.trim().isEmpty()) {
            warnings.add(warning);
        }
    }

    /**
     * Returns an unmodifiable list of errors.
     *
     * @return the list of errors
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns an unmodifiable list of warnings.
     *
     * @return the list of warnings
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Checks if the validation result has any errors.
     *
     * @return true if there are errors, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Checks if the validation result has any warnings.
     *
     * @return true if there are warnings, false otherwise
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Checks if the validation was successful (no errors).
     *
     * @return true if validation was successful, false otherwise
     */
    public boolean isValid() {
        return !hasErrors();
    }

    /**
     * Returns the total number of issues (errors + warnings).
     *
     * @return the total number of issues
     */
    public int getTotalIssues() {
        return errors.size() + warnings.size();
    }

    /**
     * Returns a formatted string representation of all issues.
     *
     * @return formatted string with all errors and warnings
     */
    public String getFormattedReport() {
        StringBuilder sb = new StringBuilder();
        
        if (hasErrors()) {
            sb.append("ERRORS:\n");
            for (int i = 0; i < errors.size(); i++) {
                sb.append(String.format("  %d. %s\n", i + 1, errors.get(i)));
            }
        }
        
        if (hasWarnings()) {
            if (hasErrors()) {
                sb.append("\n");
            }
            sb.append("WARNINGS:\n");
            for (int i = 0; i < warnings.size(); i++) {
                sb.append(String.format("  %d. %s\n", i + 1, warnings.get(i)));
            }
        }
        
        if (!hasErrors() && !hasWarnings()) {
            sb.append("No issues found - validation successful.\n");
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("ValidationResult{errors=%d, warnings=%d}", 
                           errors.size(), warnings.size());
    }
}