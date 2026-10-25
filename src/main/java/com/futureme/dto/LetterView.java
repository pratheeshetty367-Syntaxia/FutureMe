package com.futureme.dto;
 
import java.time.LocalDateTime;
 
/**
 * Everything the detail page (and GET /api/letters/{id}) may show about one letter.
 *
 * RULE: content is null whenever locked is true. LetterService is the only class
 * that creates LetterView objects, and it is the one place that enforces this rule.
 */
public class LetterView {
 
    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime unlockAt;
    private final boolean locked;
    private final long secondsRemaining;
    private final String content;
 
    public LetterView(Long id, String title, LocalDateTime createdAt, LocalDateTime updatedAt,
                      LocalDateTime unlockAt, boolean locked, long secondsRemaining, String content) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.unlockAt = unlockAt;
        this.locked = locked;
        this.secondsRemaining = secondsRemaining;
        this.content = content;
    }
 
    public Long getId() {
        return id;
    }
 
    public String getTitle() {
        return title;
    }
 
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
 
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
 
    public LocalDateTime getUnlockAt() {
        return unlockAt;
    }
 
    public boolean isLocked() {
        return locked;
    }
 
    public long getSecondsRemaining() {
        return secondsRemaining;
    }
 
    /** Null while the letter is locked. */
    public String getContent() {
        return content;
    }
}
