import com.parsergenerator.error.ErrorReporter;

public class debug_test2 {
    public static void main(String[] args) {
        ErrorReporter errorReporter = new ErrorReporter();
        
        // Test basic functionality
        System.out.println("Initial state:");
        System.out.println("Has errors: " + errorReporter.hasErrors());
        System.out.println("Total errors: " + errorReporter.getErrorCount());
        System.out.println("INFO messages: " + errorReporter.getErrors(ErrorReporter.Severity.INFO).size());
        
        // Add an INFO message
        errorReporter.reportInfo(ErrorReporter.ErrorType.GRAMMAR_VALIDATION, "Test INFO message");
        
        System.out.println("After adding INFO message:");
        System.out.println("Has errors: " + errorReporter.hasErrors());
        System.out.println("Total errors: " + errorReporter.getErrorCount());
        System.out.println("INFO messages: " + errorReporter.getErrors(ErrorReporter.Severity.INFO).size());
        
        // Print all messages
        System.out.println("All messages:");
        for (var error : errorReporter.getErrors()) {
            System.out.println("  - " + error.getSeverity() + ": " + error.getMessage());
        }
        
        // Test the specific method used in the test
        boolean hasInfoMessages = errorReporter.getErrors(ErrorReporter.Severity.INFO).size() > 0;
        System.out.println("Has INFO messages (test condition): " + hasInfoMessages);
    }
}