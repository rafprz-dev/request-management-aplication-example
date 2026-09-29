package dev.rafprz.requestmanagement.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleNotFound_returns404WithMessageAndNoDetails() {
        //given
        final var id = UUID.randomUUID();
        final var ex = new RequestNotFoundException(id);

        //when
        final var response = handler.handleNotFound(ex);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        final var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getError()).isEqualTo("Not Found");
        assertThat(body.getMessage()).isEqualTo("Request not found: " + id);
        assertThat(body.getTimestamp()).isNotNull();
        assertThat(body.getDetails()).isEmpty();
    }

    @Test
    void should_return_409_with_message_and_no_details() {
        //given
        final var ex = new InvalidStateTransitionException("cannot publish from CREATED");

        //when
        final var response = handler.handleConflict(ex);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        final var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(409);
        assertThat(body.getError()).isEqualTo("Conflict");
        assertThat(body.getMessage()).isEqualTo("cannot publish from CREATED");
        assertThat(body.getDetails()).isEmpty();
    }

    @Test
    void should_return_400_with_field_error_details() throws NoSuchMethodException {
        //given
        Method method = SampleTarget.class.getDeclaredMethod("sample", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        BindingResult bindingResult = new BeanPropertyBindingResult(new SampleTarget(), "createRequestDto");
        bindingResult.addError(new FieldError("createRequestDto", "name", "must not be blank"));
        bindingResult.addError(new FieldError("createRequestDto", "content", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        //when
        final var response = handler.handleValidation(ex);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getError()).isEqualTo("Bad Request");
        assertThat(body.getMessage()).isEqualTo("Validation failed");
        assertThat(body.getDetails())
                .containsExactlyInAnyOrder("name: must not be blank", "content: must not be blank");
    }

    @Test
    void should_return_400_with_property_path_details() {
        //given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("list.page");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be greater than or equal to 0");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        //when
        final var response = handler.handleConstraintViolation(ex);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getError()).isEqualTo("Bad Request");
        assertThat(body.getMessage()).isEqualTo("Validation failed");
        assertThat(body.getDetails()).containsExactly("list.page: must be greater than or equal to 0");
    }

    @SuppressWarnings("unused")
    private static final class SampleTarget {
        void sample(String name) {
        }
    }
}
