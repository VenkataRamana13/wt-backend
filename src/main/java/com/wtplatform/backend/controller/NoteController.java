package com.wtplatform.backend.controller;

import com.wtplatform.backend.dto.ErrorResponse;
import com.wtplatform.backend.dto.NoteDTO;
import com.wtplatform.backend.model.Note.NoteCategory;
import com.wtplatform.backend.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    @Autowired
    private NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteDTO> createNote(@Valid @RequestBody NoteDTO noteDTO) {
        logger.info("Received create note request for client ID: {}", noteDTO.getClientId());
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createNote(noteDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDTO> updateNote(@PathVariable Long id, @Valid @RequestBody NoteDTO noteDTO) {
        logger.info("Received update note request for ID: {}", id);
        return ResponseEntity.ok(noteService.updateNote(id, noteDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDTO> getNoteById(@PathVariable Long id) {
        logger.info("Received get note request for ID: {}", id);
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        logger.info("Received delete note request for ID: {}", id);
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<NoteDTO>> getNotesByClientId(@PathVariable Long clientId) {
        logger.info("Received get all notes request for client ID: {}", clientId);
        return ResponseEntity.ok(noteService.getNotesByClientId(clientId));
    }

    @GetMapping("/client/{clientId}/paged")
    public ResponseEntity<Page<NoteDTO>> getPagedNotesByClientId(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.info("Received get paged notes request for client ID: {}", clientId);
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(noteService.getNotesByClientId(clientId, pageable));
    }

    @GetMapping("/client/{clientId}/category/{category}")
    public ResponseEntity<List<NoteDTO>> getNotesByCategory(
            @PathVariable Long clientId,
            @PathVariable String category) {
        
        logger.info("Received get notes by category request: {} for client ID: {}", category, clientId);
        
        try {
            NoteCategory noteCategory = NoteCategory.valueOf(category.toUpperCase());
            return ResponseEntity.ok(noteService.getNotesByClientIdAndCategory(clientId, noteCategory));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid category: {}", category, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/client/{clientId}/pinned")
    public ResponseEntity<List<NoteDTO>> getPinnedNotes(@PathVariable Long clientId) {
        logger.info("Received get pinned notes request for client ID: {}", clientId);
        return ResponseEntity.ok(noteService.getPinnedNotesByClientId(clientId));
    }

    @GetMapping("/client/{clientId}/search")
    public ResponseEntity<List<NoteDTO>> searchNotes(
            @PathVariable Long clientId,
            @RequestParam String query) {
        
        logger.info("Received search notes request for client ID: {} with query: {}", clientId, query);
        return ResponseEntity.ok(noteService.searchNotes(clientId, query));
    }

    @PatchMapping("/{id}/toggle-pin")
    public ResponseEntity<NoteDTO> toggleNotePinned(@PathVariable Long id) {
        logger.info("Received toggle pin request for note ID: {}", id);
        try {
            NoteDTO updatedNote = noteService.toggleNotePinned(id);
            logger.info("Successfully toggled pin status for note ID: {}, isPinned: {}", id, updatedNote.isPinned());
            return ResponseEntity.ok(updatedNote);
        } catch (Exception e) {
            logger.error("Error toggling pin status for note ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new NoteDTO()); // Return empty DTO in case of error
        }
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        logger.error("Error processing request", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage()));
    }
} 