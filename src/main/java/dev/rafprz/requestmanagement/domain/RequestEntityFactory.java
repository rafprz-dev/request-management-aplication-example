package dev.rafprz.requestmanagement.domain;

import org.springframework.stereotype.Component;

/**
 * creates RequestEntity instances
 */
@Component
public class RequestEntityFactory {

    public RequestEntity create(final String name, final String content) {
        return new RequestEntity(name, content);
    }
}
