package es.codeurjc.AcademiaElSoto.controller;

import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.repository.CartRepository;
import es.codeurjc.AcademiaElSoto.repository.CommentRepository;
import es.codeurjc.AcademiaElSoto.repository.CourseRepository;
import es.codeurjc.AcademiaElSoto.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Controller
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CommentRepository commentRepository;

    // Temporary data to verify that everything works.
    @PostConstruct
    public void init() {
    }

    @GetMapping("/courses")
    public String showCourses(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "courses";
    }

    @GetMapping("/course/{id}")
    public String showCourse(Model model, @PathVariable long id) {
        Optional<Course> courseOptional = courseRepository.findById(id);

        if (courseOptional.isPresent()) {
            Course course = courseOptional.get();
            model.addAttribute("course", course);
            model.addAttribute("hasImage", course.getImageFile() != null);
            model.addAttribute("comments", commentRepository.findByCourseIdOrderByPublicationDateDesc(id));
            return "course_db/show_course";
        }

        return "course_db/course_not_found";
    }

    @PostMapping("/course/new")
    public String newCourse(Model model, Course course, @RequestParam MultipartFile image) {
        try {
            if (!image.isEmpty()) {
                course.setImageFile(new SerialBlob(image.getBytes()));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        courseRepository.save(course);
        return "course_db/saved_course";
    }

    @GetMapping("/admin_statistics")
    public String showAdminStatistics(Model model) {
        // Retrieve all courses from the database.
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        return "admin_statistics";
    }

    @GetMapping("/course/{id}/image")
    public ResponseEntity<Object> getImage(@PathVariable long id) throws Exception {
        Course course = courseRepository.findById(id).orElseThrow();

        if (course.getImageFile() != null) {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(course.getImageFile().getBinaryStream()));
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/edit-course/{id}")
    public String editCourse(Model model, @PathVariable long id) {
        Optional<Course> courseOptional = courseRepository.findById(id);

        if (courseOptional.isPresent()) {
            model.addAttribute("course", courseOptional.get());
            return "course_db/edit_course_page";
        }

        return "course_db/course_not_found";
    }

    @PostMapping("/edited-course/{id}")
    public String editCourseProcess(Model model, @PathVariable long id, Course editedCourse) {
        Optional<Course> courseOptional = courseRepository.findById(editedCourse.getId());

        if (courseOptional.isPresent()) {
            editedCourse.setId(id);
            courseRepository.save(editedCourse);
            model.addAttribute("course", editedCourse);
            return "course_db/edited_course";
        }

        return "course_db/course_not_found";
    }

    @PostMapping("/course/{id}/delete")
    public String deleteCourse(Model model, @PathVariable long id) {
        Optional<Course> courseOptional = courseRepository.findById(id);

        if (courseOptional.isPresent()) {
            courseRepository.deleteById(id);
            return "course_db/deleted_course";
        }

        return "course_db/course_not_found";
    }

    @PostMapping("/course/{id}/add-cart")
    public String addToCart(Model model, @PathVariable long id) {
        Optional<Course> courseOptional = courseRepository.findById(id);
        List<User> users = userRepository.findAll();

        if (courseOptional.isPresent() && !users.isEmpty()) {
            Course course = courseOptional.get();
            User user = users.get(0);

            Cart cart = user.getCart();
            if (cart == null) {
                cart = new Cart("Cart of " + user.getUserName(), 0);
                cart.setUser(user);
                user.setCart(cart);
            }

            cart.addCourse(course);
            userRepository.save(user);
            return "redirect:/courses";
        }

        return "course_db/course_not_found";
    }

    
    
}
