# Implementation Plan

- [-] 1. Add multi-line detection infrastructure to GrammarLexer


  - Add private fields for tracking multi-line state (tokenBuffer, inMultiLineProduction, multiLineStartLine)
  - Create helper method `isProductionContinuation()` to detect when a line continues a production rule
  - Create helper method `isAtLineStart()` to check if current position is at the beginning of a line
  - Write unit tests for the basic detection logic
  - Run the Maven tests to verify that the current implementation doesn't break anything.
  - When every thing is ok, save the changes using `git` command.
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 2. Implement line continuation detection logic
  - Create method `detectContinuationPattern()` that identifies trailing pipe patterns and incomplete productions
  - Add logic to handle whitespace normalization on continuation lines
  - Create method `skipLeadingWhitespaceOnContinuationLine()` for proper whitespace handling
  - Write unit tests for continuation pattern detection with various whitespace scenarios
  - Run the Maven tests to verify that the current implementation doesn't break anything.
  - When every thing is ok, save the changes using `git` command.
  - _Requirements: 1.1, 1.4, 1.5_

- [ ] 3. Implement token buffering mechanism for multi-line productions
  - Create method `handleMultiLineProduction()` that buffers tokens across line boundaries
  - Modify `nextToken()` method to use token buffering when multi-line production is detected
  - Add logic to flush token buffer when production rule is complete
  - Write unit tests for token buffering with simple two-line production rules
  - Run the Maven tests to verify that the current implementation doesn't break anything.
  - When every thing is ok, save the changes using `git` command.
  - _Requirements: 1.1, 1.3, 4.1_

- [ ] 4. Enhance nextToken() method to support multi-line productions
  - Integrate multi-line detection into the main tokenization flow
  - Add state management for tracking when lexer is processing multi-line productions
  - Ensure backward compatibility by maintaining existing behavior for single-line grammars
  - Write unit tests comparing token sequences between single-line and multi-line equivalent grammars
  - Run the Maven tests to verify that the current implementation doesn't break anything.
  - When every thing is ok, save the changes using `git` command.
  - _Requirements: 1.5, 2.1, 2.2, 4.1_

- [ ] 5. Implement comprehensive error handling for multi-line productions
  - Add error detection for incomplete multi-line productions (trailing pipe with no continuation)
  - Add error detection for invalid continuation syntax
  - Create enhanced error messages that provide multi-line context with line numbers
  - Implement error recovery mechanism to reset to single-line mode after errors
  - Write unit tests for all error scenarios with verification of error message content
  - Run the Maven tests to verify that the current implementation doesn't break anything.
  - When every thing is ok, save the changes using `git` command.
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [ ] 6. Add support for complex multi-line production patterns
  - Extend handling to support productions spanning more than two lines
  - Add support for multiple alternatives distributed across different lines
  - Handle mixed single-line and multi-line productions within the same grammar file
  - Write unit tests for complex multi-line scenarios including the provided calculator grammar examples
  - Run the Maven tests to verify that the current implementation doesn't break anything.
  - When every thing is ok, save the changes using `git` command.
  - _Requirements: 1.1, 1.2, 1.3, 2.3_

- [ ] 7. Implement position tracking enhancements for multi-line tokens
  - Enhance Position class to track multi-line spans (startLine, endLine fields)
  - Update token creation to properly set position information for multi-line productions
  - Ensure error reporting includes accurate line and column information for multi-line contexts
  - Write unit tests for position tracking accuracy in multi-line scenarios
  - Run the Maven tests to verify that the current implementation doesn't break anything.
  - When every thing is ok, save the changes using `git` command.
  - _Requirements: 3.1, 3.4, 4.3_

- [ ] 8. Create comprehensive test suite for backward compatibility
  - Run all existing GrammarLexerTest cases to ensure no regressions
  - Create performance benchmark tests comparing single-line grammar processing before and after changes
  - Add integration tests using existing grammar files from examples directory
  - Verify that token sequences are identical for equivalent single-line and multi-line grammars
  - Run the Maven tests to verify that the current implementation doesn't break anything.
  - When every thing is ok, save the changes using `git` command.
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 4.2_

- [ ] 9. Add comprehensive test coverage for multi-line grammar examples
  - Create test cases using the provided calculator grammar examples in both single-line and multi-line formats
  - Test edge cases including empty continuation lines, comments within multi-line productions
  - Add tests for very long multi-line productions (10+ lines) and mixed line ending types
  - Verify proper handling of whitespace variations in continuation lines
  - Run the Maven tests to verify that the current implementation doesn't break anything.
  - When every thing is ok, save the changes using `git` command.
  - _Requirements: 1.4, 4.4, 4.5_

- [ ] 10. Optimize performance and finalize implementation
  - Implement bounded token buffering to prevent memory exhaustion with very large multi-line productions
  - Add memory usage optimization for token buffer management
  - Ensure no performance degradation for single-line grammar processing
  - Add comprehensive JavaDoc documentation for all new methods and enhanced functionality
  - Run the Maven tests to verify that the current implementation doesn't break anything.
  - When every thing is ok, save the changes using `git` command.
  - _Requirements: 2.4, 4.2, 5.1, 5.2, 5.3_