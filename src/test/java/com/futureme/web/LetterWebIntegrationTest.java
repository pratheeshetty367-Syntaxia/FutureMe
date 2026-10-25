package com.futureme.web;
 
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
 
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
 
import com.futureme.entity.Letter;
import com.futureme.repository.LetterRepository;
 
/**
 * End-to-end tests of the web layer: real controllers, real service, real Thymeleaf
 * templates and an in-memory H2 database (profile "test"). Because the real templates are
 * rendered, these tests also prove that every template and route fits together.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LetterWebIntegrationTest {
 
    private static final String SECRET = "TOP-SECRET-MESSAGE-12345";
    private static final DateTimeFormatter FORM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
 
    @Autowired
    private MockMvc mockMvc;
 
    @Autowired
    private LetterRepository letterRepository;
 
    @BeforeEach
    void cleanDatabase() {
        letterRepository.deleteAll();
    }
 
    /** Three days ahead, so the result does not depend on the time zone of the machine running the test. */
    private String futureDate() {
        return LocalDateTime.now().plusDays(3).format(FORM_FORMAT);
    }
 
    private Long createLockedLetterThroughTheForm() throws Exception {
        MvcResult result = mockMvc.perform(post("/letters")
                        .param("title", "For my future self")
                        .param("content", SECRET)
                        .param("unlockAt", futureDate()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String location = result.getResponse().getRedirectedUrl();
        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }
 
    private Letter saveUnlockedLetter() {
        return letterRepository.save(new Letter("From the past", "Hello from the past",
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(30)));
    }
 
    // ---------------------------------------------------------------- empty state
 
    @Test
    void dashboardShowsFriendlyMessageWhenThereAreNoLetters() throws Exception {
        mockMvc.perform(get("/letters"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No letters yet")));
    }
 
    // ------------------------------------------------------------- locked letters
 
    @Test
    void lockedLetterContentIsNeverSentToTheBrowser() throws Exception {
        Long id = createLockedLetterThroughTheForm();
 
        mockMvc.perform(get("/letters/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("This letter is sealed")))
                .andExpect(content().string(not(containsString(SECRET))));
 
        mockMvc.perform(get("/letters"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("For my future self")))
                .andExpect(content().string(not(containsString(SECRET))));
 
        mockMvc.perform(get("/letters/" + id + "/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(SECRET))));
 
        mockMvc.perform(get("/letters/" + id + "/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(SECRET))));
    }
 
    @Test
    void lockedLetterContentIsNeverSentByTheApi() throws Exception {
        Long id = createLockedLetterThroughTheForm();
 
        mockMvc.perform(get("/api/letters/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locked").value(true))
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(content().string(not(containsString(SECRET))));
 
        mockMvc.perform(get("/api/letters/" + id + "/content"))
                .andExpect(status().isForbidden());
 
        mockMvc.perform(get("/api/letters"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(SECRET))));
    }
 
    // ----------------------------------------------------------- unlocked letters
 
    @Test
    void unlockedLetterShowsItsContent() throws Exception {
        Letter letter = saveUnlockedLetter();
 
        mockMvc.perform(get("/letters/" + letter.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello from the past")));
 
        mockMvc.perform(get("/api/letters/" + letter.getId() + "/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello from the past"));
    }
 
    // ----------------------------------------------------------------- validation
 
    @Test
    void pastUnlockDateIsRejectedAndNothingIsSaved() throws Exception {
        String past = LocalDateTime.now().minusDays(1).format(FORM_FORMAT);
 
        mockMvc.perform(post("/letters")
                        .param("title", "Old news")
                        .param("content", "text")
                        .param("unlockAt", past))
                .andExpect(status().isOk())
                .andExpect(view().name("letter-form"))
                .andExpect(content().string(containsString("must be in the future")));
 
        assertEquals(0L, letterRepository.count());
    }
 
    @Test
    void missingTitleAndDateAreReported() throws Exception {
        mockMvc.perform(post("/letters")
                        .param("title", "")
                        .param("content", "text"))
                .andExpect(status().isOk())
                .andExpect(view().name("letter-form"))
                .andExpect(content().string(containsString("Please give your letter a title.")))
                .andExpect(content().string(containsString("Please choose an unlock date and time.")));
 
        assertEquals(0L, letterRepository.count());
    }
 
    @Test
    void missingMessageIsReported() throws Exception {
        mockMvc.perform(post("/letters")
                        .param("title", "No body")
                        .param("content", "")
                        .param("unlockAt", futureDate()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Please write a message to your future self.")));
 
        assertEquals(0L, letterRepository.count());
    }
 
    @Test
    void unparsableDateIsReportedInFriendlyLanguage() throws Exception {
        mockMvc.perform(post("/letters")
                        .param("title", "Bad date")
                        .param("content", "text")
                        .param("unlockAt", "not-a-date"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Please enter a valid date and time.")));
    }
 
    // ------------------------------------------------------------- edit and delete
 
    @Test
    void editingKeepsTheMessageWhenTheMessageBoxIsEmpty() throws Exception {
        Long id = createLockedLetterThroughTheForm();
 
        mockMvc.perform(post("/letters/" + id + "/edit")
                        .param("title", "Renamed letter")
                        .param("content", "")
                        .param("unlockAt", futureDate()))
                .andExpect(status().is3xxRedirection());
 
        Letter stored = letterRepository.findById(id).orElseThrow();
        assertEquals("Renamed letter", stored.getTitle());
        assertEquals(SECRET, stored.getContent());
    }
 
    @Test
    void unlockedLetterCannotBeEdited() throws Exception {
        Letter letter = saveUnlockedLetter();
 
        mockMvc.perform(get("/letters/" + letter.getId() + "/edit"))
                .andExpect(status().isConflict());
 
        mockMvc.perform(post("/letters/" + letter.getId() + "/edit")
                        .param("title", "Hacked")
                        .param("content", "new text")
                        .param("unlockAt", futureDate()))
                .andExpect(status().isConflict());
    }
 
    @Test
    void deleteNeedsConfirmationAndThenRemovesTheLetter() throws Exception {
        Long id = createLockedLetterThroughTheForm();
 
        mockMvc.perform(get("/letters/" + id + "/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Delete this letter?")));
        assertEquals(1L, letterRepository.count());
 
        mockMvc.perform(post("/letters/" + id + "/delete"))
                .andExpect(status().is3xxRedirection());
        assertEquals(0L, letterRepository.count());
 
        mockMvc.perform(get("/letters/" + id))
                .andExpect(status().isNotFound());
    }
 
    @Test
    void unknownLetterGivesNotFound() throws Exception {
        mockMvc.perform(get("/letters/99999"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/letters/99999"))
                .andExpect(status().isNotFound());
    }
 
    // ------------------------------------------------------------------------ API
 
    @Test
    void apiCreateRejectsPastDateAndAcceptsFutureDate() throws Exception {
        String pastJson = "{\"title\":\"x\",\"content\":\"y\",\"unlockAt\":\""
                + LocalDateTime.now().minusDays(1).format(FORM_FORMAT) + "\"}";
        mockMvc.perform(post("/api/letters").contentType(MediaType.APPLICATION_JSON).content(pastJson))
                .andExpect(status().isBadRequest());
 
        String futureJson = "{\"title\":\"x\",\"content\":\"" + SECRET + "\",\"unlockAt\":\"" + futureDate() + "\"}";
        mockMvc.perform(post("/api/letters").contentType(MediaType.APPLICATION_JSON).content(futureJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.locked").value(true))
                .andExpect(content().string(not(containsString(SECRET))));
    }
 
    @Test
    void apiDeleteRemovesTheLetter() throws Exception {
        Letter letter = saveUnlockedLetter();
 
        mockMvc.perform(delete("/api/letters/" + letter.getId()))
                .andExpect(status().isNoContent());
        assertEquals(0L, letterRepository.count());
    }
}
