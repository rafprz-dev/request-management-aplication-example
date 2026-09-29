package dev.rafprz.requestmanagement.domain;

import dev.rafprz.requestmanagement.exception.InvalidStateTransitionException;
import dev.rafprz.requestmanagement.generated.model.RequestState;
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
import java.util.Arrays;
import java.util.UUID;

/**
 * JPA entity for the {@code requests} table. Owns the request state machine described in
 * {@code requests.yaml}: transition methods validate the current state before mutating it.
 */
@Entity
@Table(name = "requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestState state;

    @Column(name = "published_number")
    private Long publishedNumber;

    @Column(length = 1000)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    RequestEntity(String name, String content) {
        this.name = name;
        this.content = content;
        this.state = RequestState.CREATED;
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void updateContent(String content) {
        requireState(RequestState.CREATED, RequestState.VERIFIED);
        this.content = content;
        touch();
    }

    public void verify() {
        requireState(RequestState.CREATED);
        this.state = RequestState.VERIFIED;
        touch();
    }

    public void accept() {
        requireState(RequestState.VERIFIED);
        this.state = RequestState.ACCEPTED;
        touch();
    }

    public void reject(String reason) {
        requireState(RequestState.VERIFIED, RequestState.ACCEPTED);
        this.state = RequestState.REJECTED;
        this.reason = reason;
        touch();
    }

    public void publish(long publishedNumber) {
        requireState(RequestState.ACCEPTED);
        this.state = RequestState.PUBLISHED;
        this.publishedNumber = publishedNumber;
        touch();
    }

    public void delete(String reason) {
        requireState(RequestState.CREATED);
        this.state = RequestState.DELETED;
        this.reason = reason;
        touch();
    }

    private void requireState(RequestState... allowed) {
        if (Arrays.stream(allowed).noneMatch(s -> s == this.state)) {
            throw new InvalidStateTransitionException(
                    "Request " + id + " is in state " + state + " but expected one of " + Arrays.toString(allowed));
        }
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}
