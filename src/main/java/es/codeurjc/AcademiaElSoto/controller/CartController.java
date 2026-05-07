package es.codeurjc.AcademiaElSoto.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// A09: Logger for monitoring financial-related transactions (Cart actions)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.service.CartService;
import es.codeurjc.AcademiaElSoto.service.CourseService;
import es.codeurjc.AcademiaElSoto.service.UserService;
import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    // A09: Initializing Logger for audit trails
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final CourseService courseService;
    private final UserService userService;

    public CartController(CartService cartService, CourseService courseService, UserService userService) {
        this.cartService = cartService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model, Authentication authentication) {
        List<Course> cartCourses = new ArrayList<>();
        int totalCourses = 0;
        int totalPrice = 0;

        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> userOpt = userService.findByUserName(authentication.getName());

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Cart cart = user.getCart();

                if (cart != null) {
                    cartCourses = cartService.getCourses(cart);
                    totalCourses = cartService.totalCourses(cart);
                    totalPrice = cartService.totalPrice(cart);
                    session.setAttribute("cartId", cart.getId());
                }
            }
        }

        model.addAttribute("cartCourses", cartCourses);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalPrice", totalPrice);

        return "cart";
    }

    @PostMapping("/course/{id}/add-cart")
    public String addToCart(@PathVariable long id, Authentication authentication, HttpSession session) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUserName(authentication.getName());
        Optional<Course> courseOpt = courseService.findById(id);

        if (userOpt.isPresent() && courseOpt.isPresent()) {
            User user = userOpt.get();
            Course course = courseOpt.get();
            Cart cart = user.getCart();

            // A08: Data Integrity - Prevent adding already purchased courses
            if (user.getPurchasedCourses().contains(course)) {
                log.warn("User '{}' tried to add an already purchased course: {}", user.getUserName(), course.getCourseName());
                return "redirect:/courses";
            }

            if (cart == null) {
                cart = new Cart("Cart for " + user.getUserName(), 0);
                user.setCart(cart);
            }

            if (!cart.getCourses().contains(course)) {
                cart.addCourse(course);
                log.info("USER ACTION: Course '{}' added to cart by user '{}'", course.getCourseName(), user.getUserName());
            }

            cartService.save(cart);
            userService.saveUser(user);
            session.setAttribute("cartId", cart.getId());
        }

        return "redirect:/courses";
    }

    @PostMapping("/cart/remove/{id}")
    public String removeCourse(@PathVariable long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUserName(authentication.getName());
        Optional<Course> courseOpt = courseService.findById(id);

        if (userOpt.isPresent() && courseOpt.isPresent()) {
            User user = userOpt.get();
            Cart cart = user.getCart();
            Course course = courseOpt.get();

            if (cart != null && cart.getCourses() != null) {
                cart.getCourses().remove(course);
                cart.setPrice(cartService.totalPrice(cart));
                cartService.save(cart);
                log.info("USER ACTION: Course '{}' removed from cart by user '{}'", course.getCourseName(), user.getUserName());
            }
        }

        return "redirect:/cart";
    }

    @PostMapping("/complete-purchase")
    public String completePurchase(HttpSession session, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUserName(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        Cart cart = user.getCart();
        
        if (cart == null || cart.getCourses().isEmpty()) {
            return "course_purchase_error";
        }

        List<Course> cartCourses = new ArrayList<>(cart.getCourses());

        for (Course course : cartCourses) {
            // A08: Business Logic Integrity - Final check before granting access
            if (!user.getPurchasedCourses().contains(course)) {
                
                // Logic check: If 'students' is the limit, verify there is room
                // existingCourse.getStudents() is the 'int' capacity we updated earlier
                if (course.getStudents() > 0) {
                    user.addPurchasedCourse(course);
                    // Optional: Decrease capacity if 'students' represents available slots
                    // course.setStudents(course.getStudents() - 1); 
                } else {
                    log.warn("Purchase failed for course '{}': No capacity left.", course.getCourseName());
                    return "course_purchase_error";
                }
            }
        }

        // Clear cart after successful transaction
        cart.getCourses().clear();
        cart.setPrice(0);

        cartService.save(cart);
        userService.saveUser(user);
        
        log.info("TRANSACTION SUCCESS: User '{}' completed purchase of {} courses.", 
                 user.getUserName(), cartCourses.size());

        session.setAttribute("cartId", cart.getId());
        return "completed_purchase";
    }
}