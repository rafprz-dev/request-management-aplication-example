package dev.rafprz.requestmanagement.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequestNotFoundExceptionTest {

    @Test
    void constructor_setsMessageContainingId() {
        UUID id = UUID.randomUUID();

        RequestNotFoundException ex = new RequestNotFoundException(id);

        assertThat(ex.getMessage()).isEqualTo("Request not found: " + id);
    }

    @Test
    void isRuntimeException() {
        RequestNotFoundException ex = new RequestNotFoundException(UUID.randomUUID());

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
