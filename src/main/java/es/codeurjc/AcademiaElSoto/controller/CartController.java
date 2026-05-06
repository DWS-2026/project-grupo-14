package es.codeurjc.AcademiaElSoto.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

                cartCourses = cartService.getCourses(cart);
                totalCourses = cartService.totalCourses(cart);
                totalPrice = cartService.totalPrice(cart);

                if (cart != null) {
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

            if (cart == null) {
                cart = new Cart("Carrito de " + user.getUserName(), 0);
                user.setCart(cart);
            }

            if (!cart.getCourses().contains(course)) {
                cart.addCourse(course);
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
        List<Course> cartCourses = cartService.getCourses(cart);

        if (cartCourses.isEmpty()) {
            return "course_purchase_error";
        }

        for (Course course : cartCourses) {
            if (!user.getPurchasedCourses().contains(course)) {
                user.addPurchasedCourse(course);
            }
        }

        cart.getCourses().clear();
        cart.setPrice(0);

        cartService.save(cart);
        userService.saveUser(user);

        session.setAttribute("cartId", cart.getId());

        return "completed_purchase";
    }
}