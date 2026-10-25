package com.futureme.exception;
 
import java.time.LocalDateTime;
 
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
 
/** Thrown when someone asks for the content of a letter that is still locked. Becomes HTTP 403. */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class LetterLockedException extends RuntimeException {
 
    public LetterLockedException(Long id, LocalDateTime unlockAt) {
        super("Letter " + id + " is locked until " + unlockAt + ".");
    }
}
