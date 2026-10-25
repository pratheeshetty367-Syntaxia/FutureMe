package com.futureme.service;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
 
import com.futureme.dto.LetterForm;
import com.futureme.dto.LetterView;
import com.futureme.entity.Letter;
import com.futureme.exception.InvalidLetterException;
import com.futureme.exception.LetterLockedException;
import com.futureme.exception.LetterNotEditableException;
import com.futureme.exception.LetterNotFoundException;
import com.futureme.repository.LetterRepository;
 
/**
 * Pure unit tests: no Spring, no database. The repository is a Mockito mock and the
 * clock is frozen, so every test knows exactly what "now" is.
 */
class LetterServiceTest {
 
    // 2026-10-24 10:00 UTC is 15:30 in Asia/Kolkata (UTC+05:30)
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 24, 15, 30, 0);
    private static final String SECRET = "SECRET BODY";
 
    private LetterRepository repository;
    private LetterService service;
 
    @BeforeEach
    void setUp() {
        repository = mock(LetterRepository.class);
        Clock fixedClock = Clock.fixed(Instant.parse("2026-10-24T10:00:00Z"), ZoneId.of("Asia/Kolkata"));
        service = new LetterService(repository, fixedClock);
    }
 
    private LetterForm form(String title, String content, LocalDateTime unlockAt) {
        LetterForm form = new LetterForm();
        form.setTitle(title);
        form.setContent(content);
        form.setUnlockAt(unlockAt);
        return form;
    }
 
    private Letter storedLetter(LocalDateTime unlockAt) {
        Letter letter = new Letter("Stored title", SECRET, unlockAt, NOW.minusDays(10));
        letter.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(letter));
        return letter;
    }
 
    // ---------------------------------------------------------------- create
 
    @Test
    void createRejectsUnlockDateInThePast() {
        InvalidLetterException ex = assertThrows(InvalidLetterException.class,
                () -> service.create(form("Hello", "Body", NOW.minusMinutes(1))));
        assertEquals("unlockAt", ex.getField());
        verify(repository, never()).save(any(Letter.class));
    }
 
    @Test
    void createRejectsUnlockDateEqualToNow() {
        InvalidLetterException ex = assertThrows(InvalidLetterException.class,
                () -> service.create(form("Hello", "Body", NOW)));
        assertEquals("unlockAt", ex.getField());
    }
 
    @Test
    void createRejectsBlankContent() {
        InvalidLetterException ex = assertThrows(InvalidLetterException.class,
                () -> service.create(form("Hello", "   ", NOW.plusDays(1))));
        assertEquals("content", ex.getField());
    }
 
    @Test
    void createRejectsBlankTitle() {
        InvalidLetterException ex = assertThrows(InvalidLetterException.class,
                () -> service.create(form("  ", "Body", NOW.plusDays(1))));
        assertEquals("title", ex.getField());
    }
 
    @Test
    void createSavesTrimmedTitleAndUsesCurrentTimeAsCreatedAt() {
        when(repository.save(any(Letter.class))).thenAnswer(invocation -> {
            Letter letter = invocation.getArgument(0);
            letter.setId(7L);
            return letter;
        });
 
        Long id = service.create(form("  Dear me  ", "Body", NOW.plusDays(30)));
 
        assertEquals(Long.valueOf(7L), id);
        ArgumentCaptor<Letter> captor = ArgumentCaptor.forClass(Letter.class);
        verify(repository).save(captor.capture());
        Letter saved = captor.getValue();
        assertEquals("Dear me", saved.getTitle());
        assertEquals("Body", saved.getContent());
        assertEquals(NOW, saved.getCreatedAt());
        assertEquals(NOW.plusDays(30), saved.getUnlockAt());
    }
 
    // ------------------------------------------------------------------ read
 
    @Test
    void getViewHidesContentWhileLocked() {
        storedLetter(NOW.plusHours(1));
 
        LetterView view = service.getView(1L);
 
        assertTrue(view.isLocked());
        assertNull(view.getContent());
        assertEquals(3600L, view.getSecondsRemaining());
    }
 
    @Test
    void getViewShowsContentOnceUnlocked() {
        storedLetter(NOW.minusMinutes(5));
 
        LetterView view = service.getView(1L);
 
        assertFalse(view.isLocked());
        assertEquals(SECRET, view.getContent());
        assertEquals(0L, view.getSecondsRemaining());
    }
 
    @Test
    void letterUnlocksExactlyAtItsUnlockTime() {
        storedLetter(NOW);
 
        LetterView view = service.getView(1L);
 
        assertFalse(view.isLocked());
        assertEquals(SECRET, view.getContent());
    }
 
    @Test
    void getContentIfUnlockedThrowsWhileLocked() {
        storedLetter(NOW.plusMinutes(1));
 
        assertThrows(LetterLockedException.class, () -> service.getContentIfUnlocked(1L));
    }
 
    @Test
    void getContentIfUnlockedReturnsTextAfterUnlock() {
        storedLetter(NOW.minusSeconds(1));
 
        assertEquals(SECRET, service.getContentIfUnlocked(1L));
    }
 
    @Test
    void getViewThrowsWhenLetterDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
 
        assertThrows(LetterNotFoundException.class, () -> service.getView(99L));
    }
 
    // ------------------------------------------------------------------ edit
 
    @Test
    void editFormNeverContainsTheStoredMessage() {
        storedLetter(NOW.plusDays(2));
 
        LetterForm editForm = service.getEditForm(1L);
 
        assertNull(editForm.getContent());
        assertEquals("Stored title", editForm.getTitle());
        assertEquals(NOW.plusDays(2), editForm.getUnlockAt());
    }
 
    @Test
    void updateKeepsExistingContentWhenNewContentIsBlank() {
        Letter letter = storedLetter(NOW.plusDays(2));
 
        service.update(1L, form("New title", "   ", NOW.plusDays(5)));
 
        assertEquals("New title", letter.getTitle());
        assertEquals(SECRET, letter.getContent());
        assertEquals(NOW.plusDays(5), letter.getUnlockAt());
        assertEquals(NOW, letter.getUpdatedAt());
    }
 
    @Test
    void updateReplacesContentWhenNewTextIsSupplied() {
        Letter letter = storedLetter(NOW.plusDays(2));
 
        service.update(1L, form("Title", "Brand new message", NOW.plusDays(3)));
 
        assertEquals("Brand new message", letter.getContent());
    }
 
    @Test
    void updateRejectsPastUnlockDate() {
        storedLetter(NOW.plusDays(2));
 
        InvalidLetterException ex = assertThrows(InvalidLetterException.class,
                () -> service.update(1L, form("Title", "", NOW.minusDays(1))));
        assertEquals("unlockAt", ex.getField());
    }
 
    @Test
    void updateRejectsLetterThatIsAlreadyUnlocked() {
        storedLetter(NOW.minusDays(1));
 
        assertThrows(LetterNotEditableException.class,
                () -> service.update(1L, form("Title", "Text", NOW.plusDays(1))));
        assertThrows(LetterNotEditableException.class, () -> service.getEditForm(1L));
    }
 
    // ---------------------------------------------------------------- delete
 
    @Test
    void deleteThrowsWhenLetterDoesNotExist() {
        when(repository.existsById(5L)).thenReturn(false);
 
        assertThrows(LetterNotFoundException.class, () -> service.delete(5L));
        verify(repository, never()).deleteById(any());
    }
}

