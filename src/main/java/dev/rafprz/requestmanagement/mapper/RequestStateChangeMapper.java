package dev.rafprz.requestmanagement.mapper;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import dev.rafprz.requestmanagement.domain.RequestStateChangeEntity;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import dev.rafprz.requestmanagement.generated.model.TransitionAction;
import org.mapstruct.Mapper;

/**
 * creates audit log entities
 */
@Mapper(componentModel = "spring")
public interface RequestStateChangeMapper {

    default RequestStateChangeEntity toEntity(RequestEntity request, TransitionAction action, RequestState fromState) {
        return new RequestStateChangeEntity(
                request.getId(), action, fromState, request.getState(), request.getReason());
    }
}
