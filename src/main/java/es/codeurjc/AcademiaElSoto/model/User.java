package es.codeurjc.AcademiaElSoto.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.sql.Blob;
import jakarta.persistence.Lob;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;

@Component
@SessionScope
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String info;
    private String userName;
    private String email;
    private String password;
    private String lastName;
    @Lob
    private Blob profileImage;

    @OneToOne(cascade = CascadeType.ALL)
    private Cart cart;

    @ManyToMany
    private List<Course> purchasedCourses = new ArrayList<>();

    private String encodedPassword;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    public User() {
    }

    public User(String userName, String lastName, String email, String password,
            String... roles) {

        this.userName = userName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;

        this.roles = List.of(roles);
    }

    public Long getId() {
        return id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Blob getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Blob profileImage) {
        this.profileImage = profileImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<Course> getPurchasedCourses() {
        return purchasedCourses;
    }

    public void setPurchasedCourses(List<Course> purchasedCourses) {
        this.purchasedCourses = purchasedCourses;
    }

    public void addPurchasedCourse(Course course) {
        this.purchasedCourses.add(course);
    }
}