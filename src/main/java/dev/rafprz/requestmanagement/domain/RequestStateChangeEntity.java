package dev.rafprz.requestmanagement.domain;

import dev.rafprz.requestmanagement.generated.model.RequestState;
import dev.rafprz.requestmanagement.generated.model.TransitionAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for the {@code request_state_changes} table, an append-only audit log of every
 * transition applied to a {@link RequestEntity}.
 */
@Entity
@Table(name = "request_state_changes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class RequestStateChangeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransitionAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_state", length = 20)
    private RequestState fromState;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false, length = 20)
    private RequestState toState;

    @Column(length = 1000)
    private String reason;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    public RequestStateChangeEntity(UUID requestId, TransitionAction action, RequestState fromState,
                                     RequestState toState, String reason) {
        this.requestId = requestId;
        this.action = action;
        this.fromState = fromState;
        this.toState = toState;
        this.reason = reason;
        this.changedAt = OffsetDateTime.now();
    }
}
