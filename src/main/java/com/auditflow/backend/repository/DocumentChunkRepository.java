package com.auditflow.backend.repository;

import com.auditflow.backend.entity.DocumentChunck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunck, UUID> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO document_chunks (id, content, embedding, document_id)" +
                    "VALUES (gen_random_uuid(), :content, cast(:embedding as vector), :documentId)",
        nativeQuery = true)
    void saveVector(String content, String embedding, Long documentId);
}
