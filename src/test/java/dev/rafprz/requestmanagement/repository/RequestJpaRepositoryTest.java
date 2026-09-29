package dev.rafprz.requestmanagement.repository;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import dev.rafprz.requestmanagement.domain.RequestEntityFactory;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * jpaRepository with H2 in memory database
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RequestJpaRepositoryTest {

    @Autowired
    private RequestJpaRepository requestRepository;

    private final RequestEntityFactory requestEntityFactory = new RequestEntityFactory();

    @Test
    void should_save_persists_and_assign_id() {
        //given
        final var entity = requestEntityFactory.create("name", "content");

        //when
        final var saved = requestRepository.save(entity);

        //then
        assertThat(saved.getId()).isNotNull();
        final var found = requestRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("name");
        assertThat(found.get().getContent()).isEqualTo("content");
        assertThat(found.get().getState()).isEqualTo(RequestState.CREATED);
    }

    @Test
    void should_return_zero_when_none_are_published() {
        //given
        requestRepository.save(requestEntityFactory.create("name", "content"));

        //when
        long max = requestRepository.findMaxPublishedNumber();

        //then
        assertThat(max).isZero();
    }

    @Test
    void should_return_highest_published_number() {
        //given
        final var first = requestEntityFactory.create("first", "content-1");
        first.verify();
        first.accept();
        first.publish(1L);
        requestRepository.save(first);

        //and
        final var second = requestEntityFactory.create("second", "content-2");
        second.verify();
        second.accept();
        second.publish(5L);
        requestRepository.save(second);

        //and
        final var unpublished = requestEntityFactory.create("third", "content-3");

        //when
        requestRepository.save(unpublished);

        //then
        long max = requestRepository.findMaxPublishedNumber();

        //and
        assertThat(max).isEqualTo(5L);
    }

    @Test
    void should_filter_by_name_contains() {
        //given
        requestRepository.save(requestEntityFactory.create("Alpha request", "content"));
        requestRepository.save(requestEntityFactory.create("Beta request", "content"));

        //when
        List<RequestEntity> results = requestRepository.findAll(
                RequestSpecifications.nameContains("alpha"));

        //then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Alpha request");
    }

    @Test
    void should_filter_by_state() {
        //given
        final var created = requestEntityFactory.create("created request", "content");
        requestRepository.save(created);

        //and
        final var verified = requestEntityFactory.create("verified request", "content");
        verified.verify();
        requestRepository.save(verified);

        //when
        final var results = requestRepository.findAll(
                RequestSpecifications.hasState(RequestState.VERIFIED));

        //then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("verified request");
    }

    @Test
    void should_combine_name_and_state_filters() {
        //given
        final var match = requestEntityFactory.create("Gamma request", "content");
        match.verify();
        requestRepository.save(match);

        //and
        final var wrongState = requestEntityFactory.create("Gamma request", "content");
        requestRepository.save(wrongState);

        //and
        final var wrongName = requestEntityFactory.create("Delta request", "content");
        wrongName.verify();
        requestRepository.save(wrongName);

        //and
        final var specs = List.of(
                RequestSpecifications.nameContains("gamma"),
                RequestSpecifications.hasState(RequestState.VERIFIED));

        //when
        final var results = requestRepository.findAll(Specification.allOf(specs));

        //then
        assertThat(results).hasSize(1);

        //and
        assertThat(results.get(0).getId()).isEqualTo(match.getId());
    }

    @Test
    void findById_should_return_empty_when_request_does_not_exist() {
        //given
        final var randomId = UUID.randomUUID();

        //when
        final var found = requestRepository.findById(randomId);

        //then
        assertThat(found).isEmpty();
    }
}
