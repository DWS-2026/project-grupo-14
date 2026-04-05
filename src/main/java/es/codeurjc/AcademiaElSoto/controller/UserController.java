package es.codeurjc.AcademiaElSoto.controller;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.model.Comment;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.service.UserService;
import es.codeurjc.AcademiaElSoto.repository.CommentRepository;
import es.codeurjc.AcademiaElSoto.repository.UserRepository;
import es.codeurjc.AcademiaElSoto.dto.AdminUserView;
import org.springframework.security.web.csrf.CsrfToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class UserController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- RUTAS PARA MOSTRAR LOS FORMULARIOS (GET) ---

    @GetMapping("/register")
    public String showRegisterForm() {
        return "auth/register";
    }

    @GetMapping("/login")
    public String showLoginuserForm() {
        return "auth/login";
    }

    // --- RUTAS PARA PROCESAR LOS DATOS (POST) ---

    @PostMapping("/register")
    public String registerUser(Model model, User user) {

        if (userRepository.findByUserName(user.getUserName()).isPresent() ||
                userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "El nombre de usuario o el correo ya están en uso. ¡Prueba con otro!");
            return "auth/register";
        }

        // Codificamos la contraseña con BCrypt
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        

        // Le creamos un carrito vacío y se lo damos al usuario
        Cart newCart = new Cart("Carrito de " + user.getUserName(), 0);
        user.setCart(newCart);

        // Le damos un rol por defecto
        user.setRoles(List.of("USER")); // <-- Importante para Spring Security

        // Guardamos el usuario
        userRepository.save(user);

        return "redirect:/login";
    }

    @PostMapping("/login")
    public String loginUser(Model model,
            @RequestParam String usernameOrEmail,
            @RequestParam String password,
            HttpSession session,
            HttpServletRequest request) {

        Optional<User> userOpt = userRepository.findByUserNameOrEmail(usernameOrEmail, usernameOrEmail);

        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("token", token.getToken());

        // Si el usuario existe y la contraseña es correcta:
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {

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
            return "auth/login";
        }
    }

    @GetMapping("/loginerror")
    public String loginError() {
        return "auth/loginerror";
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

        if (loggedUser == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findById(loggedUser.getId());

        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        User userFromDb = userOpt.get();

        model.addAttribute("userName", userFromDb.getUserName());
        model.addAttribute("lastName", userFromDb.getLastName());
        model.addAttribute("email", userFromDb.getEmail());

        List<Comment> misComentarios = commentRepository.findByUser(userFromDb.getUserName());
        model.addAttribute("userComments", misComentarios);

        model.addAttribute("purchasedCourses", userFromDb.getPurchasedCourses());

        return "user";
    }

    @GetMapping("/admin_users")
    public String showAdminUsers(Model model) {

        List<User> users = userService.getUsers();

        List<AdminUserView> adminUsers = users.stream().map(user -> {
            int totalPurchasedCourses = user.getPurchasedCourses() != null
                    ? user.getPurchasedCourses().size()
                    : 0;

            String purchasedCourseNames = user.getPurchasedCourses() != null && !user.getPurchasedCourses().isEmpty()
                    ? user.getPurchasedCourses().stream()
                            .map(Course::getCourseName)
                            .collect(Collectors.joining(", "))
                    : "Sin cursos";

            return new AdminUserView(
                    user.getId(),
                    user.getUserName() + " " + user.getLastName(),
                    user.getEmail(),
                    totalPurchasedCourses,
                    purchasedCourseNames);
        }).toList();

        model.addAttribute("users", adminUsers);
        return "admin_users";
    }

    @GetMapping("/admin/user/{id}")
    public String showAdminUserProfile(@PathVariable Long id, Model model) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return "user_db/user_not_found";
        }

        User user = userOpt.get();

        model.addAttribute("userName", user.getUserName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("purchasedCourses", user.getPurchasedCourses());

        List<Comment> userComments = commentRepository.findByUser(user.getUserName());
        model.addAttribute("userComments", userComments);

        return "user";
    }

    @PostMapping("/admin/user/{id}/delete")
    public String deleteUser(@PathVariable Long id) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            List<Comment> userComments = commentRepository.findByUser(user.getUserName());
            commentRepository.deleteAll(userComments);

            user.getPurchasedCourses().clear();
            userRepository.save(user);

            userRepository.delete(user);
        }

        return "user_db/deleted_user";
    }

    @GetMapping("/admin/user/edit/{id}")
    public String editUser(Model model, @PathVariable Long id) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {

            User user = userOpt.get();

            model.addAttribute("id", user.getId());
            model.addAttribute("userName", user.getUserName());
            model.addAttribute("lastName", user.getLastName());
            model.addAttribute("email", user.getEmail());

            return "user_db/edit_user_page";
        }

        return "user_db/user_not_found";
    }

    @PostMapping("/admin/user/edited/{id}")
    public String editUserProcess(Model model, @PathVariable Long id, User editedUser) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {

            User existingUser = userOpt.get();

            editedUser.setId(id);
            editedUser.setCart(existingUser.getCart());
            editedUser.setPurchasedCourses(existingUser.getPurchasedCourses());

            userRepository.save(editedUser);
            model.addAttribute("userName", editedUser.getUserName());
            model.addAttribute("lastName", editedUser.getLastName());
            model.addAttribute("email", editedUser.getEmail());

            return "user_db/edited_user";
        }

        return "user_db/user_not_found";
    }
}