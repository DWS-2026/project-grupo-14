package es.codeurjc.AcademiaElSoto.restcontroller;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CartRestController.class);

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

    // 1. GET THE CART PAGINATED
    @GetMapping
    public Page<CartResponseDto> getCarts(Pageable pageable, Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            logger.warn("Access denied for user '{}' attempting to list all carts", authentication.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can list all carts");
        }

        return cartService.findAll(pageable).map(this::toDTO);
    }

    @GetMapping("/me")
    public CartResponseDto getMyCart(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = getOrCreateUserCart(user);

        return toDTO(cart);
    }

    // 2. ADD A COURSE TO THE CART
    @PostMapping("/me/courses/{courseId}")
    public ResponseEntity<CartResponseDto> addCourseToMyCart(
            @PathVariable Long courseId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        Cart cart = getOrCreateUserCart(user);

        Course course = courseService.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (cart.getCourses().contains(course)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course is already in the cart");
        }

        cart.addCourse(course);

        Cart savedCart = cartService.save(cart);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(location).body(toDTO(savedCart));
    }

    // 3. REMOVE A COURSE FROM THE CART
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

        return ResponseEntity.ok(toDTO(savedCart));
    }

    // 4. CLEAR THE CART COMPLETELY
    @DeleteMapping("/me/courses")
    public ResponseEntity<CartResponseDto> clearMyCart(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = getOrCreateUserCart(user);

        cart.getCourses().clear();
        cart.setPrice(0);

        Cart savedCart = cartService.save(cart);

        return ResponseEntity.ok(toDTO(savedCart));
    }

    @GetMapping("/{cartId}")
    public CartResponseDto getCartById(
            @PathVariable Long cartId,
            Authentication authentication) {

        Cart cart = cartService.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        try {
            // Security: Check if user owns the cart or is admin
            checkCartAccess(cart, authentication);
        } catch (ResponseStatusException e) {
            // Security Reporting
            logger.warn("Access denied for user '{}' on cart ID: {}", authentication.getName(), cartId);
            throw e;
        }

        return toDTO(cart);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Map<String, String>> deleteCart(
            @PathVariable Long cartId,
            Authentication authentication) {

        if (!authorizationService.isAdmin(authentication)) {
            logger.warn("Access denied for user '{}' attempting to delete cart ID: {}", authentication.getName(), cartId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can delete carts");
        }

        Cart cart = cartService.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        cartService.deleteById(cart.getId());

        return ResponseEntity.ok(Map.of("message", "Cart deleted successfully"));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return userService.findByUserName(authentication.getName())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private Cart getOrCreateUserCart(User user) {
        Cart cart = user.getCart();

        if (cart == null) {
            cart = new Cart("Cart of " + user.getUserName(), 0);
            user.setCart(cart);
            userService.saveUser(user);
        }

        return cart;
    }

    private void checkCartAccess(Cart cart, Authentication authentication) {
        if (authorizationService.isAdmin(authentication)) {
            return;
        }

        User user = getAuthenticatedUser(authentication);

        if (cart.getUser() == null || !cart.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own cart");
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