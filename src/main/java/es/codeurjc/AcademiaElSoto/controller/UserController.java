package es.codeurjc.AcademiaElSoto.controller;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.model.Comment;
import es.codeurjc.AcademiaElSoto.service.UserService;
import es.codeurjc.AcademiaElSoto.repository.CommentRepository;
import es.codeurjc.AcademiaElSoto.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // --- RUTAS PARA MOSTRAR LOS FORMULARIOS (GET) ---

    @GetMapping("/register")
    public String showRegisterForm() {
        return "auth/register";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login"; 
    }

    // --- RUTAS PARA PROCESAR LOS DATOS (POST) ---

    @PostMapping("/register")
    public String registerUser(Model model, User user) {
        
        // Comprobamos si el nombre o el correo ya existen
        if (userRepository.findByUserName(user.getUserName()).isPresent() || 
            userRepository.findByEmail(user.getEmail()).isPresent()) {
            
            model.addAttribute("error", "El nombre de usuario o el correo ya están en uso. ¡Prueba con otro!");
            return "register";
        }

        // Le creamos un carrito vacío y se lo damos al usuario
        Cart newCart = new Cart("Carrito de " + user.getUserName(), 0);
        user.setCart(newCart);

        // Guardamos el usuario
        userRepository.save(user);
        
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String loginUser(Model model, 
                            @RequestParam String usernameOrEmail, 
                            @RequestParam String password, 
                            HttpSession session) {
        
        Optional<User> userOpt = userRepository.findByUserNameOrEmail(usernameOrEmail, usernameOrEmail);

        // Si el usuario existe y la contraseña es correcta:
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            
            User loggedUser = userOpt.get();
            
            // Guardamos al usuario en la sesión
            session.setAttribute("loggedUser", loggedUser);
            
            // Buscamos el ID de su carrito en la base de datos y lo metemos en la sesión.
            if (loggedUser.getCart() != null) {
                session.setAttribute("cartId", loggedUser.getCart().getId());
            }
            
            return "redirect:/profile"; 
        } else {
            model.addAttribute("error", "Usuario, correo o contraseña incorrectos.");
            return "login";
        }
    }

    // Ruta extra para cerrar sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Destruye la sesión (incluido el carrito)
        return "redirect:/index";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        
        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser != null) {
            // 1. Pasamos los datos básicos
            model.addAttribute("userName", loggedUser.getUserName());
            model.addAttribute("lastName", loggedUser.getLastName());
            model.addAttribute("email", loggedUser.getEmail());
            
            // 2. Buscamos TODOS los comentarios que ha escrito este usuario
            List<Comment> misComentarios = commentRepository.findByUser(loggedUser.getUserName());
            
            // 3. Se los pasamos a Mustache para que los pinte
            model.addAttribute("userComments", misComentarios);
            
            return "user"; 
        }

        return "redirect:/login"; 
    }
}