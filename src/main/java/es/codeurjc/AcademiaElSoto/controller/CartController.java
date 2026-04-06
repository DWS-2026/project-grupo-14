package es.codeurjc.AcademiaElSoto.controller;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import org.springframework.ui.Model;

import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.repository.UserRepository;
import es.codeurjc.AcademiaElSoto.repository.CartRepository;
import es.codeurjc.AcademiaElSoto.repository.CourseRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model,
            org.springframework.security.core.Authentication authentication) {

        int totalCourses = 0;
        int totalPrice = 0;
        List<Course> cartCourses = new ArrayList<>();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUserName(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Cart cart = user.getCart();

                if (cart != null && cart.getCourses() != null) {
                    cartCourses = cart.getCourses();
                    totalCourses = cartCourses.size();

                    for (Course c : cartCourses) {
                        totalPrice += c.getPrice();
                    }

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
    public String addToCart(@PathVariable long id,
            org.springframework.security.core.Authentication authentication,
            HttpSession session) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();

        Optional<User> userOpt = userRepository.findByUserName(username);
        Optional<Course> courseOpt = courseRepository.findById(id);

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

            cartRepository.save(cart);
            userRepository.save(user);

            session.setAttribute("cartId", cart.getId());
        }

        return "redirect:/courses";
    }

    @PostMapping("/cart/remove/{id}")
    public String removeCourse(@PathVariable long id,
            org.springframework.security.core.Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();

        Optional<User> userOpt = userRepository.findByUserName(username);
        Optional<Course> courseOpt = courseRepository.findById(id);

        if (userOpt.isPresent() && courseOpt.isPresent()) {
            User user = userOpt.get();
            Cart cart = user.getCart();
            Course course = courseOpt.get();

            if (cart != null && cart.getCourses() != null) {
                cart.getCourses().remove(course);

                int total = 0;
                for (Course c : cart.getCourses()) {
                    total += c.getPrice();
                }
                cart.setPrice(total);

                cartRepository.save(cart);
            }
        }

        return "redirect:/cart";
    }

    @PostMapping("/complete-purchase")
    public String completePurchase(HttpSession session, Model model,
            org.springframework.security.core.Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();

        Optional<User> userOpt = userRepository.findByUserName(username);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User userFromDb = userOpt.get();

        Cart cart = userFromDb.getCart();
        List<Course> cartCourses = new ArrayList<>();

        if (cart != null && cart.getCourses() != null) {
            cartCourses = cart.getCourses();
        }

        if (cartCourses.isEmpty()) {
            return "course_purchase_error";
        }

        for (Course course : cartCourses) {
            if (!userFromDb.getPurchasedCourses().contains(course)) {
                userFromDb.addPurchasedCourse(course);
            }
        }

        cart.getCourses().clear();
        cart.setPrice(0);

        cartRepository.save(cart);
        userRepository.save(userFromDb);

        session.setAttribute("cartId", cart.getId());

        return "completed_purchase";
    }
}