package dev.rafprz.requestmanagement.repository;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests of making specification to query
 */
class RequestSpecificationsTest {

    @SuppressWarnings("unchecked")
    private final Root<RequestEntity> root = mock(Root.class);
    private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
    private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "   "})
    void should_return_null_whenNameIsNullOrBlank(final String name) {
        //when
        final var spec = RequestSpecifications.nameContains(name);

        //then
        assertThat(spec).isNull();
        verifyNoInteractions(root, cb);
    }

    @Test
    void should_build_case_insensitive_like_predicate_on_name() {
        //given
        final var namePath = mock(Path.class);

        //and
        final var loweredName = mock(Expression.class);

        //and
        final var predicate = mock(Predicate.class);

        //and
        when(root.<String>get("name")).thenReturn(namePath);
        when(cb.lower(namePath)).thenReturn(loweredName);
        when(cb.like(loweredName, "%alpha%")).thenReturn(predicate);

        //when
        final var spec = RequestSpecifications.nameContains("Alpha");
        final var result = spec.toPredicate(root, query, cb);

        //then
        verify(cb).lower(namePath);
        verify(cb).like(eq(loweredName), eq("%alpha%"));
        assertThat(result).isSameAs(predicate);
    }

    @Test
    void should_lowercase_and_wrap_the_pattern_with_wildcards() {
        //given
        final var namePath = mock(Path.class);

        //and
        final var loweredName = mock(Expression.class);

        when(root.<String>get("name")).thenReturn(namePath);
        when(cb.lower(namePath)).thenReturn(loweredName);

        //when
        final var spec = RequestSpecifications.nameContains("MiXeD CaSe");
        spec.toPredicate(root, query, cb);

        //then
        verify(cb).like(loweredName, "%mixed case%");
    }

    @Test
    void should_return_null_when_state_is_null() {
        //given
        final var spec = RequestSpecifications.hasState(null);

        //expect
        assertThat(spec).isNull();
        verifyNoInteractions(root, cb);
    }

    @Test
    void should_build_equal_predicate_on_state() {
        //given
        final var statePath = mock(Path.class);
        final var predicate = mock(Predicate.class);

        when(root.<RequestState>get("state")).thenReturn(statePath);
        when(cb.equal(statePath, RequestState.VERIFIED)).thenReturn(predicate);

        //when
        final var spec = RequestSpecifications.hasState(RequestState.VERIFIED);
        final var result = spec.toPredicate(root, query, cb);

        //then
        verify(cb).equal(statePath, RequestState.VERIFIED);
        assertThat(result).isSameAs(predicate);
    }
}
