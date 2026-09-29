package dev.rafprz.requestmanagement.domain;

import org.springframework.stereotype.Component;

/**
 * Centralizes construction of {@link RequestEntity} instances so creation rules live in one place.
 */
@Component
public class RequestEntityFactory {

    public RequestEntity create(final String name, final String content) {
        return new RequestEntity(name, content);
    }
}
