package com.auditflow.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "document_chunks")
@Data
public class DocumentChunck {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private Long documentId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Transient
    private Object embedding;
}
