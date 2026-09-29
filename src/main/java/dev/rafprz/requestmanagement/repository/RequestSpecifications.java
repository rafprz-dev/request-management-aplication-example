package dev.rafprz.requestmanagement.repository;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RequestSpecifications {

    public static Specification<RequestEntity> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<RequestEntity> hasState(RequestState state) {
        if (state == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("state"), state);
    }
}
