package es.codeurjc.AcademiaElSoto.restcontroller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.service.CourseService;

@RestController // <-- La etiqueta mágica
@RequestMapping("/api/courses") // <-- Todas las rutas empezarán por /api/courses
public class CourseRestController {

    @Autowired
    private CourseService courseService;

    // Cuando alguien entre a http://localhost:8080/api/courses
    @GetMapping
    public List<Course> getAllCourses() {
        // Devolvemos la lista de cursos directamente. 
        // Spring Boot se encarga de convertirla a JSON.
        return courseService.findAll(); 
    }

    // Cuando alguien busque un curso específico: /api/courses/1
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.findById(id).orElse(null);
    }
}