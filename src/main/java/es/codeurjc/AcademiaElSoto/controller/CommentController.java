package es.codeurjc.AcademiaElSoto.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.AcademiaElSoto.model.Comment;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.repository.CommentRepository;
import es.codeurjc.AcademiaElSoto.repository.CourseRepository;
import es.codeurjc.AcademiaElSoto.repository.UserRepository;

@Controller
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/course/{id}/comment")
    public String createComment(@PathVariable long id,
            @RequestParam String description,
            Authentication authentication) {

        Optional<Course> courseOpt = courseRepository.findById(id);

        if (courseOpt.isEmpty()) {
            return "course_db/course_not_found";
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        if (description == null || description.isBlank()) {
            return "redirect:/course/" + id;
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUserName(username);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        Course course = courseOpt.get();

        Comment comment = new Comment();
        comment.setUser(user.getUserName());
        comment.setDescription(description.trim());
        comment.setPublicationDate(LocalDateTime.now());
        comment.setCourse(course);

        commentRepository.save(comment);

        return "redirect:/courses";
    }

    @GetMapping("/profile/comments/{id}/edit")
    public String editOwnComment(Model model,
            @PathVariable long id,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Comment> commentOpt = commentRepository.findById(id);

        if (commentOpt.isEmpty()) {
            return "comment_db/comment_not_found";
        }

        Comment comment = commentOpt.get();
        String username = authentication.getName();

        if (!comment.getUser().equals(username)) {
            return "error/403";
        }

        model.addAttribute("comment", comment);
        return "comment_db/edit_own_comment_page";
    }

    @PostMapping("/profile/comments/{id}/edit")
    public String editOwnCommentProcess(Model model,
            @PathVariable long id,
            @RequestParam String description,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Comment> commentOpt = commentRepository.findById(id);

        if (commentOpt.isEmpty()) {
            return "comment_db/comment_not_found";
        }

        Comment comment = commentOpt.get();
        String username = authentication.getName();

        if (!comment.getUser().equals(username)) {
            return "error/403";
        }

        String cleanDescription = description == null ? "" : description.trim();

        if (cleanDescription.isEmpty() || cleanDescription.length() > 500) {
            model.addAttribute("comment", comment);
            return "comment_db/edit_own_comment_page";
        }

        comment.setDescription(cleanDescription);
        commentRepository.save(comment);

        return "redirect:/profile";
    }

    @PostMapping("/profile/comments/{id}/delete")
    public String deleteOwnComment(@PathVariable long id,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Comment> commentOpt = commentRepository.findById(id);

        if (commentOpt.isEmpty()) {
            return "comment_db/comment_not_found";
        }

        Comment comment = commentOpt.get();
        String username = authentication.getName();

        if (!comment.getUser().equals(username)) {
            return "error/403";
        }

        commentRepository.delete(comment);
        return "redirect:/profile";
    }

    @GetMapping("/admin/comments")
    public String showComments(Model model) {
        model.addAttribute("comments", commentRepository.findAll());
        return "admin/admin_comment";
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

    @GetMapping("/admin/comments/{id}/edit")
    public String editComment(Model model, @PathVariable long id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);

        if (commentOptional.isPresent()) {
            model.addAttribute("comment", commentOptional.get());
            return "comment_db/edit_comment_page";
        }

        return "comment_db/comment_not_found";
    }

    @PostMapping("/admin/comments/{id}/delete")
    public String deleteComment(@PathVariable long id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);

        if (commentOptional.isPresent()) {
            commentRepository.deleteById(id);
            return "comment_db/deleted_comment";
        }

        return "comment_db/comment_not_found";
    }

    @PostMapping("/admin/comments/{id}/edit")
    public String editCommentProcess(Model model,
            @PathVariable long id,
            Comment editedComment) {

        Optional<Comment> commentOptional = commentRepository.findById(id);

        if (commentOptional.isPresent()) {
            Comment originalComment = commentOptional.get();

            String cleanUser = editedComment.getUser() == null ? "" : editedComment.getUser().trim();
            String cleanDescription = editedComment.getDescription() == null ? ""
                    : editedComment.getDescription().trim();

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
}