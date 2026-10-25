package com.futureme.exception;
 
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
 
/** Thrown when someone tries to edit a letter whose unlock time has already passed. Becomes HTTP 409. */
@ResponseStatus(HttpStatus.CONFLICT)
public class LetterNotEditableException extends RuntimeException {
 
    public LetterNotEditableException(Long id) {
        super("Letter " + id + " is already unlocked, so it can no longer be edited.");
    }
}
