package dev.rafprz.requestmanagement.factory;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestEntityFactory {

    public static RequestEntity create(String name, String content) {
        return new RequestEntity(name, content);
    }
}
