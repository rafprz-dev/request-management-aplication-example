package dev.rafprz.requestmanagement.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequestNotFoundExceptionTest {

    @Test
    void should_set_message_containing_id() {
        //given
        final var id = UUID.randomUUID();

        //when
        final var ex = new RequestNotFoundException(id);

        //then
        assertThat(ex.getMessage()).isEqualTo("Request not found: " + id);
    }

    @Test
    void isRuntimeException() {
        //given
        final var id = UUID.randomUUID();

        //when
        final var ex = new RequestNotFoundException(id);

        //then
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
