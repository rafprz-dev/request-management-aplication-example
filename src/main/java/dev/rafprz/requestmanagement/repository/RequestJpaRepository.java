package dev.rafprz.requestmanagement.repository;

import dev.rafprz.requestmanagement.domain.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RequestJpaRepository extends JpaRepository<RequestEntity, UUID>,
        JpaSpecificationExecutor<RequestEntity> {

    @Query("select coalesce(max(r.publishedNumber), 0) from RequestEntity r")
    long findMaxPublishedNumber();
}
