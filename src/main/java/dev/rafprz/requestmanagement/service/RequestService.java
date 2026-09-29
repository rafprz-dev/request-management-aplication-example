package dev.rafprz.requestmanagement.service;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import dev.rafprz.requestmanagement.domain.RequestEntityFactory;
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
import lombok.NonNull;
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
    private final RequestEntityFactory requestEntityFactory;

    public RequestDto create(@NonNull final String name,
                             @NonNull final String content) {
        RequestEntity request = requestRepository.save(requestEntityFactory.create(name, content));
        recordHistory(request, TransitionAction.CREATE, null);
        return requestMapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public RequestDto getOrThrow(@NonNull final UUID id) {
        return requestMapper.toDto(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public RequestPageDto list(final String name,
                               final RequestState state,
                               final Pageable pageable) {
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

    public RequestDto verify(final UUID id) {
        RequestEntity request = getEntityOrThrow(id);
        RequestState from = request.getState();
        request.verify();
        recordHistory(request, TransitionAction.VERIFY, from);
        return requestMapper.toDto(request);
    }

    public RequestDto accept(final UUID id) {
        RequestEntity request = getEntityOrThrow(id);
        RequestState from = request.getState();
        request.accept();
        recordHistory(request, TransitionAction.ACCEPT, from);
        return requestMapper.toDto(request);
    }

    public RequestDto reject(final UUID id,
                             final String reason) {
        RequestEntity request = getEntityOrThrow(id);
        RequestState from = request.getState();
        request.reject(reason);
        recordHistory(request, TransitionAction.REJECT, from);
        return requestMapper.toDto(request);
    }

    public RequestDto publish(final UUID id) {
        RequestEntity request = getEntityOrThrow(id);
        RequestState from = request.getState();
        long nextPublishedNumber = requestRepository.findMaxPublishedNumber() + 1;
        request.publish(nextPublishedNumber);
        recordHistory(request, TransitionAction.PUBLISH, from);
        return requestMapper.toDto(request);
    }

    public RequestDto delete(final UUID id,
                             final String reason) {
        RequestEntity request = getEntityOrThrow(id);
        RequestState from = request.getState();
        request.delete(reason);
        recordHistory(request, TransitionAction.DELETE, from);
        return requestMapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public List<StateChangeDto> history(final UUID id) {
        getEntityOrThrow(id);
        return stateChangeRepository.findByRequestIdOrderByChangedAtAsc(id).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    private RequestEntity getEntityOrThrow(final UUID id) {
        return requestRepository.findById(id).orElseThrow(() -> new RequestNotFoundException(id));
    }

    private void recordHistory(final RequestEntity request,
                               final TransitionAction action, final RequestState from) {
        stateChangeRepository.save(requestStateChangeMapper.toEntity(request, action, from));
    }
}
