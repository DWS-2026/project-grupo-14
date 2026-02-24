package es.codeurjc.AcademiaElSoto;

import org.springframework.ui.Model;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class WebController {
    /* @GetMapping("/ruta")
        public String nombreMetodo() { //Cuando el usuario entra en http://localhost:8080/ruta
                                        // el navegador abre vista.html
            return "vista";
        }
    */
    @GetMapping("/cursos") 
    public String cursos() {
        return "cursos";
    }

    @GetMapping("/index") 
    public String index() {
        return "index";
    }


   @PostMapping("/user")
        public String user(Model model, @RequestParam String userName,
            @RequestParam String apellidos,
            @RequestParam String email,
            @RequestParam String contraseña
            ) {
        
        String iniciales = userName.substring(0,1) + apellidos.substring(0,1);
        model.addAttribute("iniciales", iniciales);

        model.addAttribute("userName", userName);
        model.addAttribute("apellidos", apellidos);
        model.addAttribute("email", email);
        model.addAttribute("password", contraseña);
        return "user";
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

    @GetMapping("/admin_crear_curso")
    public String admin_crear_curso() {
        return "admin_crear_curso";
    }

    @GetMapping("/admin_editar_curso")
    public String admin_editar_curso() {
        return "admin_editar_curso";
    }

    @GetMapping("/admin_estadisticas")
    public String admin_estadisticas() {
        return "admin_estadisticas";
    }

    @GetMapping("/admin_users")
    public String admin_users() {
        return "admin_users";
    }

    @GetMapping("/carrito")
    public String carrito() {
        return "carrito";
    }

}