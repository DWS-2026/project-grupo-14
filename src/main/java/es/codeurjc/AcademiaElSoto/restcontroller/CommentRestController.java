package es.codeurjc.AcademiaElSoto.restcontroller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.AcademiaElSoto.dto.CommentRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CommentResponseDto;
import es.codeurjc.AcademiaElSoto.model.Comment;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.service.CommentService;
import es.codeurjc.AcademiaElSoto.service.CourseService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentRestController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CourseService courseService;

    @GetMapping
    public List<CommentResponseDto> getComments() {
        return commentService.findAllComments()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public CommentResponseDto getCommentById(@PathVariable Long id) {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        return toDto(comment);
    }

    @GetMapping("/course/{courseId}")
    public List<CommentResponseDto> getCommentsByCourse(@PathVariable Long courseId) {
        return commentService.findByCourseId(courseId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@Valid @RequestBody CommentRequestDto commentRequestDto) {

        Course course = courseService.findById(commentRequestDto.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Comment comment = new Comment();
        comment.setDescription(commentRequestDto.getDescription().trim());
        comment.setUser(commentRequestDto.getUser().trim());
        comment.setPublicationDate(LocalDateTime.now());
        comment.setCourse(course);

        Comment savedComment = commentService.save(comment);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedComment.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(toDto(savedComment));
    }

    @PutMapping("/{id}")
    public CommentResponseDto updateComment(@PathVariable Long id,
            @Valid @RequestBody CommentRequestDto commentRequestDto) {

        Comment existingComment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        Course course = courseService.findById(commentRequestDto.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        existingComment.setDescription(commentRequestDto.getDescription().trim());
        existingComment.setUser(commentRequestDto.getUser().trim());
        existingComment.setCourse(course);

        Comment updatedComment = commentService.save(existingComment);

        return toDto(updatedComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {

        Comment existingComment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        commentService.deleteById(existingComment.getId());

        return ResponseEntity.noContent().build();
    }

    private CommentResponseDto toDto(Comment comment) {
        Long courseId = null;
        String courseName = null;

        if (comment.getCourse() != null) {
            courseId = comment.getCourse().getId();
            courseName = comment.getCourse().getCourseName();
        }

        return new CommentResponseDto(
                comment.getId(),
                comment.getDescription(),
                comment.getUser(),
                comment.getFormattedDate(),
                courseId,
                courseName);
    }
}