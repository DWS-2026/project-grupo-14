package es.codeurjc.AcademiaElSoto.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.service.CartService;
import es.codeurjc.AcademiaElSoto.service.CourseService;

@RestController
@RequestMapping("/api/carts")
public class CartRestController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CourseService courseService;

    // 1. GET THE CART: GET /api/carts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Cart> getCart(@PathVariable Long id) {
        Optional<Cart> cartOpt = cartService.findById(id);
        
        if (cartOpt.isPresent()) {
            return ResponseEntity.ok(cartOpt.get()); // 200 OK with the cart JSON
        } else {
            return ResponseEntity.notFound().build(); // 404 Error if the cart does not exist
        }
    }

    // 2. ADD A COURSE TO THE CART: POST /api/carts/{cartId}/courses/{courseId}
    @PostMapping("/{cartId}/courses/{courseId}")
    public ResponseEntity<Cart> addCourseToCart(@PathVariable Long cartId, @PathVariable Long courseId) {
        
        Optional<Cart> cartOpt = cartService.findById(cartId);
        Optional<Course> courseOpt = courseService.findById(courseId);

        if (cartOpt.isPresent() && courseOpt.isPresent()) {
            Cart cart = cartOpt.get();
            Course course = courseOpt.get();

            cart.getCourses().add(course);
            cartService.save(cart); // Save the update to the DB

            return ResponseEntity.ok(cart);
        }

        // If the cart or course does not exist, return 404 Not Found
        return ResponseEntity.notFound().build(); 
    }

    // 3. REMOVE A COURSE FROM THE CART: DELETE /api/carts/{cartId}/courses/{courseId}
    @DeleteMapping("/{cartId}/courses/{courseId}")
    public ResponseEntity<Cart> removeCourseFromCart(@PathVariable Long cartId, @PathVariable Long courseId) {
        
        Optional<Cart> cartOpt = cartService.findById(cartId);
        Optional<Course> courseOpt = courseService.findById(courseId);

        if (cartOpt.isPresent() && courseOpt.isPresent()) {
            Cart cart = cartOpt.get();
            Course course = courseOpt.get();

            // Remove the course from the cart's list
            cart.getCourses().remove(course);
            cartService.save(cart); // Save the changes to the DB

            return ResponseEntity.ok(cart);
        }

        return ResponseEntity.notFound().build();
    }
    
    // 4. CLEAR THE CART COMPLETELY: DELETE /api/carts/{cartId}/clear
    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<Cart> clearCart(@PathVariable Long cartId) {
        
        Optional<Cart> cartOpt = cartService.findById(cartId);

        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            
            // Empty the course list
            cart.getCourses().clear();
            cartService.save(cart);
            
            return ResponseEntity.ok(cart);
        }

        return ResponseEntity.notFound().build();
    }
}