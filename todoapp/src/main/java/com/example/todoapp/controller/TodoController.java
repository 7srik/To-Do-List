package com.example.todoapp.controller;

import com.example.todoapp.model.TodoItem;
import com.example.todoapp.repository.TodoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TodoController {

    @Autowired
    private TodoRepository repo;

    private String APP_PASSWORD = "1234"; // Now non-final so it can be changed

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam String digit1,
            @RequestParam String digit2,
            @RequestParam String digit3,
            @RequestParam String digit4,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String enteredPassword = digit1 + digit2 + digit3 + digit4;
        
        if (APP_PASSWORD.equals(enteredPassword)) {
            session.setAttribute("loggedIn", true);
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Incorrect PIN");
            return "redirect:/login";
        }
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        if (session.getAttribute("loggedIn") == null) return "redirect:/login";
        model.addAttribute("todos", repo.findAll());
        return "index";
    }

    @PostMapping("/add")
    public String addTodo(@RequestParam String description, HttpSession session) {
        if (session.getAttribute("loggedIn") == null) return "redirect:/login";
        TodoItem item = new TodoItem();
        item.setDescription(description);
        item.setCompleted(false);
        repo.save(item);
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteTodo(@RequestParam Long id, HttpSession session) {
        if (session.getAttribute("loggedIn") == null) return "redirect:/login";
        repo.deleteById(id);
        return "redirect:/";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedIn") == null) return "redirect:/login";
        return "change-password"; // This should match the template filename without .html
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedIn") == null) return "redirect:/login";
        
        if (!APP_PASSWORD.equals(oldPassword)) {
            redirectAttributes.addFlashAttribute("error", "Old password is incorrect");
            return "redirect:/change-password";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords don't match");
            return "redirect:/change-password";
        }
        
        if (newPassword.length() != 4) {
            redirectAttributes.addFlashAttribute("error", "Password must be 4 digits");
            return "redirect:/change-password";
        }
        
        APP_PASSWORD = newPassword;
        redirectAttributes.addFlashAttribute("success", "Password changed successfully");
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}