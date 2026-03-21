package es.codeurjc.AcademiaElSoto.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String product;
    private int price;

    @OneToOne
    private User user;

    // We use ManyToMany so the same course can be in multiple carts.
    @ManyToMany
    private List<Course> courses = new ArrayList<>();

    public Cart() {
    }

    public Cart(String product, int price) {
        this.product = product;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    // Convenience method to add a course to the cart.
    public void addCourse(Course course) {
        this.courses.add(course);
        this.price += course.getPrice();
    }
}
