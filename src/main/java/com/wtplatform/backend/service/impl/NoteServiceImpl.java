package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.NoteDTO;
import com.wtplatform.backend.model.Client;
import com.wtplatform.backend.model.Note;
import com.wtplatform.backend.model.User;
import com.wtplatform.backend.model.Note.NoteCategory;
import com.wtplatform.backend.repository.ClientRepository;
import com.wtplatform.backend.repository.NoteRepository;
import com.wtplatform.backend.repository.UserRepository;
import com.wtplatform.backend.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {
    private static final Logger logger = LoggerFactory.getLogger(NoteServiceImpl.class);

    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get the currently authenticated user
     * 
     * @return the current user
     * @throws RuntimeException if no user is authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
    
    /**
     * Verify the client belongs to the current user
     * 
     * @param clientId the client ID
     * @return the client if it belongs to the current user
     * @throws RuntimeException if the client doesn't belong to the current user
     */
    private Client verifyClientOwnership(Long clientId) {
        User currentUser = getCurrentUser();
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        
        if (!client.getUser().getId().equals(currentUser.getId())) {
            logger.warn("User {} attempted to access client {} which belongs to user {}", 
                    currentUser.getId(), clientId, client.getUser().getId());
            throw new RuntimeException("Access denied: Client does not belong to current user");
        }
        
        return client;
    }
    
    /**
     * Verify the note belongs to a client owned by the current user
     * 
     * @param noteId the note ID
     * @return the note if it belongs to a client owned by the current user
     * @throws RuntimeException if the note doesn't belong to a client owned by the current user
     */
    private Note verifyNoteOwnership(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with ID: " + noteId));
        
        // Verify the client that owns this note belongs to the current user
        verifyClientOwnership(note.getClient().getId());
        
        return note;
    }
    
    @Override
    @Transactional
    public NoteDTO createNote(NoteDTO noteDTO) {
        logger.info("Creating new note for client ID: {}", noteDTO.getClientId());
        
        // Verify client belongs to current user
        Client client = verifyClientOwnership(noteDTO.getClientId());
        
        Note note = new Note();
        mapDTOToEntity(noteDTO, note);
        note.setClient(client);
        
        // Set the current user's email as the creator
        User currentUser = getCurrentUser();
        note.setCreatedBy(currentUser.getEmail());
        
        Note savedNote = noteRepository.save(note);
        return mapEntityToDTO(savedNote);
    }
    
    @Override
    @Transactional
    public NoteDTO updateNote(Long id, NoteDTO noteDTO) {
        logger.info("Updating note with ID: {}", id);
        
        // Verify note belongs to a client owned by the current user
        Note note = verifyNoteOwnership(id);
        
        // Only update certain fields to preserve metadata
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        note.setCategory(noteDTO.getCategory());
        note.setPinned(noteDTO.isPinned());
        
        Note updatedNote = noteRepository.save(note);
        return mapEntityToDTO(updatedNote);
    }
    
    @Override
    public NoteDTO getNoteById(Long id) {
        logger.info("Fetching note with ID: {}", id);
        
        // Verify note belongs to a client owned by the current user
        Note note = verifyNoteOwnership(id);
        
        return mapEntityToDTO(note);
    }
    
    @Override
    @Transactional
    public void deleteNote(Long id) {
        logger.info("Deleting note with ID: {}", id);
        
        // Verify note belongs to a client owned by the current user
        verifyNoteOwnership(id);
        
        noteRepository.deleteById(id);
    }
    
    @Override
    public List<NoteDTO> getNotesByClientId(Long clientId) {
        logger.info("Fetching all notes for client ID: {}", clientId);
        
        // Verify client belongs to current user
        verifyClientOwnership(clientId);
        
        return noteRepository.findByClientId(clientId).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<NoteDTO> getNotesByClientId(Long clientId, Pageable pageable) {
        logger.info("Fetching paginated notes for client ID: {}", clientId);
        
        // Verify client belongs to current user
        verifyClientOwnership(clientId);
        
        return noteRepository.findByClientId(clientId, pageable)
                .map(this::mapEntityToDTO);
    }
    
    @Override
    public List<NoteDTO> getNotesByClientIdAndCategory(Long clientId, NoteCategory category) {
        logger.info("Fetching notes for client ID: {} with category: {}", clientId, category);
        
        // Verify client belongs to current user
        verifyClientOwnership(clientId);
        
        return noteRepository.findByClientIdAndCategory(clientId, category).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<NoteDTO> getPinnedNotesByClientId(Long clientId) {
        logger.info("Fetching pinned notes for client ID: {}", clientId);
        
        // Verify client belongs to current user
        verifyClientOwnership(clientId);
        
        return noteRepository.findByClientIdAndIsPinnedTrue(clientId).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<NoteDTO> searchNotes(Long clientId, String searchTerm) {
        logger.info("Searching notes for client ID: {} with term: {}", clientId, searchTerm);
        
        // Verify client belongs to current user
        verifyClientOwnership(clientId);
        
        return noteRepository.searchNotes(clientId, searchTerm).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public NoteDTO toggleNotePinned(Long id) {
        logger.info("Toggling pinned status for note ID: {}", id);
        
        // Verify note belongs to a client owned by the current user
        Note note = verifyNoteOwnership(id);
        
        note.setPinned(!note.isPinned());
        Note updatedNote = noteRepository.save(note);
        
        return mapEntityToDTO(updatedNote);
    }
    
    private NoteDTO mapEntityToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setClientId(note.getClient().getId());
        dto.setCreatedBy(note.getCreatedBy());
        dto.setCategory(note.getCategory());
        dto.setPinned(note.isPinned());
        return dto;
    }
    
    private void mapDTOToEntity(NoteDTO dto, Note note) {
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setCreatedBy(dto.getCreatedBy());
        note.setCategory(dto.getCategory());
        note.setPinned(dto.isPinned());
    }
} 