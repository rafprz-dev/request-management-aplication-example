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
 * Exercises {@link RequestJpaRepository} against a real H2 database (PostgreSQL compatibility
 * mode) with the actual Liquibase-managed schema, so queries and specifications run against
 * real data instead of mocks.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RequestJpaRepositoryTest {

    @Autowired
    private RequestJpaRepository requestRepository;

    private final RequestEntityFactory requestEntityFactory = new RequestEntityFactory();

    @Test
    void save_persistsAndAssignsId() {
        //given
        RequestEntity entity = requestEntityFactory.create("name", "content");

        //when
        RequestEntity saved = requestRepository.save(entity);

        //then
        assertThat(saved.getId()).isNotNull();
        Optional<RequestEntity> found = requestRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("name");
        assertThat(found.get().getContent()).isEqualTo("content");
        assertThat(found.get().getState()).isEqualTo(RequestState.CREATED);
    }

    @Test
    void findMaxPublishedNumber_returnsZero_whenNoneArePublished() {
        //given
        requestRepository.save(requestEntityFactory.create("name", "content"));

        //when
        long max = requestRepository.findMaxPublishedNumber();

        //then
        assertThat(max).isZero();
    }

    @Test
    void findMaxPublishedNumber_returnsHighestPublishedNumber() {
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
    void findAllWithSpecification_filtersByNameContains() {
        requestRepository.save(requestEntityFactory.create("Alpha request", "content"));
        requestRepository.save(requestEntityFactory.create("Beta request", "content"));

        List<RequestEntity> results = requestRepository.findAll(
                RequestSpecifications.nameContains("alpha"));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Alpha request");
    }

    @Test
    void findAllWithSpecification_filtersByState() {
        RequestEntity created = requestEntityFactory.create("created request", "content");
        requestRepository.save(created);

        RequestEntity verified = requestEntityFactory.create("verified request", "content");
        verified.verify();
        requestRepository.save(verified);

        List<RequestEntity> results = requestRepository.findAll(
                RequestSpecifications.hasState(RequestState.VERIFIED));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("verified request");
    }

    @Test
    void findAllWithSpecification_combinesNameAndStateFilters() {
        RequestEntity match = requestEntityFactory.create("Gamma request", "content");
        match.verify();
        requestRepository.save(match);

        RequestEntity wrongState = requestEntityFactory.create("Gamma request", "content");
        requestRepository.save(wrongState);

        RequestEntity wrongName = requestEntityFactory.create("Delta request", "content");
        wrongName.verify();
        requestRepository.save(wrongName);

        List<Specification<RequestEntity>> specs = List.of(
                RequestSpecifications.nameContains("gamma"),
                RequestSpecifications.hasState(RequestState.VERIFIED));
        List<RequestEntity> results = requestRepository.findAll(Specification.allOf(specs));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(match.getId());
    }

    @Test
    void findById_returnsEmpty_whenRequestDoesNotExist() {
        Optional<RequestEntity> found = requestRepository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }
}
