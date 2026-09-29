package dev.rafprz.requestmanagement.repository;

import dev.rafprz.requestmanagement.domain.RequestStateChangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RequestStateChangeJpaRepository extends JpaRepository<RequestStateChangeEntity, UUID> {

    List<RequestStateChangeEntity> findByRequestIdOrderByChangedAtAsc(UUID requestId);
}
