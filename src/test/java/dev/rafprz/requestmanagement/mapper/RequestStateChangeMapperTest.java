package dev.rafprz.requestmanagement.mapper;

import dev.rafprz.requestmanagement.domain.RequestEntityFactory;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import dev.rafprz.requestmanagement.generated.model.TransitionAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestStateChangeMapperTest {

    private final RequestStateChangeMapper mapper = new RequestStateChangeMapperImpl();
    private final RequestEntityFactory requestEntityFactory = new RequestEntityFactory();

    @Test
    void toEntity_mapsRequestIdActionFromStateAndCurrentState() {
        //given
        final var request = requestEntityFactory.create("name", "content");

        //when
        final var entity = mapper.toEntity(request, TransitionAction.CREATE, null);

        //then
        assertThat(entity.getRequestId()).isEqualTo(request.getId());
        assertThat(entity.getAction()).isEqualTo(TransitionAction.CREATE);
        assertThat(entity.getFromState()).isNull();
        assertThat(entity.getToState()).isEqualTo(RequestState.CREATED);
        assertThat(entity.getReason()).isNull();
        assertThat(entity.getChangedAt()).isNotNull();
    }

    @Test
    void toEntity_capturesToStateFromCurrentRequestState() {
        //given
        final var request = requestEntityFactory.create("name", "content");
        request.verify();

        //when
        final var entity = mapper.toEntity(request, TransitionAction.VERIFY, RequestState.CREATED);

        //then
        assertThat(entity.getFromState()).isEqualTo(RequestState.CREATED);
        assertThat(entity.getToState()).isEqualTo(RequestState.VERIFIED);
    }

    @Test
    void toEntity_capturesReasonWhenRequestHasOne() {
        //given
        final var request = requestEntityFactory.create("name", "content");
        request.verify();
        request.reject("not good enough");

        //when
        final var entity = mapper.toEntity(request, TransitionAction.REJECT, RequestState.VERIFIED);

        //then
        assertThat(entity.getToState()).isEqualTo(RequestState.REJECTED);
        assertThat(entity.getReason()).isEqualTo("not good enough");
    }
}
