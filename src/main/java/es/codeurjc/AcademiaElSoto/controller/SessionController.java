package es.codeurjc.AcademiaElSoto.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.AcademiaElSoto.model.User;
import jakarta.servlet.http.HttpSession;

@Controller
public class SessionController {

    @Autowired
    private User user;

    private String sharedInfo;

    @PostMapping("/user")
    public String user(Model model, HttpSession session,
            @RequestParam String userName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password) {

        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassword(password);
        user.setLastName(lastName);

        session.setAttribute("user", user);
        return "auth/form_result";
    }

    @GetMapping("/showResults")
    public String showResults(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            model.addAttribute("userName", user.getUserName());
            model.addAttribute("lastName", user.getLastName());
            model.addAttribute("email", user.getEmail());
        }

        return "user";
    }
}
