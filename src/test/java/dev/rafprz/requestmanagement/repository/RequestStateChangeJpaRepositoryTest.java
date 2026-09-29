package dev.rafprz.requestmanagement.repository;

import dev.rafprz.requestmanagement.domain.RequestEntityFactory;
import dev.rafprz.requestmanagement.domain.RequestStateChangeEntity;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import dev.rafprz.requestmanagement.generated.model.TransitionAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * H2 Repository test for RequestStateChangeJpaRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RequestStateChangeJpaRepositoryTest {

    @Autowired
    private RequestStateChangeJpaRepository stateChangeRepository;

    @Autowired
    private RequestJpaRepository requestRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final RequestEntityFactory requestEntityFactory = new RequestEntityFactory();

    @Test
    void should_persist_and_assign_id() {
        final var request = requestRepository.save(requestEntityFactory.create("name", "content"));

        final var change = new RequestStateChangeEntity(
                request.getId(), TransitionAction.CREATE, null, RequestState.CREATED, null);
        final var saved = stateChangeRepository.save(change);

        assertThat(saved.getId()).isNotNull();
        final var found = stateChangeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRequestId()).isEqualTo(request.getId());
        assertThat(found.get().getAction()).isEqualTo(TransitionAction.CREATE);
        assertThat(found.get().getFromState()).isNull();
        assertThat(found.get().getToState()).isEqualTo(RequestState.CREATED);
        assertThat(found.get().getReason()).isNull();
        assertThat(found.get().getChangedAt()).isNotNull();
    }

    @Test
    void findByRequestIdOrderByChangedAtAsc_returnsEmptyList_whenNoneExist() {
        final var history =
                stateChangeRepository.findByRequestIdOrderByChangedAtAsc(UUID.randomUUID());

        assertThat(history).isEmpty();
    }

    @Test
    void should_return_only_matching_request_in_chronological_order() {
        //given
        final var request = requestRepository.save(requestEntityFactory.create("name", "content"));

        //and
        final var otherRequest = requestRepository.save(requestEntityFactory.create("other", "content"));

        //and
        final var base = OffsetDateTime.now().minusHours(1);
        stateChangeRepository.save(withChangedAt(new RequestStateChangeEntity(
                request.getId(), TransitionAction.VERIFY, RequestState.CREATED, RequestState.VERIFIED, null),
                base.plusMinutes(10)));

        //and
        stateChangeRepository.save(withChangedAt(new RequestStateChangeEntity(
                request.getId(), TransitionAction.CREATE, null, RequestState.CREATED, null),
                base));

        //and
        stateChangeRepository.save(withChangedAt(new RequestStateChangeEntity(
                request.getId(), TransitionAction.ACCEPT, RequestState.VERIFIED, RequestState.ACCEPTED, null),
                base.plusMinutes(20)));

        //and
        stateChangeRepository.save(new RequestStateChangeEntity(
                otherRequest.getId(), TransitionAction.CREATE, null, RequestState.CREATED, null));

        //and
        final var history =
                stateChangeRepository.findByRequestIdOrderByChangedAtAsc(request.getId());

        //and
        assertThat(history).hasSize(3);
        assertThat(history).extracting(RequestStateChangeEntity::getAction)
                .containsExactly(TransitionAction.CREATE, TransitionAction.VERIFY, TransitionAction.ACCEPT);
        assertThat(history).allMatch(change -> change.getRequestId().equals(request.getId()));
    }

    @Test
    void should_cascade_delete_state_changes() {
        //given
        final var request = requestRepository.save(requestEntityFactory.create("name", "content"));
        final var change = stateChangeRepository.save(new RequestStateChangeEntity(
                request.getId(), TransitionAction.CREATE, null, RequestState.CREATED, null));

        //when
        requestRepository.delete(request);
        requestRepository.flush();
        entityManager.clear();

        //then
        assertThat(stateChangeRepository.findById(change.getId())).isEmpty();
    }

    private RequestStateChangeEntity withChangedAt(final RequestStateChangeEntity entity, OffsetDateTime changedAt) {
        try {
            var field = RequestStateChangeEntity.class.getDeclaredField("changedAt");
            field.setAccessible(true);
            field.set(entity, changedAt);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return entity;
    }
}
