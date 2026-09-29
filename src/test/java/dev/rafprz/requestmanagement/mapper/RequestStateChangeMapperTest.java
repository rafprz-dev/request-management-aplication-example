package dev.rafprz.requestmanagement.mapper;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import dev.rafprz.requestmanagement.domain.RequestStateChangeEntity;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import dev.rafprz.requestmanagement.generated.model.TransitionAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestStateChangeMapperTest {

    private final RequestStateChangeMapper mapper = new RequestStateChangeMapperImpl();

    @Test
    void toEntity_mapsRequestIdActionFromStateAndCurrentState() {
        RequestEntity request = new RequestEntity("name", "content");

        RequestStateChangeEntity entity = mapper.toEntity(request, TransitionAction.CREATE, null);

        assertThat(entity.getRequestId()).isEqualTo(request.getId());
        assertThat(entity.getAction()).isEqualTo(TransitionAction.CREATE);
        assertThat(entity.getFromState()).isNull();
        assertThat(entity.getToState()).isEqualTo(RequestState.CREATED);
        assertThat(entity.getReason()).isNull();
        assertThat(entity.getChangedAt()).isNotNull();
    }

    @Test
    void toEntity_capturesToStateFromCurrentRequestState() {
        RequestEntity request = new RequestEntity("name", "content");
        request.verify();

        RequestStateChangeEntity entity = mapper.toEntity(request, TransitionAction.VERIFY, RequestState.CREATED);

        assertThat(entity.getFromState()).isEqualTo(RequestState.CREATED);
        assertThat(entity.getToState()).isEqualTo(RequestState.VERIFIED);
    }

    @Test
    void toEntity_capturesReasonWhenRequestHasOne() {
        RequestEntity request = new RequestEntity("name", "content");
        request.verify();
        request.reject("not good enough");

        RequestStateChangeEntity entity = mapper.toEntity(request, TransitionAction.REJECT, RequestState.VERIFIED);

        assertThat(entity.getToState()).isEqualTo(RequestState.REJECTED);
        assertThat(entity.getReason()).isEqualTo("not good enough");
    }
}
