package es.codeurjc.AcademiaElSoto.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import es.codeurjc.AcademiaElSoto.dto.CartResponseDto;
import es.codeurjc.AcademiaElSoto.dto.CourseResponseDto;
import es.codeurjc.AcademiaElSoto.mapper.CartMapper;
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

    @Autowired
    private CartMapper cartMapper;

    // 1. GET THE CART PAGINATED
    @GetMapping
    public Page<CartResponseDto> getCarts(Pageable pageable) {
        return cartService.findAll(pageable).map(this::toDTO);
    }

    // 2. ADD A COURSE TO THE CART
    @PostMapping("/{cartId}/courses/{courseId}")
    public ResponseEntity<CartResponseDto> addCourseToCart(@PathVariable Long cartId, @PathVariable Long courseId) {
        
        Optional<Cart> cartOpt = cartService.findById(cartId);
        Optional<Course> courseOpt = courseService.findById(courseId);

        if (cartOpt.isPresent() && courseOpt.isPresent()) {
            Cart cart = cartOpt.get();
            Course course = courseOpt.get();

            cart.getCourses().add(course);
            cartService.save(cart);

            return ResponseEntity.ok(toDTO(cart));
        }

        return ResponseEntity.notFound().build(); 
    }

    // 3. REMOVE A COURSE FROM THE CART
    @DeleteMapping("/{cartId}/courses/{courseId}")
    public ResponseEntity<CartResponseDto> removeCourseFromCart(@PathVariable Long cartId, @PathVariable Long courseId) {
        
        Optional<Cart> cartOpt = cartService.findById(cartId);
        Optional<Course> courseOpt = courseService.findById(courseId);

        if (cartOpt.isPresent() && courseOpt.isPresent()) {
            Cart cart = cartOpt.get();
            Course course = courseOpt.get();

            cart.getCourses().remove(course);
            cartService.save(cart);

            return ResponseEntity.ok(toDTO(cart));
        }

        return ResponseEntity.notFound().build();
    }
    
    // 4. CLEAR THE CART COMPLETELY
    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<CartResponseDto> clearCart(@PathVariable Long cartId) {
        
        Optional<Cart> cartOpt = cartService.findById(cartId);

        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            
            cart.getCourses().clear();
            cartService.save(cart);
            
            return ResponseEntity.ok(toDTO(cart));
        }

        return ResponseEntity.notFound().build();
    }

    
    private CartResponseDto toDTO(Cart cart) {
        return cartMapper.toDTO(cart);
    }
}