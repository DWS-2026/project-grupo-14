package es.codeurjc.AcademiaElSoto.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.model.Image;
import es.codeurjc.AcademiaElSoto.service.CommentService;
import es.codeurjc.AcademiaElSoto.service.CourseService;
import es.codeurjc.AcademiaElSoto.service.ImageService;
import es.codeurjc.AcademiaElSoto.service.HtmlSanitizerService;

@Controller
public class CourseController {

    private final CourseService courseService;
    private final CommentService commentService;
    private final ImageService imageService;
    private final HtmlSanitizerService htmlSanitizerService;

    public CourseController(CourseService courseService,
            CommentService commentService,
            ImageService imageService,
            HtmlSanitizerService htmlSanitizerService) {
        this.courseService = courseService;
        this.commentService = commentService;
        this.imageService = imageService;
        this.htmlSanitizerService = htmlSanitizerService;
    }

    @GetMapping("/courses")
    public String showCourses(Model model) {
        model.addAttribute("courses", courseService.findAll());
        return "courses";
    }

    @GetMapping("/course/{id}")
    public String showCourse(Model model, @PathVariable long id) {
        Optional<Course> courseOptional = courseService.findById(id);

        if (courseOptional.isPresent()) {
            Course course = courseOptional.get();

            model.addAttribute("course", course);
            model.addAttribute("hasImage", course.getImage() != null);
            model.addAttribute("comments", commentService.findByCourseId(id));

            return "course_db/show_course";
        }

        return "course_db/course_not_found";
    }

    @PostMapping("/admin/courses/new")
    public String newCourse(Model model,
            Course course,
            @RequestParam("image") MultipartFile imageFile) {

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                Image newImage = imageService.createImage(imageFile);
                course.setImage(newImage);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        course.setDescription(htmlSanitizerService.sanitize(course.getDescription()));
        courseService.save(course);

        return "course_db/saved_course";
    }

    @GetMapping("/admin/statistics")
    public String showAdminStatistics(Model model) {
        List<Course> courses = courseService.findAll();
        model.addAttribute("courses", courses);
        return "admin/admin_statistics";
    }

    @GetMapping("/course/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable long id) {
        try {
            Optional<Course> courseOptional = courseService.findById(id);

            if (courseOptional.isPresent()) {
                Course course = courseOptional.get();

                if (course.getImage() != null) {
                    Resource file = imageService.getImageFile(course.getImage().getId());

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                            .body(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/admin/courses/{id}/edit")
    public String editCourse(Model model, @PathVariable long id) {
        Optional<Course> courseOptional = courseService.findById(id);

        if (courseOptional.isPresent()) {
            model.addAttribute("course", courseOptional.get());
            return "course_db/edit_course_page";
        }

        return "course_db/course_not_found";
    }

    @PostMapping("/admin/courses/{id}/edit")
    public String editCourseProcess(Model model,
            @PathVariable long id,
            Course editedCourse,
            @RequestParam(name = "image", required = false) MultipartFile imageFile) {

        Optional<Course> courseOptional = courseService.findById(id);

        if (courseOptional.isEmpty()) {
            return "course_db/course_not_found";
        }

        Course existingCourse = courseOptional.get();

        existingCourse.setCourseName(editedCourse.getCourseName());
        existingCourse.setTeacher(editedCourse.getTeacher());
        existingCourse.setPrice(editedCourse.getPrice());
        existingCourse.setDescription(htmlSanitizerService.sanitize(editedCourse.getDescription()));
        existingCourse.setStudents(editedCourse.getStudents());

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                if (existingCourse.getImage() != null) {
                    imageService.replaceImageFile(existingCourse.getImage().getId(), imageFile);
                } else {
                    Image newImage = imageService.createImage(imageFile);
                    existingCourse.setImage(newImage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        courseService.save(existingCourse);

        model.addAttribute("course", existingCourse);
        return "course_db/edited_course";
    }

    @PostMapping("/admin/courses/{id}/delete")
    public String deleteCourse(Model model, @PathVariable long id) {
        Optional<Course> courseOptional = courseService.findById(id);

        if (courseOptional.isEmpty()) {
            return "course_db/course_not_found";
        }

        Course course = courseOptional.get();

        if (course.getImage() != null) {
            imageService.deleteImage(course.getImage().getId());
        }

        courseService.deleteById(id);

        return "course_db/deleted_course";
    }
}