package es.codeurjc.AcademiaElSoto.restcontroller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.AcademiaElSoto.dto.CommentRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CommentResponseDto;
import es.codeurjc.AcademiaElSoto.mapper.CommentMapper;
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

    @Autowired
    private CommentMapper mapper;

    @GetMapping
    public Collection<CommentResponseDto> getComments() {
        return toDTOs(commentService.findAllComments());
    }

    @GetMapping("/{id}")
    public CommentResponseDto getCommentById(@PathVariable Long id) {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        return toDTO(comment);
    }

    @GetMapping("/course/{courseId}")
    public Collection<CommentResponseDto> getCommentsByCourse(@PathVariable Long courseId) {
        return toDTOs(commentService.findByCourseId(courseId));
    }

    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@Valid @RequestBody CommentRequestDto commentRequestDto) {

        Course course = courseService.findById(commentRequestDto.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Comment comment = toEntity(commentRequestDto);
        comment.setPublicationDate(LocalDateTime.now());
        comment.setCourse(course);

        commentService.save(comment);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(comment.getId())
                .toUri();

        return ResponseEntity.created(location).body(toDTO(comment));
    }

    @PutMapping("/{id}")
    public CommentResponseDto updateComment(@PathVariable Long id, @Valid @RequestBody CommentRequestDto commentRequestDto) {

        Comment existingComment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        Course course = courseService.findById(commentRequestDto.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        
        mapper.updateEntity(commentRequestDto, existingComment);
        existingComment.setCourse(course);

        commentService.save(existingComment);

        return toDTO(existingComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        Comment existingComment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        commentService.deleteById(existingComment.getId());
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