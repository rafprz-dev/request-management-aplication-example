package dev.rafprz.requestmanagement.mapper;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import dev.rafprz.requestmanagement.domain.RequestEntityFactory;
import dev.rafprz.requestmanagement.domain.RequestStateChangeEntity;
import dev.rafprz.requestmanagement.generated.model.RequestDto;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import dev.rafprz.requestmanagement.generated.model.TransitionAction;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMapperTest {

    private final RequestMapper mapper = new RequestMapperImpl();
    private final RequestEntityFactory requestEntityFactory = new RequestEntityFactory();

    @Test
    void should_map_new_request_to_dto_with_undefined_optional_fields() {
        //given
        final var entity = requestEntityFactory.create("name", "content");

        //when
        final var dto = mapper.toDto(entity);

        //then
        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getName()).isEqualTo("name");
        assertThat(dto.getContent()).isEqualTo("content");
        assertThat(dto.getState()).isEqualTo(RequestState.CREATED);
        assertThat(dto.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        assertThat(dto.getPublishedNumber().isPresent()).isFalse();
        assertThat(dto.getReason().isPresent()).isFalse();
    }

    @Test
    void should_map_published_request_with_present_published_number() {
        //given
        final var entity = requestEntityFactory.create("name", "content");
        entity.verify();
        entity.accept();
        entity.publish(42L);

        //when
        final var dto = mapper.toDto(entity);

        //then
        assertThat(dto.getState()).isEqualTo(RequestState.PUBLISHED);
        assertThat(dto.getPublishedNumber().isPresent()).isTrue();
        assertThat(dto.getPublishedNumber().get()).isEqualTo(42L);
        assertThat(dto.getReason().isPresent()).isFalse();
    }

    @Test
    void should_map_rejected_request_with_present_reason() {
        //given
        final var entity = requestEntityFactory.create("name", "content");
        entity.verify();
        entity.reject("not good enough");

        //when
        RequestDto dto = mapper.toDto(entity);

        //then
        assertThat(dto.getState()).isEqualTo(RequestState.REJECTED);
        assertThat(dto.getReason().isPresent()).isTrue();
        assertThat(dto.getReason().get()).isEqualTo("not good enough");
        assertThat(dto.getPublishedNumber().isPresent()).isFalse();
    }

    @Test
    void should_map_state_change_with_from_state_and_reason() {
        //given
        final var requestId = UUID.randomUUID();
        final var entity = new RequestStateChangeEntity(
                requestId, TransitionAction.REJECT, RequestState.VERIFIED, RequestState.REJECTED, "bad request");

        //when
        final var dto = mapper.toDto(entity);

        //then
        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getRequestId()).isEqualTo(requestId);
        assertThat(dto.getAction()).isEqualTo(TransitionAction.REJECT);
        assertThat(dto.getFromState()).isEqualTo(RequestState.VERIFIED);
        assertThat(dto.getToState()).isEqualTo(RequestState.REJECTED);
        assertThat(dto.getChangedAt()).isEqualTo(entity.getChangedAt());
        assertThat(dto.getReason().isPresent()).isTrue();
        assertThat(dto.getReason().get()).isEqualTo("bad request");
    }

    @Test
    void should_map_state_change_without_from_state_or_reason() {
        //given
        final var requestId = UUID.randomUUID();
        final var entity = new RequestStateChangeEntity(
                requestId, TransitionAction.CREATE, null, RequestState.CREATED, null);

        //when
        final var dto = mapper.toDto(entity);

        //then
        assertThat(dto.getFromState()).isNull();
        assertThat(dto.getReason().isPresent()).isFalse();
    }

    @Test
    void toPageDto_mapsContentAndPagingMetadata() {
        //given
        final var first = requestEntityFactory.create("first", "content-1");
        final var second = requestEntityFactory.create("second", "content-2");
        final var page = new PageImpl<>(List.of(first, second), PageRequest.of(0, 10), 2);

        //when
        final var dto = mapper.toPageDto(page);

        //then
        assertThat(dto.getContent()).hasSize(2);
        assertThat(dto.getContent().get(0).getId()).isEqualTo(first.getId());
        assertThat(dto.getContent().get(1).getId()).isEqualTo(second.getId());
        assertThat(dto.getPage()).isEqualTo(0);
        assertThat(dto.getSize()).isEqualTo(10);
        assertThat(dto.getTotalElements()).isEqualTo(2L);
        assertThat(dto.getTotalPages()).isEqualTo(1);
    }

    @Test
    void should_map_empty_page() {
        //given
        final var page = new PageImpl<RequestEntity>(List.of(), PageRequest.of(0, 10), 0);

        //when
        final var dto = mapper.toPageDto(page);

        //then
        assertThat(dto.getContent()).isEmpty();
        assertThat(dto.getTotalElements()).isEqualTo(0L);
        assertThat(dto.getTotalPages()).isEqualTo(0);
    }
}
