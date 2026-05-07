package es.codeurjc.AcademiaElSoto.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Logger imports for system monitoring and security auditing
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.AcademiaElSoto.dto.AdminUserView;
import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Comment;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.model.Image;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.service.CommentService;
import es.codeurjc.AcademiaElSoto.service.ImageService;
import es.codeurjc.AcademiaElSoto.service.UserService;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    // SLF4J Logger for operational monitoring and security auditing
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final CommentService commentService;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService,
                          CommentService commentService,
                          ImageService imageService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.commentService = commentService;
        this.imageService = imageService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "auth/register";
    }

    @GetMapping("/login")
    public String showLoginuserForm() {
        return "auth/login";
    }

    @PostMapping("/register")
    public String registerUser(Model model, User user) {

        // Validate user uniqueness to maintain data integrity
        if (userService.existsByUserName(user.getUserName()) || userService.existsByEmail(user.getEmail())) {
            log.warn("Registration failed: Username or Email already in use: {}", user.getUserName());
            model.addAttribute("error", "The username or email is already in use.");
            return "auth/register";
        }

        // Secure password hashing before persistence
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Initializing user state (Cart and default Roles)
        Cart newCart = new Cart("Carrito de " + user.getUserName(), 0);
        user.setCart(newCart);
        user.setRoles(List.of("USER"));

        userService.saveUser(user);
        log.info("New user registered successfully: {}", user.getUserName());

        return "redirect:/login";
    }

    @GetMapping("/loginerror")
    public String loginError() {
        log.warn("Unauthorized access attempt or invalid credentials entered.");
        return "auth/loginerror";
    }

    @GetMapping("/profile")
    public String showProfile(Model model,
                              Authentication authentication,
                              HttpSession session) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUserName(authentication.getName());

        if (userOpt.isEmpty()) {
            log.error("Authenticated user session found, but user record is missing in database.");
            session.invalidate();
            return "redirect:/login";
        }

        User user = userOpt.get();

        if (user.getCart() != null) {
            session.setAttribute("cartId", user.getCart().getId());
        }

        // Passing specific user data to view to avoid exposing full entity
        model.addAttribute("id", user.getId());
        model.addAttribute("userName", user.getUserName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("hasProfileImage", user.getProfileImage() != null);

        List<Comment> userComments = commentService.findByUser(user.getUserName());
        model.addAttribute("userComments", userComments);

        List<Course> purchasedCourses = new java.util.ArrayList<>(user.getPurchasedCourses());
        model.addAttribute("purchasedCourses", purchasedCourses);

        return "user";
    }

    @GetMapping("/user/{id}/image")
    public ResponseEntity<Resource> getUserImage(@PathVariable long id) {
        try {
            Optional<User> userOptional = userService.findById(id);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (user.getProfileImage() != null) {
                    // Accessing file via internal ID to prevent Path Traversal
                    Resource file = imageService.getImageFile(user.getProfileImage().getId());

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                            .body(file);
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve profile image for user ID: {}", id);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/profile/edit")
    public String editOwnProfile(Model model,
                                 Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUserName(authentication.getName());

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

    @PostMapping("/profile/edit")
    public String editOwnProfileProcess(Authentication authentication,
                                        HttpSession session,
                                        User editedUser,
                                        @RequestParam(required = false) MultipartFile image) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUserName(authentication.getName());

        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        User existingUser = userOpt.get();

        // Data Integrity: Manual mapping ensures users only edit allowed profile fields
        existingUser.setUserName(editedUser.getUserName());
        existingUser.setLastName(editedUser.getLastName());
        existingUser.setEmail(editedUser.getEmail());

        if (editedUser.getPassword() != null && !editedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(editedUser.getPassword()));
        }

        try {
            if (image != null && !image.isEmpty()) {
                if (existingUser.getProfileImage() != null) {
                    imageService.replaceImageFile(existingUser.getProfileImage().getId(), image);
                } else {
                    Image newImage = imageService.createImage(image);
                    existingUser.setProfileImage(newImage);
                }
            }
        } catch (Exception e) {
            log.error("Profile image update failed for user: {}", existingUser.getUserName());
        }

        userService.saveUser(existingUser);
        log.info("User profile updated successfully: {}", existingUser.getUserName());

        session.invalidate(); // Force re-login to refresh security context
        return "redirect:/login";
    }

    @GetMapping("/admin/users")
    public String showAdminUsers(Model model) {
        List<User> users = userService.getUsers();

        // Using DTO for admin view to separate internal logic from presentation
        List<AdminUserView> adminUsers = users.stream().map(user -> {
            int totalPurchasedCourses = user.getPurchasedCourses() != null ? user.getPurchasedCourses().size() : 0;
            String purchasedCourseNames = user.getPurchasedCourses() != null && !user.getPurchasedCourses().isEmpty()
                    ? user.getPurchasedCourses().stream().map(Course::getCourseName).collect(Collectors.joining(", "))
                    : "None";

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

    @GetMapping("/admin/user/{id}")
    public String showAdminUserProfile(@PathVariable Long id, Model model) {
        Optional<User> userOpt = userService.findById(id);

        if (userOpt.isEmpty()) {
            return "user_db/user_not_found";
        }

        User user = userOpt.get();
        model.addAttribute("userName", user.getUserName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("purchasedCourses", user.getPurchasedCourses());

        List<Comment> userComments = commentService.findByUser(user.getUserName());
        model.addAttribute("userComments", userComments);

        return "user";
    }

    @PostMapping("/admin/user/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.warn("ADMIN ACTION: Permanent deletion of user account: {}", user.getUserName());

            // Cascading manual cleanup to ensure data integrity
            List<Comment> userComments = commentService.findByUser(user.getUserName());
            for (Comment comment : userComments) {
                commentService.deleteById(comment.getId());
            }

            user.getPurchasedCourses().clear();

            if (user.getProfileImage() != null) {
                imageService.deleteImage(user.getProfileImage().getId());
            }

            userService.saveUser(user);
            userService.deleteById(id);
        }

        return "user_db/deleted_user";
    }

    @GetMapping("/admin/user/{id}/edit")
    public String editUser(Model model, @PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);

        if (userOpt.isEmpty()) {
            return "user_db/user_not_found";
        }

        User user = userOpt.get();
        model.addAttribute("id", user.getId());
        model.addAttribute("userName", user.getUserName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());

        return "user_db/edit_user_page";
    }

    @PostMapping("/admin/user/{id}/edit")
    public String editUserProcess(Model model,
                                  @PathVariable Long id,
                                  User editedUser,
                                  @RequestParam(required = false) MultipartFile image) {

        Optional<User> userOpt = userService.findById(id);

        if (userOpt.isEmpty()) {
            log.error("Admin edit attempt on non-existent user ID: {}", id);
            return "user_db/user_not_found";
        }

        User existingUser = userOpt.get();

        // Integrity Protection: Ensuring Admin only modifies intended fields
        existingUser.setUserName(editedUser.getUserName());
        existingUser.setLastName(editedUser.getLastName());
        existingUser.setEmail(editedUser.getEmail());

        if (editedUser.getPassword() != null && !editedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(editedUser.getPassword()));
        }

        try {
            if (image != null && !image.isEmpty()) {
                if (existingUser.getProfileImage() != null) {
                    imageService.replaceImageFile(existingUser.getProfileImage().getId(), image);
                } else {
                    Image newImage = imageService.createImage(image);
                    existingUser.setProfileImage(newImage);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update user profile image via admin panel for user: {}", existingUser.getUserName());
        }

        userService.saveUser(existingUser);
        log.info("ADMIN ACTION: User ID {} successfully modified by administrator.", id);

        model.addAttribute("userName", existingUser.getUserName());
        model.addAttribute("lastName", existingUser.getLastName());
        model.addAttribute("email", existingUser.getEmail());

        return "user_db/edited_user";
    }
}