package dev.rafprz.requestmanagement.domain;

public class RequestEntityFactory {

    public static RequestEntity create(final String name, final String content) {
        return new RequestEntity(name, content);
    }
}
