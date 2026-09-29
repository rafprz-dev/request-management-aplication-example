package dev.rafprz.requestmanagement.mapper;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import dev.rafprz.requestmanagement.domain.RequestStateChangeEntity;
import dev.rafprz.requestmanagement.generated.model.RequestDto;
import dev.rafprz.requestmanagement.generated.model.RequestPageDto;
import dev.rafprz.requestmanagement.generated.model.StateChangeDto;
import org.mapstruct.Mapper;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.Page;

/**
 * Maps Entity to DTO and vice versa
 */
@Mapper(componentModel = "spring")
public interface RequestMapper {

    RequestDto toDto(RequestEntity entity);

    StateChangeDto toDto(RequestStateChangeEntity entity);

    default RequestPageDto toPageDto(Page<RequestEntity> page) {
        return new RequestPageDto(
                page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    default JsonNullable<Long> wrap(Long value) {
        return value == null ? JsonNullable.undefined() : JsonNullable.of(value);
    }

    default JsonNullable<String> wrap(String value) {
        return value == null ? JsonNullable.undefined() : JsonNullable.of(value);
    }
}
