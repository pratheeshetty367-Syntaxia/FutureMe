package com.futureme.dto;
 
import java.time.LocalDateTime;
 
import org.springframework.format.annotation.DateTimeFormat;
 
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
 
/**
 * The data the user types into the "write a letter" / "edit a letter" form.
 * Also used as the JSON request body of POST /api/letters.
 *
 * The content is NOT annotated as required here: when EDITING a sealed letter,
 * an empty message means "keep the existing message". The service enforces that
 * the content is present when a letter is created.
 */
public class LetterForm {
 
    @NotBlank(message = "Please give your letter a title.")
    @Size(max = 150, message = "The title can be at most 150 characters.")
    private String title;
 
    @Size(max = 10000, message = "The message can be at most 10000 characters.")
    private String content;
 
    @NotNull(message = "Please choose an unlock date and time.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime unlockAt;
 
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
 
    public LocalDateTime getUnlockAt() {
        return unlockAt;
    }
 
    public void setUnlockAt(LocalDateTime unlockAt) {
        this.unlockAt = unlockAt;
    }
}
