package dev.rafprz.requestmanagement.exception;

/**
 * Thrown when a transition is requested from a state that does not allow it to 409.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
