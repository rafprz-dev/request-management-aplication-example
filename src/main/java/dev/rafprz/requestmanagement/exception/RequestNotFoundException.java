package dev.rafprz.requestmanagement.exception;

import java.util.UUID;

/** Thrown when a request cannot be found by id. Mapped to HTTP 404 by {@link ApiExceptionHandler}. */
public class RequestNotFoundException extends RuntimeException {

    public RequestNotFoundException(UUID id) {
        super("Request not found: " + id);
    }
}
