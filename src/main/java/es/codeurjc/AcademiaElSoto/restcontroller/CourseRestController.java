package es.codeurjc.AcademiaElSoto.restcontroller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

// A09: Logger for security auditing and monitoring
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.core.Authentication;

import es.codeurjc.AcademiaElSoto.dto.CommentResponseDto;
import es.codeurjc.AcademiaElSoto.mapper.CommentMapper;
import es.codeurjc.AcademiaElSoto.service.AuthorizationService;
import es.codeurjc.AcademiaElSoto.service.CommentService;
import es.codeurjc.AcademiaElSoto.dto.CourseRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CourseResponseDto;
import es.codeurjc.AcademiaElSoto.mapper.CourseMapper;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.service.CourseService;
import es.codeurjc.AcademiaElSoto.service.ImageService;
import es.codeurjc.AcademiaElSoto.service.HtmlSanitizerService;
import jakarta.validation.Valid;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseRestController {

    // A09: Logger initialization for audit trails
    private static final Logger log = LoggerFactory.getLogger(CourseRestController.class);

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseMapper mapper;

    @Autowired
    private ImageService imageService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private HtmlSanitizerService htmlSanitizerService;

    // --- BASIC CRUD METHODS ---

    @GetMapping
    public Page<CourseResponseDto> getCourses(Pageable pageable) {
        return courseService.findAll(pageable).map(mapper::toDTO);
    }

    @GetMapping("/{id}")
    public CourseResponseDto getCourseById(@PathVariable Long id) {
        Course course = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return mapper.toDTO(course);
    }

    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(
            @Valid @RequestBody CourseRequestDto courseRequestDto,
            Authentication authentication) {

        if (!authorizationService.isAdmin(authentication)) {
            log.warn("SECURITY ALERT: Unauthorized course creation attempt by user '{}'", authentication.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrative privileges required");
        }

        Course course = mapper.toEntity(courseRequestDto);
        
        // A03: Sanitize input to prevent XSS
        course.setDescription(htmlSanitizerService.sanitize(course.getDescription()));
        
        Course savedCourse = courseService.save(course);

        // A09: Audit log for record creation
        log.info("ADMIN ACTION: New course '{}' created via API by user '{}'", 
                 savedCourse.getCourseName(), authentication.getName());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCourse.getId())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toDTO(savedCourse));
    }

    @PutMapping("/{id}")
    public CourseResponseDto updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequestDto courseRequestDto,
            Authentication authentication) {

        if (!authorizationService.isAdmin(authentication)) {
            log.error("SECURITY ALERT: Unauthorized update attempt on course ID {} by user '{}'", id, authentication.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrative privileges required");
        }

        Course existingCourse = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        // A08: Data Integrity - Map only allowed fields from DTO
        mapper.updateEntity(courseRequestDto, existingCourse);
        
        // Re-enforcing integrity for the 'students' capacity field
        existingCourse.setStudents(courseRequestDto.getStudents());
        
        existingCourse.setDescription(htmlSanitizerService.sanitize(courseRequestDto.getDescription()));
        
        Course updatedCourse = courseService.save(existingCourse);

        // A09: Logging administrative modification
        log.info("ADMIN ACTION: Course ID {} ('{}') updated via API.", id, updatedCourse.getCourseName());

        return mapper.toDTO(updatedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id,
            Authentication authentication) {

        if (!authorizationService.isAdmin(authentication)) {
            log.error("SECURITY ALERT: Unauthorized deletion attempt on course ID {} by user '{}'", id, authentication.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrative privileges required");
        }

        Course existingCourse = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        courseService.deleteById(existingCourse.getId());
        
        // A09: Warning log for permanent data removal
        log.warn("ADMIN ALERT: Course ID {} ('{}') was permanently deleted via API.", id, existingCourse.getCourseName());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    public Collection<CommentResponseDto> getCourseComments(@PathVariable Long id) {
        Course course = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return commentMapper.toDTOs(commentService.findByCourseId(course.getId()));
    }

    // --- IMAGE SYSTEM (DISK PERSISTENCE) ---

    @PostMapping("/{id}/image")
    public ResponseEntity<Object> uploadCourseImage(
            @PathVariable Long id,
            @RequestParam("imageFile") MultipartFile imageFile,
            Authentication authentication) throws IOException {

        if (!authorizationService.isAdmin(authentication)) {
            log.warn("SECURITY ALERT: Unauthorized image upload for course ID: {}", id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrative privileges required");
        }

        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Course course = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        // A08: Integrity Check - Manage physical file replacement
        if (course.getImage() != null) {
            imageService.replaceImageFile(course.getImage().getId(), imageFile);
            log.info("IMAGE SYSTEM: Replaced image for course ID: {}", id);
        } else {
            es.codeurjc.AcademiaElSoto.model.Image newImage = imageService.createImage(imageFile);
            courseService.addImageToCourse(id, newImage);
            log.info("IMAGE SYSTEM: New image uploaded for course ID: {}", id);
        }

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/courses/{id}/image")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> downloadCourseImage(@PathVariable Long id) throws MalformedURLException {
        Course course = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (course.getImage() != null) {
            // A08: Fetch file via Internal ID to prevent Path Traversal
            Resource file = imageService.getImageFile(course.getImage().getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{courseId}/image")
    public ResponseEntity<Void> deleteCourseImage(
            @PathVariable Long courseId,
            Authentication authentication) {

        if (!authorizationService.isAdmin(authentication)) {
            log.warn("SECURITY ALERT: Unauthorized image deletion for course ID: {}", courseId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrative privileges required");
        }

        Course course = courseService.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (course.getImage() != null) {
            Long imageId = course.getImage().getId();
            courseService.removeImageCourse(courseId, course.getImage());
            imageService.deleteImage(imageId);
            log.warn("IMAGE SYSTEM: Image ID {} deleted for course ID: {}", imageId, courseId);
        }

        return ResponseEntity.noContent().build();
    }

    // --- EXTERNAL API INTEGRATION (A08: Supply Chain Integrity) ---

    record BooksResponse(List<Book> items) {}
    record Book(VolumeInfo volumeInfo) {}
    record VolumeInfo(String title) {}

    @GetMapping("/{id}/recommended-books")
    public List<String> getRecommendedBooks(@PathVariable Long id) {
        Course course = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        
        String courseName = course.getCourseName();

        // A09: Logging outbound call to external service
        log.info("EXTERNAL API: Fetching recommended books for course '{}' from Google Books.", courseName);

        RestClient restClient = RestClient.create();

        try {
            BooksResponse data = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.googleapis.com")
                        .path("/books/v1/volumes")
                        .queryParam("q", "intitle:" + courseName)
                        .build())
                    .retrieve()
                    .body(BooksResponse.class);

            List<String> titles = new ArrayList<>();
            if (data != null && data.items() != null) {
                for (Book book : data.items()) {
                    titles.add(book.volumeInfo().title());
                }
            }
            return titles;
            
        } catch (Exception e) {
            // A08: Handling third-party integrity/availability failures
            log.error("EXTERNAL API ERROR: Failed to fetch books from Google. Potential connectivity or integrity issue.");
            return new ArrayList<>();
        }
    }
}