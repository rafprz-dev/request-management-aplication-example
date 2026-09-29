package dev.rafprz.requestmanagement.service;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import dev.rafprz.requestmanagement.exception.RequestNotFoundException;
import dev.rafprz.requestmanagement.generated.model.RequestDto;
import dev.rafprz.requestmanagement.generated.model.RequestPageDto;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import dev.rafprz.requestmanagement.generated.model.StateChangeDto;
import dev.rafprz.requestmanagement.generated.model.TransitionAction;
import dev.rafprz.requestmanagement.mapper.RequestMapper;
import dev.rafprz.requestmanagement.mapper.RequestStateChangeMapper;
import dev.rafprz.requestmanagement.repository.RequestJpaRepository;
import dev.rafprz.requestmanagement.repository.RequestSpecifications;
import dev.rafprz.requestmanagement.repository.RequestStateChangeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Implements state machine and records every transition in the audit log.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class RequestService {

    private final RequestJpaRepository requestRepository;
    private final RequestStateChangeJpaRepository stateChangeRepository;
    private final RequestMapper requestMapper;
    private final RequestStateChangeMapper requestStateChangeMapper;

    public RequestDto create(String name, String content) {
        RequestEntity request = requestRepository.save(new RequestEntity(name, content));
        recordHistory(request, TransitionAction.CREATE, null);
        return requestMapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public RequestDto getOrThrow(UUID id) {
        return requestMapper.toDto(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public RequestPageDto list(String name, RequestState state, Pageable pageable) {
        List<Specification<RequestEntity>> specs = Stream.of(
                        RequestSpecifications.nameContains(name),
                        RequestSpecifications.hasState(state))
                .filter(Objects::nonNull)
                .toList();
        Page<RequestEntity> page = requestRepository.findAll(Specification.allOf(specs), pageable);
        return requestMapper.toPageDto(page);
    }

    public RequestDto updateContent(UUID id, String content) {
        RequestEntity request = getEntityOrThrow(id);
        request.updateContent(content);
        return requestMapper.toDto(request);
    }

    public RequestDto verify(UUID id) {
        RequestEntity request = getEntityOrThrow(id);
        RequestState from = request.getState();
        request.verify();
        recordHistory(request, TransitionAction.VERIFY, from);
        return requestMapper.toDto(request);
    }

    public RequestDto accept(UUID id) {
        RequestEntity request = getEntityOrThrow(id);
        RequestState from = request.getState();
        request.accept();
        recordHistory(request, TransitionAction.ACCEPT, from);
        return requestMapper.toDto(request);
    }

    public RequestDto reject(UUID id, String reason) {
        RequestEntity request = getEntityOrThrow(id);
        RequestState from = request.getState();
        request.reject(reason);
        recordHistory(request, TransitionAction.REJECT, from);
        return requestMapper.toDto(request);
    }

    public RequestDto publish(UUID id) {
        RequestEntity request = getEntityOrThrow(id);
        RequestState from = request.getState();
        long nextPublishedNumber = requestRepository.findMaxPublishedNumber() + 1;
        request.publish(nextPublishedNumber);
        recordHistory(request, TransitionAction.PUBLISH, from);
        return requestMapper.toDto(request);
    }

    public RequestDto delete(UUID id, String reason) {
        RequestEntity request = getEntityOrThrow(id);
        RequestState from = request.getState();
        request.delete(reason);
        recordHistory(request, TransitionAction.DELETE, from);
        return requestMapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public List<StateChangeDto> history(UUID id) {
        getEntityOrThrow(id);
        return stateChangeRepository.findByRequestIdOrderByChangedAtAsc(id).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    private RequestEntity getEntityOrThrow(UUID id) {
        return requestRepository.findById(id).orElseThrow(() -> new RequestNotFoundException(id));
    }

    private void recordHistory(RequestEntity request, TransitionAction action, RequestState from) {
        stateChangeRepository.save(requestStateChangeMapper.toEntity(request, action, from));
    }
}
