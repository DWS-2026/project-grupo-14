package es.codeurjc.AcademiaElSoto.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.AcademiaElSoto.model.Comment;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.repository.CommentRepository;
import es.codeurjc.AcademiaElSoto.repository.CourseRepository;

@Controller
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping("/course/{id}/comment")
    public String saveComment(@PathVariable long id,
                              @RequestParam String user,
                              @RequestParam String description) {

        Optional<Course> courseOptional = courseRepository.findById(id);

        if (courseOptional.isPresent()) {
            Course course = courseOptional.get();

            Comment comment = new Comment();
            comment.setUser(user);
            comment.setDescription(description);
            comment.setCourse(course);
            comment.setPublicationDate(LocalDateTime.now());

            commentRepository.save(comment);
        }

        return "redirect:/courses";
    }
}
