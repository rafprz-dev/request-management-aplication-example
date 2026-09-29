package dev.rafprz.requestmanagement.controller;

import dev.rafprz.requestmanagement.generated.api.RequestsApi;
import dev.rafprz.requestmanagement.generated.model.CreateRequestDto;
import dev.rafprz.requestmanagement.generated.model.ReasonDto;
import dev.rafprz.requestmanagement.generated.model.RequestDto;
import dev.rafprz.requestmanagement.generated.model.RequestPageDto;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import dev.rafprz.requestmanagement.generated.model.StateChangeDto;
import dev.rafprz.requestmanagement.generated.model.UpdateContentDto;
import dev.rafprz.requestmanagement.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** Implements API */
@RestController
@RequiredArgsConstructor
public class RequestController implements RequestsApi {

    private final RequestService requestService;

    @Override
    public ResponseEntity<RequestDto> createRequest(CreateRequestDto createRequestDto) {
        RequestDto created = requestService.create(createRequestDto.getName(), createRequestDto.getContent());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Override
    public ResponseEntity<RequestPageDto> listRequests(String name, RequestState state, Integer page, Integer size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(requestService.list(name, state, pageable));
    }

    @Override
    public ResponseEntity<RequestDto> getRequest(UUID id) {
        return ResponseEntity.ok(requestService.getOrThrow(id));
    }

    @Override
    public ResponseEntity<RequestDto> updateRequestContent(UUID id, UpdateContentDto updateContentDto) {
        return ResponseEntity.ok(requestService.updateContent(id, updateContentDto.getContent()));
    }

    @Override
    public ResponseEntity<RequestDto> verifyRequest(UUID id) {
        return ResponseEntity.ok(requestService.verify(id));
    }

    @Override
    public ResponseEntity<RequestDto> acceptRequest(UUID id) {
        return ResponseEntity.ok(requestService.accept(id));
    }

    @Override
    public ResponseEntity<RequestDto> publishRequest(UUID id) {
        return ResponseEntity.ok(requestService.publish(id));
    }

    @Override
    public ResponseEntity<RequestDto> rejectRequest(UUID id, ReasonDto reasonDto) {
        return ResponseEntity.ok(requestService.reject(id, reasonDto.getReason()));
    }

    @Override
    public ResponseEntity<RequestDto> deleteRequest(UUID id, ReasonDto reasonDto) {
        return ResponseEntity.ok(requestService.delete(id, reasonDto.getReason()));
    }

    @Override
    public ResponseEntity<List<StateChangeDto>> getRequestHistory(UUID id) {
        return ResponseEntity.ok(requestService.history(id));
    }
}
