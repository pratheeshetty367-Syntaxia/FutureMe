package com.futureme.service;
 
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import com.futureme.dto.LetterForm;
import com.futureme.dto.LetterSummary;
import com.futureme.dto.LetterView;
import com.futureme.entity.Letter;
import com.futureme.exception.InvalidLetterException;
import com.futureme.exception.LetterLockedException;
import com.futureme.exception.LetterNotEditableException;
import com.futureme.exception.LetterNotFoundException;
import com.futureme.repository.LetterListItem;
import com.futureme.repository.LetterRepository;
 
/**
 * All business rules of FutureMe live here.
 *
 * THE LOCKING RULE: a letter is locked while now < unlockAt. The message text
 * leaves this class only through getView() (when unlocked) and
 * getContentIfUnlocked() (throws when locked). No other method returns content.
 */
@Service
public class LetterService {
 
    private static final int MAX_TITLE_LENGTH = 150;
    private static final int MAX_CONTENT_LENGTH = 10000;
    private static final int MAX_YEARS_AHEAD = 50;
 
    private final LetterRepository letterRepository;
    private final Clock clock;
 
    public LetterService(LetterRepository letterRepository, Clock clock) {
        this.letterRepository = letterRepository;
        this.clock = clock;
    }
 
    /** The current date and time in the application time zone, without fractions of a second. */
    public LocalDateTime now() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }
 
    // ------------------------------------------------------------------ reading
 
    @Transactional(readOnly = true)
    public List<LetterSummary> findAllSummaries() {
        LocalDateTime now = now();
        List<LetterSummary> summaries = new ArrayList<>();
        for (LetterListItem item : letterRepository.findAllByOrderByCreatedAtDesc()) {
            boolean locked = now.isBefore(item.getUnlockAt());
            summaries.add(new LetterSummary(item.getId(), item.getTitle(), item.getCreatedAt(),
                    item.getUnlockAt(), locked, secondsUntil(now, item.getUnlockAt())));
        }
        return summaries;
    }
 
    /** Returns the letter for display. The content is included ONLY if the letter is unlocked. */
    @Transactional(readOnly = true)
    public LetterView getView(Long id) {
        Letter letter = findOrThrow(id);
        LocalDateTime now = now();
        boolean locked = letter.isLockedAt(now);
        String visibleContent = locked ? null : letter.getContent();
        return new LetterView(letter.getId(), letter.getTitle(), letter.getCreatedAt(),
                letter.getUpdatedAt(), letter.getUnlockAt(), locked,
                secondsUntil(now, letter.getUnlockAt()), visibleContent);
    }
 
    /** Returns only the message text. Throws LetterLockedException (HTTP 403) while the letter is locked. */
    @Transactional(readOnly = true)
    public String getContentIfUnlocked(Long id) {
        Letter letter = findOrThrow(id);
        if (letter.isLockedAt(now())) {
            throw new LetterLockedException(id, letter.getUnlockAt());
        }
        return letter.getContent();
    }
 
    // ----------------------------------------------------------------- creating
 
    @Transactional
    public Long create(LetterForm form) {
        LocalDateTime now = now();
        String title = validateTitle(form.getTitle());
        validateRequiredContent(form.getContent());
        validateUnlockAt(form.getUnlockAt(), now);
 
        Letter letter = new Letter(title, form.getContent(), form.getUnlockAt(), now);
        return letterRepository.save(letter).getId();
    }
 
    // ------------------------------------------------------------------ editing
 
    /**
     * Prepares the edit form. The stored message is deliberately NOT copied into the form,
     * because a locked letter must never be sent to the browser.
     */
    @Transactional(readOnly = true)
    public LetterForm getEditForm(Long id) {
        Letter letter = findOrThrow(id);
        if (!letter.isLockedAt(now())) {
            throw new LetterNotEditableException(id);
        }
        LetterForm form = new LetterForm();
        form.setTitle(letter.getTitle());
        form.setUnlockAt(letter.getUnlockAt());
        form.setContent(null);
        return form;
    }
 
    /** Updates title and unlock time. The message is replaced only if new text was typed. */
    @Transactional
    public void update(Long id, LetterForm form) {
        Letter letter = findOrThrow(id);
        LocalDateTime now = now();
        if (!letter.isLockedAt(now)) {
            throw new LetterNotEditableException(id);
        }
        String title = validateTitle(form.getTitle());
        validateUnlockAt(form.getUnlockAt(), now);
 
        letter.setTitle(title);
        letter.setUnlockAt(form.getUnlockAt());
        if (form.getContent() != null && !form.getContent().isBlank()) {
            validateContentLength(form.getContent());
            letter.setContent(form.getContent());
        }
        letter.setUpdatedAt(now);
        letterRepository.save(letter);
    }
 
    // ----------------------------------------------------------------- deleting
 
    @Transactional
    public void delete(Long id) {
        if (!letterRepository.existsById(id)) {
            throw new LetterNotFoundException(id);
        }
        letterRepository.deleteById(id);
    }
 
    // ------------------------------------------------------------------ helpers
 
    private Letter findOrThrow(Long id) {
        return letterRepository.findById(id).orElseThrow(() -> new LetterNotFoundException(id));
    }
 
    /** Whole seconds until unlockAt, rounded UP, or 0 if the letter is already unlocked. */
    private long secondsUntil(LocalDateTime now, LocalDateTime unlockAt) {
        if (!now.isBefore(unlockAt)) {
            return 0;
        }
        Duration remaining = Duration.between(now, unlockAt);
        return remaining.getSeconds() + (remaining.getNano() > 0 ? 1 : 0);
    }
 
    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidLetterException("title", "Please give your letter a title.");
        }
        String trimmed = title.trim();
        if (trimmed.length() > MAX_TITLE_LENGTH) {
            throw new InvalidLetterException("title", "The title can be at most 150 characters.");
        }
        return trimmed;
    }
 
    private void validateRequiredContent(String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidLetterException("content", "Please write a message to your future self.");
        }
        validateContentLength(content);
    }
 
    private void validateContentLength(String content) {
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidLetterException("content", "The message can be at most 10000 characters.");
        }
    }
 
    private void validateUnlockAt(LocalDateTime unlockAt, LocalDateTime now) {
        if (unlockAt == null) {
            throw new InvalidLetterException("unlockAt", "Please choose an unlock date and time.");
        }
        if (!unlockAt.isAfter(now)) {
            throw new InvalidLetterException("unlockAt", "Unlock date must be in the future.");
        }
        if (unlockAt.isAfter(now.plusYears(MAX_YEARS_AHEAD))) {
            throw new InvalidLetterException("unlockAt", "Unlock date must be within the next 50 years.");
        }
    }
}
