package es.codeurjc.AcademiaElSoto.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.repository.CartRepository;
import es.codeurjc.AcademiaElSoto.repository.CourseRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CourseRepository courseRepository;

    // Obtener carrito por ID
    public Optional<Cart> findById(Long id) {
        return cartRepository.findById(id);
    }

    // Crear un nuevo carrito vacío
    public Cart createCart() {
        Cart cart = new Cart("Shopping cart", 0);
        return cartRepository.save(cart);
    }

    // Obtener los cursos de un carrito (si es null devuelve lista vacía)
    public List<Course> getCourses(Cart cart) {
        if (cart != null && cart.getCourses() != null) {
            return cart.getCourses();
        } else {
            return new ArrayList<>();
        }
    }

    // Añadir un curso al carrito
    public void addCourse(Cart cart, Long courseId) {
        if (cart == null) return;

        Optional<Course> courseOpt = courseRepository.findById(courseId);
        courseOpt.ifPresent(course -> {
            cart.addCourse(course);
            cartRepository.save(cart);
        });
    }

    // Eliminar un curso del carrito
    public void removeCourse(Cart cart, Long courseId) {
        if (cart == null || cart.getCourses() == null) return;

        Optional<Course> courseOpt = courseRepository.findById(courseId);
        courseOpt.ifPresent(course -> {
            cart.getCourses().remove(course);
            cartRepository.save(cart);
        });
    }

    // Calcular total de cursos
    public int totalCourses(Cart cart) {
        if (cart != null && cart.getCourses() != null) {
            return cart.getCourses().size();
        }
        return 0;
    }

    // Calcular precio total del carrito
    public int totalPrice(Cart cart) {
        if (cart != null && cart.getCourses() != null) {
            return cart.getCourses().stream().mapToInt(Course::getPrice).sum();
        }
        return 0;
    }
}
