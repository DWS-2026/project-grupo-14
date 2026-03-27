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
    public String viewCart(HttpSession session, Model model) {

        // 1. Preparamos unos valores por defecto (carrito vacío)
        int totalCourses = 0;
        int totalPrice = 0;
        List<Course> cartCourses = new ArrayList<>(); // Importa java.util.ArrayList si te lo pide

        // 2. Buscamos si el usuario tiene un carrito en su sesión actual
        Long cartId = (Long) session.getAttribute("cartId");

        if (cartId != null) {
            Optional<Cart> cartOpt = cartRepository.findById(cartId);

            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();

                if (cart.getCourses() != null) {
                    cartCourses = cart.getCourses();
                    totalCourses = cartCourses.size();

                    // Calculamos el precio total
                    for (Course c : cartCourses) {
                        totalPrice += c.getPrice(); // O getPrecio() según lo tengas en Course.java
                    }
                }
            }
        }

        // 3. ¡LA CLAVE! Siempre le pasamos los datos al HTML, aunque sean 0.
        model.addAttribute("cartCourses", cartCourses);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalPrice", totalPrice);

        return "cart";
    }

    // Le ponemos EXACTAMENTE la misma ruta que tiene el action de tu formulario
    // HTML
    @PostMapping("/course/{id}/add-cart")
    public String addToCart(@PathVariable long id, HttpSession session) {

        // 1. Buscamos el curso en la base de datos por su ID
        Optional<Course> courseOpt = courseRepository.findById(id);

        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            Cart cart = null;

            // 2. Comprobamos si el usuario ya tiene un carrito guardado en esta sesión
            Long cartId = (Long) session.getAttribute("cartId");

            if (cartId != null) {
                cart = cartRepository.findById(cartId).orElse(null);
            }

            // 3. Si no hay carrito todavía, creamos uno nuevo y limpio
            if (cart == null) {
                cart = new Cart("Shopping cart", 0);
            }

            // 4. Añadimos el curso seleccionado al carrito
            cart.addCourse(course);

            // 5. Guardamos el carrito en la BD y nos guardamos su ID en la sesión
            cartRepository.save(cart);
            session.setAttribute("cartId", cart.getId());
        }

        // 6. Redirigimos al usuario de vuelta a la página de cursos
        return "redirect:/courses";
    }

    @PostMapping("/cart/remove/{id}")
    public String removeCourse(@PathVariable long id, HttpSession session) {

        // 1. Buscamos el ID del carrito en la sesión
        Long cartId = (Long) session.getAttribute("cartId");

        if (cartId != null) {
            Optional<Cart> cartOpt = cartRepository.findById(cartId);
            Optional<Course> courseOpt = courseRepository.findById(id);

            if (cartOpt.isPresent() && courseOpt.isPresent()) {
                Cart cart = cartOpt.get();
                Course course = courseOpt.get();

                // 2. Eliminamos el curso de la lista del carrito
                cart.getCourses().remove(course);

                // 3. Guardamos el carrito actualizado
                cartRepository.save(cart);
            }
        }

        // 4. Recargamos la página del carrito para ver el cambio
        return "redirect:/cart";
    }

    @PostMapping("/complete-purchase")
    public String completePurchase(HttpSession session, Model model) {

        Long cartId = (Long) session.getAttribute("cartId");
        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        List<Course> cartCourses = new ArrayList<>();
        Cart cart = null;

        if (cartId != null) {
            Optional<Cart> cartOpt = cartRepository.findById(cartId);
            if (cartOpt.isPresent()) {
                cart = cartOpt.get();
                if (cart.getCourses() != null) {
                    cartCourses = cart.getCourses();
                }
            }
        }

        if (cartCourses.isEmpty()) {
            return "course_purchase_error";
        }

        Optional<User> userOpt = userRepository.findById(loggedUser.getId());

        if (userOpt.isPresent()) {
            User userFromDb = userOpt.get();

            for (Course course : cartCourses) {
                if (!userFromDb.getPurchasedCourses().contains(course)) {
                    userFromDb.addPurchasedCourse(course);
                }
            }

            if (cart != null) {
                cart.getCourses().clear();
                cart.setPrice(0);
                cartRepository.save(cart);
            }

            userRepository.save(userFromDb);

            session.setAttribute("loggedUser", userFromDb);
        }

        return "completed_purchase";
    }
}
