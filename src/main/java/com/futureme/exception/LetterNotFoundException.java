package com.futureme.exception;
 
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
 
/** Thrown when no letter exists with the requested id. Becomes HTTP 404. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LetterNotFoundException extends RuntimeException {
 
    public LetterNotFoundException(Long id) {
        super("No letter was found with id " + id + ".");
    }
}