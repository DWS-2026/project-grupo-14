package es.codeurjc.AcademiaElSoto.restcontroller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;

// A09: Using Slf4j for professional logging and monitoring
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.AcademiaElSoto.dto.CommentRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CommentResponseDto;
import es.codeurjc.AcademiaElSoto.mapper.CommentMapper;
import es.codeurjc.AcademiaElSoto.model.Comment;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.service.CommentService;
import es.codeurjc.AcademiaElSoto.service.CourseService;
import es.codeurjc.AcademiaElSoto.service.HtmlSanitizerService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.service.AuthorizationService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentRestController {

    // A09: Logger for security auditing and operational traceability
    private static final Logger log = LoggerFactory.getLogger(CommentRestController.class);

    @Autowired
    private CommentService commentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CommentMapper mapper;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private HtmlSanitizerService htmlSanitizerService;

    @GetMapping
    public Page<CommentResponseDto> getComments(Pageable pageable) {
        return commentService.findAllComments(pageable).map(this::toDTO);
    }

    @GetMapping("/{id}")
    public CommentResponseDto getCommentById(@PathVariable Long id) {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        return toDTO(comment);
    }

    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@Valid @RequestBody CommentRequestDto commentRequestDto,
            Authentication authentication) {

        User currentUser = authorizationService.getCurrentUser(authentication);

        Course course = courseService.findById(commentRequestDto.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Comment comment = toEntity(commentRequestDto);
        
        // A03: Sanitize input to prevent XSS in the API
        String cleanDescription = htmlSanitizerService.sanitize(comment.getDescription());
        
        // A08: Business Logic Integrity - Ensure comment is not empty or too long after sanitization
        if (cleanDescription.isBlank() || cleanDescription.length() > 500) {
            log.warn("API Integrity Check: User '{}' attempted to post an invalid comment.", currentUser.getUserName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid comment content");
        }

        comment.setDescription(cleanDescription);
        comment.setUser(currentUser.getUserName());
        comment.setPublicationDate(LocalDateTime.now());
        comment.setCourse(course);

        commentService.save(comment);

        // A09: Audit log for record creation
        log.info("API ACTION: New comment ID {} created by user '{}' on course ID {}.", 
                 comment.getId(), currentUser.getUserName(), course.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(comment.getId())
                .toUri();

        return ResponseEntity.created(location).body(toDTO(comment));
    }

    @PutMapping("/{id}")
    public CommentResponseDto updateComment(@PathVariable Long id,
            @Valid @RequestBody CommentRequestDto commentRequestDto,
            Authentication authentication) {

        try {
            // A01: Verify ownership (Broken Access Control)
            authorizationService.checkCommentAccess(id, authentication);
        } catch (ResponseStatusException e) {
            // A09: Logging unauthorized access attempt
            log.error("SECURITY ALERT: User '{}' attempted unauthorized update on comment ID: {}", 
                      authentication.getName(), id);
            throw e;
        }

        Comment existingComment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        Course course = courseService.findById(commentRequestDto.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        // A08: Integrity Check - Sanitize and validate update
        String cleanDescription = htmlSanitizerService.sanitize(commentRequestDto.getDescription());
        if (cleanDescription.isBlank() || cleanDescription.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid updated content");
        }

        existingComment.setDescription(cleanDescription);
        existingComment.setCourse(course);

        commentService.save(existingComment);
        
        // A09: Logging successful modification
        log.info("API ACTION: Comment ID {} successfully updated by user '{}'.", id, authentication.getName());

        return toDTO(existingComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, Authentication authentication) {

        try {
            // A01: Verify ownership or admin rights
            authorizationService.checkCommentAccess(id, authentication);
        } catch (ResponseStatusException e) {
            log.error("SECURITY ALERT: Unauthorized deletion attempt on comment ID {} by user '{}'.", 
                      id, authentication.getName());
            throw e;
        }

        Comment existingComment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        commentService.deleteById(existingComment.getId());
        
        // A09: Audit log for deletion
        log.warn("API ALERT: Comment ID {} was deleted by user '{}'.", id, authentication.getName());

        return ResponseEntity.noContent().build();
    }

    private CommentResponseDto toDTO(Comment comment) {
        return mapper.toDTO(comment);
    }

    private Comment toEntity(CommentRequestDto dto) {
        return mapper.toEntity(dto);
    }

    private Collection<CommentResponseDto> toDTOs(Collection<Comment> comments) {
        return mapper.toDTOs(comments);
    }
}