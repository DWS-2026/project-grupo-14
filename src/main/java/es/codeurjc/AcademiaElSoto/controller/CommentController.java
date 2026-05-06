package es.codeurjc.AcademiaElSoto.controller;

import java.time.LocalDateTime;
import java.util.Optional;

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
import es.codeurjc.AcademiaElSoto.service.CommentService;
import es.codeurjc.AcademiaElSoto.service.CourseService;
import es.codeurjc.AcademiaElSoto.service.HtmlSanitizerService;
import es.codeurjc.AcademiaElSoto.service.UserService;

@Controller
public class CommentController {

    private final CommentService commentService;
    private final CourseService courseService;
    private final UserService userService;
    private final HtmlSanitizerService htmlSanitizerService;

    public CommentController(CommentService commentService,
            CourseService courseService,
            UserService userService,
            HtmlSanitizerService htmlSanitizerService) {
        this.commentService = commentService;
        this.courseService = courseService;
        this.userService = userService;
        this.htmlSanitizerService = htmlSanitizerService;
    }

    @PostMapping("/course/{id}/comment")
    public String createComment(@PathVariable long id,
            @RequestParam String description,
            Authentication authentication) {

        Optional<Course> courseOpt = courseService.findById(id);

        if (courseOpt.isEmpty()) {
            return "course_db/course_not_found";
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        if (description == null || description.isBlank()) {
            return "redirect:/course/" + id;
        }

        Optional<User> userOpt = userService.findByUserName(authentication.getName());

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        Course course = courseOpt.get();

        Comment comment = new Comment();
        comment.setUser(user.getUserName());
        String cleanDescription = htmlSanitizerService.sanitize(description);

        if (cleanDescription.isBlank() || cleanDescription.length() > 500) {
            return "redirect:/course/" + id;
        }

        comment.setDescription(cleanDescription);
        comment.setPublicationDate(LocalDateTime.now());
        comment.setCourse(course);

        commentService.save(comment);

        return "redirect:/courses";
    }

    @GetMapping("/profile/comments/{id}/edit")
    public String editOwnComment(Model model,
            @PathVariable long id,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Comment> commentOpt = commentService.findById(id);

        if (commentOpt.isEmpty()) {
            return "comment_db/comment_not_found";
        }

        Comment comment = commentOpt.get();

        if (!comment.getUser().equals(authentication.getName())) {
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

        Optional<Comment> commentOpt = commentService.findById(id);

        if (commentOpt.isEmpty()) {
            return "comment_db/comment_not_found";
        }

        Comment comment = commentOpt.get();

        if (!comment.getUser().equals(authentication.getName())) {
            return "error/403";
        }

        String cleanDescription = htmlSanitizerService.sanitize(description);

        if (cleanDescription.isBlank() || cleanDescription.length() > 500) {
            model.addAttribute("comment", comment);
            return "comment_db/edit_own_comment_page";
        }

        comment.setDescription(cleanDescription);

        if (cleanDescription.isEmpty() || cleanDescription.length() > 500) {
            model.addAttribute("comment", comment);
            return "comment_db/edit_own_comment_page";
        }

        comment.setDescription(cleanDescription);
        commentService.save(comment);

        return "redirect:/profile";
    }

    @PostMapping("/profile/comments/{id}/delete")
    public String deleteOwnComment(@PathVariable long id,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Comment> commentOpt = commentService.findById(id);

        if (commentOpt.isEmpty()) {
            return "comment_db/comment_not_found";
        }

        Comment comment = commentOpt.get();

        if (!comment.getUser().equals(authentication.getName())) {
            return "error/403";
        }

        commentService.deleteById(id);

        return "redirect:/profile";
    }

    @GetMapping("/admin/comments")
    public String showComments(Model model) {
        model.addAttribute("comments", commentService.findAllComments());
        return "admin/admin_comment";
    }

    @GetMapping("/comment/{id}")
    public String showComment(Model model, @PathVariable long id) {
        Optional<Comment> commentOptional = commentService.findById(id);

        if (commentOptional.isPresent()) {
            model.addAttribute("comment", commentOptional.get());
            return "comment_db/show_comment";
        }

        return "comment_db/comment_not_found";
    }

    @GetMapping("/admin/comments/{id}/edit")
    public String editComment(Model model, @PathVariable long id) {
        Optional<Comment> commentOptional = commentService.findById(id);

        if (commentOptional.isPresent()) {
            model.addAttribute("comment", commentOptional.get());
            return "comment_db/edit_comment_page";
        }

        return "comment_db/comment_not_found";
    }

    @PostMapping("/admin/comments/{id}/delete")
    public String deleteComment(@PathVariable long id) {
        boolean deleted = commentService.deleteById(id);

        if (deleted) {
            return "comment_db/deleted_comment";
        }

        return "comment_db/comment_not_found";
    }

    @PostMapping("/admin/comments/{id}/edit")
    public String editCommentProcess(Model model,
            @PathVariable long id,
            Comment editedComment) {

        Optional<Comment> commentOptional = commentService.findById(id);

        if (commentOptional.isEmpty()) {
            return "comment_db/comment_not_found";
        }

        Comment comment = commentOptional.get();
        comment.setUser(editedComment.getUser());
        comment.setDescription(editedComment.getDescription());

        commentService.save(comment);

        model.addAttribute("comment", comment);
        return "comment_db/edited_comment";
    }
}