package es.codeurjc.AcademiaElSoto.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import java.util.List;

import es.codeurjc.AcademiaElSoto.dto.CartResponseDto;
import es.codeurjc.AcademiaElSoto.dto.CourseResponseDto;
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

    // 1. GET THE CART
    @GetMapping("/{id}")
    public ResponseEntity<CartResponseDto> getCart(@PathVariable Long id) {
        Optional<Cart> cartOpt = cartService.findById(id);
        
        if (cartOpt.isPresent()) {
            return ResponseEntity.ok(toDTO(cartOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
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

    // MAPEO MANUAL: Sustituimos el Mapper por esta lógica simple
   private CartResponseDto toDTO(Cart cart) {
    CartResponseDto dto = new CartResponseDto();
    
    // 1. Datos básicos del carrito
    dto.setId(cart.getId());
    dto.setProduct("Carrito de compra"); // O el nombre que quieras darle
    
    // 2. Nombre del usuario (asumiendo que Cart tiene relación con User)
    if (cart.getUser() != null) {
        dto.setUserName(cart.getUser().getUserName());
    }

    // 3. Convertir la lista de Course a List<CourseResponseDto>
    if (cart.getCourses() != null) {
        List<CourseResponseDto> courseDtos = cart.getCourses().stream()
            .map(course -> {
                // Creamos el DTO de cada curso a mano
                CourseResponseDto cDto = new CourseResponseDto();
                cDto.setId(course.getId());
                cDto.setCourseName(course.getCourseName());
                cDto.setPrice(course.getPrice()); // Asegúrate de que estos métodos existan en Course
                return cDto;
            })
            .toList();
            
        dto.setCourses(courseDtos);

        // 4. Calcular el precio total sumando los precios de los cursos
        int total = courseDtos.stream().mapToInt(c -> c.getPrice()).sum();
        dto.setPrice(total);
    }
    
    return dto;
}
}