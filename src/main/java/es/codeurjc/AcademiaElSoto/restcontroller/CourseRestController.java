package es.codeurjc.AcademiaElSoto.restcontroller;

import java.net.URI;
import java.sql.Blob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.NoSuchElementException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.AcademiaElSoto.dto.CourseRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CourseResponseDto;
import es.codeurjc.AcademiaElSoto.mapper.CourseMapper;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.service.CourseService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseRestController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseMapper mapper;

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
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseRequestDto courseRequestDto) {

        
        Course course = mapper.toEntity(courseRequestDto);

        Course savedCourse = courseService.save(course);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCourse.getId())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toDTO(savedCourse));
    }

    @PutMapping("/{id}")
    public CourseResponseDto updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequestDto courseRequestDto) {

        Course existingCourse = courseService.findById(id).orElseThrow();

        
        mapper.updateEntity(courseRequestDto, existingCourse);

        Course updatedCourse = courseService.save(existingCourse);
        return mapper.toDTO(updatedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        Course existingCourse = courseService.findById(id).orElseThrow();
        courseService.deleteById(existingCourse.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Object> getCourseImage(@PathVariable Long id) throws Exception {
        Course course = courseService.findById(id).orElseThrow();
        Blob image = course.getImageFile();

        if (image == null) {
            throw new NoSuchElementException();
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(new InputStreamResource(image.getBinaryStream()));
    }

    // EL MÉTODO PRIVADO toDto() YA NO ES NECESARIO, LO HACE EL MAPPER
}