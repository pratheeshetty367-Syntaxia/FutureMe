package com.futureme.exception;
 
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
 
/**
 * Thrown by LetterService when a business rule is broken (for example, the unlock
 * date is in the past). It remembers WHICH form field is wrong so the web controller
 * can show the message next to that field. For the REST API it becomes HTTP 400.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidLetterException extends RuntimeException {
 
    private final String field;
 
    public InvalidLetterException(String field, String message) {
        super(message);
        this.field = field;
    }
 
    public String getField() {
        return field;
    }
}
