package com.futureme.dto;
 
import java.time.LocalDateTime;
 
/**
 * What the dashboard (and GET /api/letters) knows about a letter.
 * There is intentionally NO content field in this class, so the message
 * cannot leak through the list page or the list API.
 */
public class LetterSummary {
 
    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;
    private final LocalDateTime unlockAt;
    private final boolean locked;
    private final long secondsRemaining;
 
    public LetterSummary(Long id, String title, LocalDateTime createdAt, LocalDateTime unlockAt,
                         boolean locked, long secondsRemaining) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.unlockAt = unlockAt;
        this.locked = locked;
        this.secondsRemaining = secondsRemaining;
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
 
    public LocalDateTime getUnlockAt() {
        return unlockAt;
    }
 
    public boolean isLocked() {
        return locked;
    }
 
    public long getSecondsRemaining() {
        return secondsRemaining;
    }
}

