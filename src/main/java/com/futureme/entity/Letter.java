package com.futureme.entity;
 
import java.time.LocalDateTime;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
 
/**
 * One letter written to your future self.
 * Maps to the MySQL table "letters".
 *
 * All date/time values are LocalDateTime values in the application time zone
 * (see app.timezone in application.properties).
 */
@Entity
@Table(name = "letters")
public class Letter {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "title", nullable = false, length = 150)
    private String title;
 
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    @Column(name = "unlock_at", nullable = false)
    private LocalDateTime unlockAt;
 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
 
    /** Required by JPA/Hibernate. Not meant to be called from our own code. */
    protected Letter() {
    }
 
    public Letter(String title, String content, LocalDateTime unlockAt, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.unlockAt = unlockAt;
        this.createdAt = createdAt;
    }
 
    /** A letter is locked while the current time is strictly before its unlock time. */
    public boolean isLockedAt(LocalDateTime now) {
        return now.isBefore(unlockAt);
    }
 
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
 
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
 
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
 
    public LocalDateTime getUnlockAt() {
        return unlockAt;
    }
 
    public void setUnlockAt(LocalDateTime unlockAt) {
        this.unlockAt = unlockAt;
    }
 
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
 
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
