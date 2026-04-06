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

/**
 * Service layer for cart-related operations.
 * This class centralizes cart business logic such as
 * creating carts, adding/removing courses, and calculating totals.
 */
@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Finds a cart by its id.
     *
     * @param id cart identifier
     * @return an Optional containing the cart if found
     */
    public Optional<Cart> findById(Long id) {
        return cartRepository.findById(id);
    }

    /**
     * Creates and saves a new empty cart.
     *
     * @return the saved cart
     */
    public Cart createCart() {
        Cart cart = new Cart("Shopping cart", 0);
        return cartRepository.save(cart);
    }

    /**
     * Returns the list of courses in a cart.
     * If the cart or its course list is null, it returns an empty list.
     *
     * @param cart the cart to inspect
     * @return the list of courses in the cart, or an empty list
     */
    public List<Course> getCourses(Cart cart) {
        if (cart != null && cart.getCourses() != null) {
            return cart.getCourses();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Adds a course to the given cart if both the cart exists
     * and the course id is found in the database.
     *
     * @param cart the target cart
     * @param courseId the id of the course to add
     */
    public void addCourse(Cart cart, Long courseId) {
        if (cart == null) return;

        Optional<Course> courseOpt = courseRepository.findById(courseId);
        courseOpt.ifPresent(course -> {
            cart.addCourse(course);
            cartRepository.save(cart);
        });
    }

    /**
     * Removes a course from the given cart if both the cart exists
     * and the course id is found in the database.
     *
     * @param cart the target cart
     * @param courseId the id of the course to remove
     */
    public void removeCourse(Cart cart, Long courseId) {
        if (cart == null || cart.getCourses() == null) return;

        Optional<Course> courseOpt = courseRepository.findById(courseId);
        courseOpt.ifPresent(course -> {
            cart.getCourses().remove(course);
            cartRepository.save(cart);
        });
    }

    /**
     * Returns the number of courses stored in the cart.
     *
     * @param cart the cart to inspect
     * @return total number of courses, or 0 if the cart is null
     */
    public int totalCourses(Cart cart) {
        if (cart != null && cart.getCourses() != null) {
            return cart.getCourses().size();
        }
        return 0;
    }

    /**
     * Calculates the total price of all courses in the cart.
     *
     * @param cart the cart to inspect
     * @return total price, or 0 if the cart is null
     */
    public int totalPrice(Cart cart) {
        if (cart != null && cart.getCourses() != null) {
            return cart.getCourses().stream().mapToInt(Course::getPrice).sum();
        }
        return 0;
    }
}