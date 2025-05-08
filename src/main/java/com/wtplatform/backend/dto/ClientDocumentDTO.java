package com.wtplatform.backend.dto;

import java.time.Instant;

public class ClientDocumentDTO {
    private String fileKey;
    private String fileName;
    private String documentType;
    private Long clientId;
    private Long size;
    private Instant uploadedAt;

    public ClientDocumentDTO() {
    }

    public ClientDocumentDTO(String fileKey, String fileName, String documentType, Long clientId, Long size, Instant uploadedAt) {
        this.fileKey = fileKey;
        this.fileName = fileName;
        this.documentType = documentType;
        this.clientId = clientId;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
} 