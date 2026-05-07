package es.codeurjc.AcademiaElSoto.restcontroller;

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.AcademiaElSoto.dto.CartResponseDto;
import es.codeurjc.AcademiaElSoto.mapper.CartMapper;
import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.service.AuthorizationService;
import es.codeurjc.AcademiaElSoto.service.CartService;
import es.codeurjc.AcademiaElSoto.service.CourseService;
import es.codeurjc.AcademiaElSoto.service.UserService;

@RestController
@RequestMapping("/api/v1/carts")
public class CartRestController {

    // A09: Using professional logging for security audit trails
    private static final Logger logger = LoggerFactory.getLogger(CartRestController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    public Page<CartResponseDto> getCarts(Pageable pageable, Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            logger.warn("UNAUTHORIZED ACCESS: User '{}' attempted to list all carts.", authentication.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrative privileges required");
        }
        return cartService.findAll(pageable).map(this::toDTO);
    }

    @GetMapping("/me")
    public CartResponseDto getMyCart(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = getOrCreateUserCart(user);
        return toDTO(cart);
    }

    @PostMapping("/me/courses/{courseId}")
    public ResponseEntity<CartResponseDto> addCourseToMyCart(
            @PathVariable Long courseId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        Cart cart = getOrCreateUserCart(user);

        Course course = courseService.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        // A08: Data Integrity - Check if the user already purchased the course
        if (user.getPurchasedCourses().contains(course)) {
            logger.info("Integrity Check: User '{}' tried to re-add purchased course ID: {}", user.getUserName(), courseId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course already purchased");
        }

        // A08: Data Integrity - Check for course capacity
        if (course.getStudents() <= 0) {
            logger.warn("Integrity Check: User '{}' tried to add a full course ID: {}", user.getUserName(), courseId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course is full");
        }

        if (cart.getCourses().contains(course)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course is already in the cart");
        }

        cart.addCourse(course);
        
        // A08: Consistency - Ensure price is recalculated upon addition
        recalculateCartPrice(cart);

        Cart savedCart = cartService.save(cart);
        
        // A09: Logging the modification for auditing purposes
        logger.info("CART UPDATE: User '{}' added course '{}' to cart.", user.getUserName(), course.getCourseName());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(location).body(toDTO(savedCart));
    }

    @DeleteMapping("/me/courses/{courseId}")
    public ResponseEntity<CartResponseDto> removeCourseFromMyCart(
            @PathVariable Long courseId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        Cart cart = getOrCreateUserCart(user);

        Course course = courseService.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        boolean removed = cart.getCourses().remove(course);

        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course is not in the cart");
        }

        recalculateCartPrice(cart);
        Cart savedCart = cartService.save(cart);

        // A09: Logging removal
        logger.info("CART UPDATE: User '{}' removed course ID {} from cart.", user.getUserName(), courseId);

        return ResponseEntity.ok(toDTO(savedCart));
    }

    @DeleteMapping("/me/courses")
    public ResponseEntity<CartResponseDto> clearMyCart(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = getOrCreateUserCart(user);

        cart.getCourses().clear();
        cart.setPrice(0);

        Cart savedCart = cartService.save(cart);

        // A09: Logging significant state change
        logger.warn("CART ALERT: User '{}' cleared their cart completely.", user.getUserName());

        return ResponseEntity.ok(toDTO(savedCart));
    }

    @GetMapping("/{cartId}")
    public CartResponseDto getCartById(
            @PathVariable Long cartId,
            Authentication authentication) {

        Cart cart = cartService.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        checkCartAccess(cart, authentication);

        return toDTO(cart);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Map<String, String>> deleteCart(
            @PathVariable Long cartId,
            Authentication authentication) {

        if (!authorizationService.isAdmin(authentication)) {
            logger.warn("SECURITY ALERT: Unauthorized cart deletion attempt by user '{}' on cart ID: {}", authentication.getName(), cartId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can delete carts");
        }

        Cart cart = cartService.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        cartService.deleteById(cart.getId());
        logger.info("ADMIN ACTION: Cart ID {} deleted by admin '{}'.", cartId, authentication.getName());

        return ResponseEntity.ok(Map.of("message", "Cart deleted successfully"));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return userService.findByUserName(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Cart getOrCreateUserCart(User user) {
        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart("Cart of " + user.getUserName(), 0);
            user.setCart(cart);
            userService.saveUser(user);
            logger.info("New cart initialized for user: {}", user.getUserName());
        }
        return cart;
    }

    private void checkCartAccess(Cart cart, Authentication authentication) {
        if (authorizationService.isAdmin(authentication)) {
            return;
        }

        User user = getAuthenticatedUser(authentication);

        // A01: Broken Access Control - Ensuring users can't snoop on other carts
        if (cart.getUser() == null || !cart.getUser().getId().equals(user.getId())) {
            logger.error("SECURITY BREACH ATTEMPT: User '{}' tried to access Cart ID: {}", user.getUserName(), cart.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to requested cart");
        }
    }

    private void recalculateCartPrice(Cart cart) {
        int totalPrice = cart.getCourses().stream()
                .mapToInt(Course::getPrice)
                .sum();
        cart.setPrice(totalPrice);
    }

    private CartResponseDto toDTO(Cart cart) {
        return cartMapper.toDTO(cart);
    }
}