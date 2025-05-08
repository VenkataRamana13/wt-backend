package com.wtplatform.backend.repository;

import com.wtplatform.backend.model.Note;
import com.wtplatform.backend.model.Note.NoteCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    /**
     * Find all notes belonging to a specific client
     */
    List<Note> findByClientId(Long clientId);
    
    /**
     * Find all notes belonging to a specific client with pagination
     */
    Page<Note> findByClientId(Long clientId, Pageable pageable);
    
    /**
     * Find all notes of a specific category for a client
     */
    List<Note> findByClientIdAndCategory(Long clientId, NoteCategory category);
    
    /**
     * Find all pinned notes for a client
     */
    List<Note> findByClientIdAndIsPinnedTrue(Long clientId);
    
    /**
     * Search for notes containing specific text in title or content for a client
     */
    @Query("SELECT n FROM Note n WHERE n.client.id = :clientId AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Note> searchNotes(@Param("clientId") Long clientId, @Param("searchTerm") String searchTerm);
    
    /**
     * Find all notes created by a specific user
     */
    List<Note> findByCreatedBy(String createdBy);
} 