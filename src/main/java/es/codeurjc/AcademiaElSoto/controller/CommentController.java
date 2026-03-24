package es.codeurjc.AcademiaElSoto.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
            String cleanUser = user == null ? "" : user.trim();
            String cleanDescription = description == null ? "" : description.trim();

            if (!cleanUser.isEmpty() && !cleanDescription.isEmpty() && cleanDescription.length() <= 500) {
                Course course = courseOptional.get();

                Comment comment = new Comment();
                comment.setUser(cleanUser);
                comment.setDescription(cleanDescription);
                comment.setCourse(course);
                comment.setPublicationDate(LocalDateTime.now());

                commentRepository.save(comment);
            }
        }

        return "redirect:/courses";
    }

    @GetMapping("/comments")
    public String showComments(Model model) {
        model.addAttribute("comments", commentRepository.findAll());
        return "admin_comment";
    }

    @GetMapping("/comment/{id}")
    public String showComment(Model model, @PathVariable long id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);

        if (commentOptional.isPresent()) {
            model.addAttribute("comment", commentOptional.get());
            return "comment_db/show_comment";
        }

        return "comment_db/comment_not_found";
    }

    @GetMapping("/edit-comment/{id}")
    public String editComment(Model model, @PathVariable long id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);

        if (commentOptional.isPresent()) {
            model.addAttribute("comment", commentOptional.get());
            return "comment_db/edit_comment_page";
        }

        return "comment_db/comment_not_found";
    }

    @PostMapping("/comment/{id}/delete")
    public String deleteComment(Model model, @PathVariable long id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);

        if (commentOptional.isPresent()) {
            commentRepository.deleteById(id);
            return "comment_db/deleted_comment";
        }

        return "comment_db/comment_not_found";
    }

    @PostMapping("/edited-comment/{id}")
    public String editCommentProcess(Model model,
                                     @PathVariable long id,
                                     Comment editedComment) {

        Optional<Comment> commentOptional = commentRepository.findById(id);

        if (commentOptional.isPresent()) {
            Comment originalComment = commentOptional.get();

            String cleanUser = editedComment.getUser() == null ? "" : editedComment.getUser().trim();
            String cleanDescription = editedComment.getDescription() == null ? "" : editedComment.getDescription().trim();

            if (cleanUser.isEmpty() || cleanDescription.isEmpty() || cleanDescription.length() > 500) {
                model.addAttribute("comment", originalComment);
                return "comment_db/edit_comment_page";
            }

            originalComment.setUser(cleanUser);
            originalComment.setDescription(cleanDescription);
            

            commentRepository.save(originalComment);
            model.addAttribute("comment", originalComment);
            return "comment_db/edited_comment";
        }

        return "comment_db/comment_not_found";
    }

    @GetMapping("/admin_comment")
    public String showAdminComments(Model model) {
        List<Comment> comments = commentRepository.findAll();
        model.addAttribute("comments", comments);
        return "admin_comment";
    }
}