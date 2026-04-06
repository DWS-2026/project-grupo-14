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

/**
 * Service layer for course-related operations.
 * This class provides methods for saving, retrieving,
 * searching, and deleting courses.
 */
@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Saves or updates a course without modifying its image.
     *
     * @param course the course to save
     */
    public void save(Course course) {
        courseRepository.save(course);
    }

    /**
     * Saves or updates a course including an uploaded image.
     * If the image exists, it is converted into a Blob and stored in the course.
     *
     * @param course the course to save
     * @param imageFile the uploaded image file
     * @throws IOException if an error occurs while creating the image Blob
     */
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

    /**
     * Retrieves all courses from the database.
     *
     * @return a list of all courses
     */
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    /**
     * Finds a course by its id.
     *
     * @param id course identifier
     * @return an Optional containing the course if found
     */
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    /**
     * Finds all courses taught by a specific teacher.
     *
     * @param teacher teacher name
     * @return list of matching courses
     */
    public List<Course> findByTeacher(String teacher) {
        return courseRepository.findByTeacher(teacher);
    }

    /**
     * Finds all courses with a given course name.
     *
     * @param courseName course name to search for
     * @return list of matching courses
     */
    public List<Course> findByCourseName(String courseName) {
        return courseRepository.findByCourseName(courseName);
    }

    /**
     * Deletes a course by its id.
     *
     * @param id course identifier
     */
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }
}