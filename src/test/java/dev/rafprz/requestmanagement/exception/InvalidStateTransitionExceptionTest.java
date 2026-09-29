package dev.rafprz.requestmanagement.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidStateTransitionExceptionTest {

    @Test
    void constructor_setsMessage() {
        InvalidStateTransitionException ex = new InvalidStateTransitionException("cannot publish from CREATED");

        assertThat(ex.getMessage()).isEqualTo("cannot publish from CREATED");
    }

    @Test
    void isRuntimeException() {
        InvalidStateTransitionException ex = new InvalidStateTransitionException("some reason");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
