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

import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.repository.CommentRepository;
import es.codeurjc.AcademiaElSoto.repository.CourseRepository;

@Controller
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CommentRepository commentRepository;

    /**
     * Displays all available courses.
     */
    @GetMapping("/courses")
    public String showCourses(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "courses";
    }

    /**
     * Displays the details of a single course.
     * It also loads the course comments ordered by publication date.
     */
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

    /**
     * Creates a new course from the admin panel.
     * If an image is uploaded, it is stored in the database as a blob.
     */
    @PostMapping("/admin/courses/new")
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

    /**
     * Displays course statistics in the admin panel.
     */
    @GetMapping("/admin/statistics")
    public String showAdminStatistics(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        return "admin/admin_statistics";
    }

    /**
     * Returns the image associated with a course.
     * If the course has no image, a 404 response is returned.
     */
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

    /**
     * Shows the admin edit page for a specific course.
     */
    @GetMapping("/admin/courses/{id}/edit")
    public String editCourse(Model model, @PathVariable long id) {
        Optional<Course> courseOptional = courseRepository.findById(id);

        if (courseOptional.isPresent()) {
            model.addAttribute("course", courseOptional.get());
            return "course_db/edit_course_page";
        }

        return "course_db/course_not_found";
    }

    /**
     * Processes the admin update of a course.
     * If a new image is uploaded, the old one is replaced.
     */
    @PostMapping("/admin/courses/{id}/edit")
    public String editCourseProcess(Model model,
            @PathVariable long id,
            Course editedCourse,
            @RequestParam(required = false) MultipartFile image) {

        Optional<Course> courseOptional = courseRepository.findById(id);

        if (courseOptional.isPresent()) {
            Course existingCourse = courseOptional.get();

            existingCourse.setCourseName(editedCourse.getCourseName());
            existingCourse.setTeacher(editedCourse.getTeacher());
            existingCourse.setPrice(editedCourse.getPrice());
            existingCourse.setDescription(editedCourse.getDescription());
            existingCourse.setStudents(editedCourse.getStudents());

            try {
                if (image != null && !image.isEmpty()) {
                    existingCourse.setImageFile(new SerialBlob(image.getBytes()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            courseRepository.save(existingCourse);
            model.addAttribute("course", existingCourse);
            return "course_db/edited_course";
        }

        return "course_db/course_not_found";
    }

    /**
     * Deletes a course from the admin panel.
     */
    @PostMapping("/admin/courses/{id}/delete")
    public String deleteCourse(Model model, @PathVariable long id) {
        Optional<Course> courseOptional = courseRepository.findById(id);

        if (courseOptional.isPresent()) {
            courseRepository.deleteById(id);
            return "course_db/deleted_course";
        }

        return "course_db/course_not_found";
    }

    /*
     * Old add-to-cart implementation kept as a comment.
     * This logic is currently handled inside CartController.
     *
     * @PostMapping("/course/{id}/add-cart")
     * public String addToCart(Model model, @PathVariable long id) {
     * Optional<Course> courseOptional = courseRepository.findById(id);
     * List<User> users = userRepository.findAll();
     *
     * if (courseOptional.isPresent() && !users.isEmpty()) {
     * Course course = courseOptional.get();
     * User user = users.get(0);
     *
     * Cart cart = user.getCart();
     * if (cart == null) {
     * cart = new Cart("Cart of " + user.getUserName(), 0);
     * cart.setUser(user);
     * user.setCart(cart);
     * }
     *
     * cart.addCourse(course);
     * userRepository.save(user);
     * return "redirect:/courses";
     * }
     *
     * return "course_db/course_not_found";
     * }
     */
}