package com.futureme.controller;
 
import java.net.URI;
import java.util.List;
import java.util.Map;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import com.futureme.dto.LetterForm;
import com.futureme.dto.LetterSummary;
import com.futureme.dto.LetterView;
import com.futureme.service.LetterService;
 
import jakarta.validation.Valid;
 
/**
 * A small JSON API, mainly so you can test the server-side locking with Postman.
 * It uses the same LetterService as the web pages, so the same rules apply.
 */
@RestController
@RequestMapping("/api/letters")
public class LetterApiController {
 
    private final LetterService letterService;
 
    public LetterApiController(LetterService letterService) {
        this.letterService = letterService;
    }
 
    @GetMapping
    public List<LetterSummary> list() {
        return letterService.findAllSummaries();
    }
 
    @GetMapping("/{id}")
    public LetterView get(@PathVariable Long id) {
        return letterService.getView(id);
    }
 
    /** Returns the message only if the letter is unlocked; otherwise HTTP 403. */
    @GetMapping("/{id}/content")
    public Map<String, String> content(@PathVariable Long id) {
        return Map.of("content", letterService.getContentIfUnlocked(id));
    }
 
    @PostMapping
    public ResponseEntity<LetterView> create(@Valid @RequestBody LetterForm form) {
        Long id = letterService.create(form);
        return ResponseEntity.created(URI.create("/api/letters/" + id)).body(letterService.getView(id));
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        letterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
