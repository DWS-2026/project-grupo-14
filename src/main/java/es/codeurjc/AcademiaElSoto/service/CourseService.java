package es.codeurjc.AcademiaElSoto.service;

import java.io.IOException;
import java.sql.Blob;
import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.repository.CourseRepository;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    // Save or update a course without an image.
    public void save(Course course) {
        courseRepository.save(course);
    }

    // Save or update a course with an image.
    public void save(Course course, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Blob blob = new SerialBlob(imageFile.getBytes());
                course.setImageFile(blob);
            } catch (Exception e) {
                throw new IOException("Error creating the image Blob", e);
            }
        }
        this.save(course);
    }

    // Find all courses.
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    // Find by id.
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    // Find by teacher.
    public List<Course> findByTeacher(String teacher) {
        return courseRepository.findByTeacher(teacher);
    }

    // Find by course name.
    public List<Course> findByCourseName(String courseName) {
        return courseRepository.findByCourseName(courseName);
    }

    // Delete a course by id.
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }
}
