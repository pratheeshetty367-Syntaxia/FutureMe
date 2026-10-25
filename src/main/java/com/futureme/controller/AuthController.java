
package com.futureme.controller;

import com.futureme.entity.User;
import com.futureme.repository.UserRepository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Validated
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam @NotBlank @Size(max = 80) String name,
            @RequestParam @NotBlank @Email @Size(max = 190) String email,
            @RequestParam @Size(min = 8, max = 72) String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        String cleanName = name.trim();
        String cleanEmail = email.trim().toLowerCase();

        if (cleanName.isBlank()) {
            model.addAttribute("errorMessage", "Please enter your name.");
            return "signup";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Your passwords do not match.");
            return "signup";
        }

        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            model.addAttribute("errorMessage",
                    "An account with that email already exists.");
            return "signup";
        }

        try {
            User user = new User(
                    cleanName,
                    cleanEmail,
                    passwordEncoder.encode(password));

            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("errorMessage",
                    "An account with that email already exists.");
            return "signup";
        }

        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
