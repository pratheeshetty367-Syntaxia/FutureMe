package com.futureme.repository;
 
import java.time.LocalDateTime;
 
/**
 * A "projection": a read-only view of a letter that deliberately has NO
 * content field. Spring Data JPA only selects these four columns from the
 * database, so the secret message is not even loaded when we build the list page.
 */
public interface LetterListItem {
 
    Long getId();
 
    String getTitle();
 
    LocalDateTime getCreatedAt();
 
    LocalDateTime getUnlockAt();
}
