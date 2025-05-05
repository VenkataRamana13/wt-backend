package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.NoteDTO;
import com.wtplatform.backend.model.Client;
import com.wtplatform.backend.model.Note;
import com.wtplatform.backend.model.Note.NoteCategory;
import com.wtplatform.backend.repository.ClientRepository;
import com.wtplatform.backend.repository.NoteRepository;
import com.wtplatform.backend.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    @Override
    @Transactional
    public NoteDTO createNote(NoteDTO noteDTO) {
        logger.info("Creating new note for client ID: {}", noteDTO.getClientId());
        
        Client client = clientRepository.findById(noteDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + noteDTO.getClientId()));
        
        Note note = new Note();
        mapDTOToEntity(noteDTO, note);
        note.setClient(client);
        
        Note savedNote = noteRepository.save(note);
        return mapEntityToDTO(savedNote);
    }
    
    @Override
    @Transactional
    public NoteDTO updateNote(Long id, NoteDTO noteDTO) {
        logger.info("Updating note with ID: {}", id);
        
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with ID: " + id));
        
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
        
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with ID: " + id));
        
        return mapEntityToDTO(note);
    }
    
    @Override
    @Transactional
    public void deleteNote(Long id) {
        logger.info("Deleting note with ID: {}", id);
        
        // Verify note exists before deleting
        if (!noteRepository.existsById(id)) {
            throw new RuntimeException("Note not found with ID: " + id);
        }
        
        noteRepository.deleteById(id);
    }
    
    @Override
    public List<NoteDTO> getNotesByClientId(Long clientId) {
        logger.info("Fetching all notes for client ID: {}", clientId);
        
        // Verify client exists
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }
        
        return noteRepository.findByClientId(clientId).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<NoteDTO> getNotesByClientId(Long clientId, Pageable pageable) {
        logger.info("Fetching paginated notes for client ID: {}", clientId);
        
        // Verify client exists
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }
        
        return noteRepository.findByClientId(clientId, pageable)
                .map(this::mapEntityToDTO);
    }
    
    @Override
    public List<NoteDTO> getNotesByClientIdAndCategory(Long clientId, NoteCategory category) {
        logger.info("Fetching notes for client ID: {} with category: {}", clientId, category);
        
        // Verify client exists
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }
        
        return noteRepository.findByClientIdAndCategory(clientId, category).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<NoteDTO> getPinnedNotesByClientId(Long clientId) {
        logger.info("Fetching pinned notes for client ID: {}", clientId);
        
        // Verify client exists
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }
        
        return noteRepository.findByClientIdAndIsPinnedTrue(clientId).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<NoteDTO> searchNotes(Long clientId, String searchTerm) {
        logger.info("Searching notes for client ID: {} with term: {}", clientId, searchTerm);
        
        // Verify client exists
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }
        
        return noteRepository.searchNotes(clientId, searchTerm).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public NoteDTO toggleNotePinned(Long id) {
        logger.info("Toggling pinned status for note ID: {}", id);
        
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with ID: " + id));
        
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