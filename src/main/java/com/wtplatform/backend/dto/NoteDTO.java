package com.wtplatform.backend.dto;

import com.wtplatform.backend.model.Note.NoteCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class NoteDTO {
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be less than 500 characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private Instant createdAt;
    private Instant updatedAt;
    
    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    private String createdBy;
    
    @NotNull(message = "Category is required")
    private NoteCategory category;
    
    private boolean isPinned;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getClientId() {
        return clientId;
    }
    
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public NoteCategory getCategory() {
        return category;
    }
    
    public void setCategory(NoteCategory category) {
        this.category = category;
    }
    
    public boolean isPinned() {
        return isPinned;
    }
    
    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }
} 