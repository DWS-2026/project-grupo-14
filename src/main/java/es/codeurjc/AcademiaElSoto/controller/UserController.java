package es.codeurjc.AcademiaElSoto.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import es.codeurjc.AcademiaElSoto.dto.AdminUserView;
import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Comment;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.repository.CommentRepository;
import es.codeurjc.AcademiaElSoto.repository.UserRepository;
import es.codeurjc.AcademiaElSoto.service.UserService;
import jakarta.servlet.http.HttpSession;

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

    /**
     * Displays the registration form.
     */
    @GetMapping("/register")
    public String showRegisterForm() {
        return "auth/register";
    }

    /**
     * Displays the login form.
     */
    @GetMapping("/login")
    public String showLoginuserForm() {
        return "auth/login";
    }

    /**
     * Registers a new user.
     * It checks that username and email are unique, encodes the password,
     * creates an empty cart, assigns the default USER role, and saves the user.
     */
    @PostMapping("/register")
    public String registerUser(Model model, User user) {

        if (userRepository.findByUserName(user.getUserName()).isPresent() ||
                userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "El nombre de usuario o el correo ya están en uso. ¡Prueba con otro!");
            return "auth/register";
        }

        // Encode the password before saving it in the database.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Create an empty cart and assign it to the new user.
        Cart newCart = new Cart("Carrito de " + user.getUserName(), 0);
        user.setCart(newCart);

        // Assign the default role required by Spring Security.
        user.setRoles(List.of("USER"));

        // Save the new user in the database.
        userRepository.save(user);

        return "redirect:/login";
    }

    /**
     * Displays the login error page.
     */
    @GetMapping("/loginerror")
    public String loginError() {
        return "auth/loginerror";
    }

    /**
     * Displays the authenticated user's profile.
     * It loads personal data, comments, purchased courses,
     * and stores the cart id in the session if available.
     */
    @GetMapping("/profile")
    public String showProfile(Model model,
            org.springframework.security.core.Authentication authentication,
            HttpSession session) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();

        Optional<User> userOpt = userRepository.findByUserName(username);

        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        User userFromDb = userOpt.get();

        if (userFromDb.getCart() != null) {
            session.setAttribute("cartId", userFromDb.getCart().getId());
        }

        model.addAttribute("id", userFromDb.getId());
        model.addAttribute("userName", userFromDb.getUserName());
        model.addAttribute("lastName", userFromDb.getLastName());
        model.addAttribute("email", userFromDb.getEmail());

        List<Comment> misComentarios = commentRepository.findByUser(userFromDb.getUserName());
        model.addAttribute("userComments", misComentarios);

        List<Course> purchasedCourses = new java.util.ArrayList<>(userFromDb.getPurchasedCourses());
        model.addAttribute("purchasedCourses", purchasedCourses);

        return "user";
    }

    /**
     * Displays the edit page for the authenticated user's own profile.
     */
    @GetMapping("/profile/edit")
    public String editOwnProfile(Model model,
            org.springframework.security.core.Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUserName(username);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();

        model.addAttribute("id", user.getId());
        model.addAttribute("userName", user.getUserName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());

        return "user_db/edit_own_user_page";
    }

    /**
     * Processes the update of the authenticated user's own profile.
     * If a new password is provided, it is encoded before saving.
     * After updating the data, the session is invalidated and the user must log in again.
     */
    @PostMapping("/profile/edit")
    public String editOwnProfileProcess(
            org.springframework.security.core.Authentication authentication,
            HttpSession session,
            User editedUser) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUserName(username);

        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        User existingUser = userOpt.get();

        existingUser.setUserName(editedUser.getUserName());
        existingUser.setLastName(editedUser.getLastName());
        existingUser.setEmail(editedUser.getEmail());

        if (editedUser.getPassword() != null && !editedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(editedUser.getPassword()));
        }

        userRepository.save(existingUser);

        session.invalidate();
        return "redirect:/login";
    }

    /**
     * Displays the admin list of users.
     * It builds a custom view object with summarized information for each user.
     */
    @GetMapping("/admin/users")
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
        return "admin/admin_users";
    }

    /**
     * Displays the profile of a specific user from the admin panel.
     * It includes personal data, purchased courses, and comments.
     */
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

    /**
     * Deletes a user from the admin panel.
     * Before deleting the user, their comments are removed and
     * the purchased courses relationship is cleared.
     */
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

    /**
     * Displays the admin edit page for a specific user.
     */
    @GetMapping("/admin/user/{id}/edit")
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

    /**
     * Processes the admin update of a user.
     * If a new password is provided, it is encoded before saving.
     */
    @PostMapping("/admin/user/{id}/edit")
    public String editUserProcess(Model model, @PathVariable Long id, User editedUser) {

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {

            User existingUser = userOpt.get();

            existingUser.setUserName(editedUser.getUserName());
            existingUser.setLastName(editedUser.getLastName());
            existingUser.setEmail(editedUser.getEmail());

            if (editedUser.getPassword() != null && !editedUser.getPassword().isBlank()) {
                existingUser.setPassword(passwordEncoder.encode(editedUser.getPassword()));
            }

            userRepository.save(existingUser);

            model.addAttribute("userName", existingUser.getUserName());
            model.addAttribute("lastName", existingUser.getLastName());
            model.addAttribute("email", existingUser.getEmail());

            return "user_db/edited_user";
        }

        return "user_db/user_not_found";
    }
}