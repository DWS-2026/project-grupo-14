package es.codeurjc.AcademiaElSoto.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.repository.CartRepository;
import es.codeurjc.AcademiaElSoto.repository.CourseRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("courseId") Long courseId, HttpSession session) {

        // 1. Look up the course in the database by its ID.
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            Cart cart = null;

            // 2. Check whether the user already has a cart stored in the current session.
            Long cartId = (Long) session.getAttribute("cartId");

            if (cartId != null) {
                cart = cartRepository.findById(cartId).orElse(null);
            }

            // 3. If there is no cart yet, create one.
            if (cart == null) {
                cart = new Cart("Shopping cart", 0);
            }

            // 4. Add the selected course to the cart.
            cart.addCourse(course);

            // 5. Save the cart and keep its ID in the session.
            cartRepository.save(cart);
            session.setAttribute("cartId", cart.getId());
        }

        // 6. Redirect the user back to the courses page.
        return "redirect:/courses";
    }
}
