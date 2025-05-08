package com.wtplatform.backend.dto;

public class DocumentUploadResponse {
    private String message;
    private String fileKey;
    private boolean success;
    private String documentType;
    private Long clientId;

    public DocumentUploadResponse(String message, String fileKey, boolean success, String documentType, Long clientId) {
        this.message = message;
        this.fileKey = fileKey;
        this.success = success;
        this.documentType = documentType;
        this.clientId = clientId;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
} 