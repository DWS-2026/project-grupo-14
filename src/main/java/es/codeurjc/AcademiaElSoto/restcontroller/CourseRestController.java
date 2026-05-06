package es.codeurjc.AcademiaElSoto.restcontroller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.core.Authentication;

import java.util.Collection;
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

@RestController
@RequestMapping("/api/v1/courses")
public class CourseRestController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CourseRestController.class);

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
        Course course = courseService.findById(id).orElseThrow();
        return mapper.toDTO(course);
    }

    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(
            @Valid @RequestBody CourseRequestDto courseRequestDto,
            Authentication authentication) {

        if (!authorizationService.isAdmin(authentication)) {
            logger.warn("Access denied for user '{}' attempting to create a course", authentication.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can create courses");
        }

        Course course = mapper.toEntity(courseRequestDto);
        course.setDescription(htmlSanitizerService.sanitize(course.getDescription()));
        Course savedCourse = courseService.save(course);

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
            logger.warn("Access denied for user '{}' attempting to update course ID: {}", authentication.getName(), id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can update courses");
        }

        Course existingCourse = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        mapper.updateEntity(courseRequestDto, existingCourse);
        existingCourse.setDescription(htmlSanitizerService.sanitize(courseRequestDto.getDescription()));
        Course updatedCourse = courseService.save(existingCourse);

        return mapper.toDTO(updatedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id,
            Authentication authentication) {

        if (!authorizationService.isAdmin(authentication)) {
            logger.warn("Access denied for user '{}' attempting to delete course ID: {}", authentication.getName(), id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can delete courses");
        }

        Course existingCourse = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        courseService.deleteById(existingCourse.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    public Collection<CommentResponseDto> getCourseComments(@PathVariable Long id) {
        Course course = courseService.findById(id).orElseThrow();
        return commentMapper.toDTOs(commentService.findByCourseId(course.getId()));
    }

    // --- NEW DISK IMAGE SYSTEM (ITEM 15) ---

    // 1. Upload/Add image to course
    @PostMapping("/{id}/image")
    public ResponseEntity<Object> uploadCourseImage(
            @PathVariable Long id,
            @RequestParam("imageFile") MultipartFile imageFile,
            Authentication authentication) throws IOException {

        if (!authorizationService.isAdmin(authentication)) {
            logger.warn("Access denied for user '{}' attempting to upload image for course ID: {}", authentication.getName(), id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can upload course images");
        }

        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Course course = courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (course.getImage() != null) {
            imageService.replaceImageFile(course.getImage().getId(), imageFile);
        } else {
            es.codeurjc.AcademiaElSoto.model.Image newImage = imageService.createImage(imageFile);
            courseService.addImageToCourse(id, newImage);
        }

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/courses/{id}/image")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // 2. Download/View the image
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> downloadCourseImage(@PathVariable Long id) throws MalformedURLException {
        Course course = courseService.findById(id).orElseThrow();

        if (course.getImage() != null) {
            // Fetch the physical file using the image ID
            Resource file = imageService.getImageFile(course.getImage().getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. Delete the image
    @DeleteMapping("/{courseId}/image")
    public ResponseEntity<Void> deleteCourseImage(
            @PathVariable Long courseId,
            Authentication authentication) {

        if (!authorizationService.isAdmin(authentication)) {
            logger.warn("Access denied for user '{}' attempting to delete image for course ID: {}", authentication.getName(), courseId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can delete course images");
        }

        Course course = courseService.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (course.getImage() != null) {
            Long imageId = course.getImage().getId();
            courseService.removeImageCourse(courseId, course.getImage());
            imageService.deleteImage(imageId);
        }

        return ResponseEntity.noContent().build();
    }

    // --- EXTERNAL API (GOOGLE BOOKS) ---

    record BooksResponse(List<Book> items) {
    }

    record Book(VolumeInfo volumeInfo) {
    }

    record VolumeInfo(String title) {
    }

    @GetMapping("/{id}/recommended-books")
    public List<String> getRecommendedBooks(@PathVariable Long id) {
        Course course = courseService.findById(id).orElseThrow();
        String courseName = course.getCourseName();

        RestClient restClient = RestClient.create();

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
    }
}