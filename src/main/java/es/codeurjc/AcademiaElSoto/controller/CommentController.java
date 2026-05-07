package es.codeurjc.AcademiaElSoto.controller;

import java.time.LocalDateTime;
import java.util.Optional;

// SLF4J Logger for security auditing and operational monitoring
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // Logger initialization for audit trails
    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

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
            log.warn("Comment creation failed: Course ID {} not found.", id);
            return "course_db/course_not_found";
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Basic input validation to prevent empty submissions
        if (description == null || description.isBlank()) {
            return "redirect:/course/" + id;
        }

        Optional<User> userOpt = userService.findByUserName(authentication.getName());

        if (userOpt.isEmpty()) {
            log.error("Authentication inconsistency: User '{}' not found in database.", authentication.getName());
            return "redirect:/login";
        }

        User user = userOpt.get();
        Course course = courseOpt.get();

        Comment comment = new Comment();
        comment.setUser(user.getUserName());
        
        // A03: Content sanitization to prevent Cross-Site Scripting (XSS)
        String cleanDescription = htmlSanitizerService.sanitize(description);

        // A08: Data integrity check for content length and validity
        if (cleanDescription.isBlank() || cleanDescription.length() > 500) {
            log.warn("Comment blocked: Sanitization resulted in empty string or exceeded length limits.");
            return "redirect:/course/" + id;
        }

        comment.setDescription(cleanDescription);
        comment.setPublicationDate(LocalDateTime.now());
        comment.setCourse(course);

        commentService.save(comment);
        log.info("USER ACTION: New comment posted by '{}' on course ID {}.", user.getUserName(), id);

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

        // A01: Broken Access Control check - Verify ownership before allowing edit access
        if (!comment.getUser().equals(authentication.getName())) {
            log.error("SECURITY ALERT: User '{}' attempted to edit someone else's comment (ID: {}).", authentication.getName(), id);
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

        // A01: Verify ownership before processing the update
        if (!comment.getUser().equals(authentication.getName())) {
            log.error("SECURITY ALERT: Unauthorized update attempt on comment ID {} by user '{}'.", id, authentication.getName());
            return "error/403";
        }

        // A03: Sanitize the updated content
        String cleanDescription = htmlSanitizerService.sanitize(description);

        if (cleanDescription.isBlank() || cleanDescription.length() > 500) {
            model.addAttribute("comment", comment);
            return "comment_db/edit_own_comment_page";
        }

        comment.setDescription(cleanDescription);
        commentService.save(comment);
        
        log.info("USER ACTION: Comment ID {} updated by owner '{}'.", id, authentication.getName());

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

        // A01: Verify ownership before deletion
        if (!comment.getUser().equals(authentication.getName())) {
            log.error("SECURITY ALERT: Unauthorized deletion attempt on comment ID {} by user '{}'.", id, authentication.getName());
            return "error/403";
        }

        commentService.deleteById(id);
        log.info("USER ACTION: Comment ID {} deleted by owner '{}'.", id, authentication.getName());

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
            log.warn("ADMIN ACTION: Comment ID {} has been deleted by an administrator.", id);
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
        
        // Manual mapping for integrity and sanitizing admin input just in case
        comment.setUser(editedComment.getUser());
        comment.setDescription(htmlSanitizerService.sanitize(editedComment.getDescription()));

        commentService.save(comment);
        
        log.info("ADMIN ACTION: Comment ID {} successfully modified by an administrator.", id);

        model.addAttribute("comment", comment);
        return "comment_db/edited_comment";
    }
}