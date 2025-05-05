package com.wtplatform.backend.service;

import com.wtplatform.backend.dto.NoteDTO;
import com.wtplatform.backend.model.Note.NoteCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoteService {
    /**
     * Create a new note
     * 
     * @param noteDTO the note data
     * @return the created note
     */
    NoteDTO createNote(NoteDTO noteDTO);
    
    /**
     * Update an existing note
     * 
     * @param id the note ID
     * @param noteDTO the updated note data
     * @return the updated note
     */
    NoteDTO updateNote(Long id, NoteDTO noteDTO);
    
    /**
     * Get a note by ID
     * 
     * @param id the note ID
     * @return the note
     */
    NoteDTO getNoteById(Long id);
    
    /**
     * Delete a note
     * 
     * @param id the note ID
     */
    void deleteNote(Long id);
    
    /**
     * Get all notes for a client
     * 
     * @param clientId the client ID
     * @return list of notes
     */
    List<NoteDTO> getNotesByClientId(Long clientId);
    
    /**
     * Get paginated notes for a client
     * 
     * @param clientId the client ID
     * @param pageable pagination information
     * @return paginated notes
     */
    Page<NoteDTO> getNotesByClientId(Long clientId, Pageable pageable);
    
    /**
     * Get notes by category for a client
     * 
     * @param clientId the client ID
     * @param category the note category
     * @return list of notes
     */
    List<NoteDTO> getNotesByClientIdAndCategory(Long clientId, NoteCategory category);
    
    /**
     * Get pinned notes for a client
     * 
     * @param clientId the client ID
     * @return list of pinned notes
     */
    List<NoteDTO> getPinnedNotesByClientId(Long clientId);
    
    /**
     * Search notes by content for a client
     * 
     * @param clientId the client ID
     * @param searchTerm the search term
     * @return list of matching notes
     */
    List<NoteDTO> searchNotes(Long clientId, String searchTerm);
    
    /**
     * Toggle the pinned status of a note
     * 
     * @param id the note ID
     * @return the updated note
     */
    NoteDTO toggleNotePinned(Long id);
} 