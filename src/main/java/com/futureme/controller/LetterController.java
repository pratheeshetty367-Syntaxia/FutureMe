package com.futureme.controller;
 
import java.time.format.DateTimeFormatter;
 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
import com.futureme.dto.LetterForm;
import com.futureme.exception.InvalidLetterException;
import com.futureme.service.LetterService;
 
import jakarta.validation.Valid;
 
/**
 * Handles the web pages (HTML). Every method either returns the NAME of a Thymeleaf
 * template in src/main/resources/templates, or a "redirect:" to another URL.
 */
@Controller
public class LetterController {
 
    /** The text format used by <input type="datetime-local">. */
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
 
    private final LetterService letterService;
 
    public LetterController(LetterService letterService) {
        this.letterService = letterService;
    }
 
    @GetMapping("/")
    public String home() {
        return "redirect:/letters";
    }
 
    // --------------------------------------------------------------- dashboard
 
    @GetMapping("/letters")
    public String list(Model model) {
        model.addAttribute("letters", letterService.findAllSummaries());
        return "letters";
    }
 
    // ------------------------------------------------------------------ create
 
    @GetMapping("/letters/new")
    public String newLetterForm(Model model) {
        model.addAttribute("letterForm", new LetterForm());
        prepareForm(model, false, "/letters", "/letters");
        return "letter-form";
    }
 
    @PostMapping("/letters")
    public String createLetter(@Valid @ModelAttribute("letterForm") LetterForm form,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                Long id = letterService.create(form);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Your letter has been sealed. It will open on the date you chose.");
                return "redirect:/letters/" + id;
            } catch (InvalidLetterException ex) {
                result.rejectValue(ex.getField(), "invalid.letter", ex.getMessage());
            }
        }
        prepareForm(model, false, "/letters", "/letters");
        return "letter-form";
    }
 
    // -------------------------------------------------------------------- read
 
    @GetMapping("/letters/{id}")
    public String letterDetails(@PathVariable Long id, Model model) {
        model.addAttribute("letter", letterService.getView(id));
        return "letter-detail";
    }
 
    // -------------------------------------------------------------------- edit
 
    @GetMapping("/letters/{id}/edit")
    public String editLetterForm(@PathVariable Long id, Model model) {
        model.addAttribute("letterForm", letterService.getEditForm(id));
        prepareForm(model, true, "/letters/" + id + "/edit", "/letters/" + id);
        return "letter-form";
    }
 
    @PostMapping("/letters/{id}/edit")
    public String updateLetter(@PathVariable Long id,
                               @Valid @ModelAttribute("letterForm") LetterForm form,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                letterService.update(id, form);
                redirectAttributes.addFlashAttribute("successMessage", "Your letter has been updated.");
                return "redirect:/letters/" + id;
            } catch (InvalidLetterException ex) {
                result.rejectValue(ex.getField(), "invalid.letter", ex.getMessage());
            }
        }
        prepareForm(model, true, "/letters/" + id + "/edit", "/letters/" + id);
        return "letter-form";
    }
 
    // ------------------------------------------------------------------ delete
 
   
 // ------------------------------------------------------------------ delete

@PostMapping("/letters/{id}/delete")
public String deleteLetter(@PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
    letterService.delete(id);
    redirectAttributes.addFlashAttribute(
            "successMessage", "The letter has been deleted.");
    return "redirect:/letters";
}
 
    // ----------------------------------------------------------------- helpers
 
    /** Adds the values the shared letter-form.html template needs. */
    private void prepareForm(Model model, boolean editMode, String formAction, String cancelUrl) {
        model.addAttribute("editMode", editMode);
        model.addAttribute("pageTitle", editMode ? "Edit letter" : "Write a letter");
        model.addAttribute("formAction", formAction);
        model.addAttribute("cancelUrl", cancelUrl);
        model.addAttribute("minUnlock", letterService.now().plusMinutes(1).format(INPUT_FORMAT));
    }
}
