package dev.rafprz.requestmanagement.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidStateTransitionExceptionTest {

    @Test
    void constructor_sets_message() {
        //when
        final var ex = new InvalidStateTransitionException("cannot publish from CREATED");

        //then
        assertThat(ex.getMessage()).isEqualTo("cannot publish from CREATED");
    }

    @Test
    void isRuntimeException() {
        //when
        final var ex = new InvalidStateTransitionException("some reason");

        //then
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
