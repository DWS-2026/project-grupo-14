package es.codeurjc.AcademiaElSoto;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/cursos")
    public String cursos() {
        return "cursos";
    }

    @GetMapping("/profesores")
    public String profesores() {
        return "profesores";
    }

    @GetMapping("/informacion")
    public String informacion() {
        return "informacion";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/carrito")
    public String carrito() {
        return "carrito";
    }

    @GetMapping("/user")
    public String user() {
        return "user";
    }
}