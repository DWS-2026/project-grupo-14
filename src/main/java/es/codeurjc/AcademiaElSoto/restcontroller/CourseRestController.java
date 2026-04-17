package es.codeurjc.AcademiaElSoto.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.AcademiaElSoto.dto.CourseResponseDto;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.service.CourseService;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseRestController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public Page<CourseResponseDto> getCourses(Pageable pageable) {
        return courseService.findAll(pageable).map(this::toDto);
    }

    private CourseResponseDto toDto(Course course) {
    return new CourseResponseDto(
            course.getId(),
            course.getCourseName(),
            course.getTeacher(),
            course.getPrice(),
            course.getDescription(),
            course.getStudents());
}
}