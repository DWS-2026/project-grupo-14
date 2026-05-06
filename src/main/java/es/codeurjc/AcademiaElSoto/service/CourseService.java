package es.codeurjc.AcademiaElSoto.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.model.Image;
import es.codeurjc.AcademiaElSoto.repository.CourseRepository;

/**
 * Service layer for course-related operations.
 * This class provides methods for saving, retrieving,
 * searching, and deleting courses.
 */
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final HtmlSanitizerService htmlSanitizerService;

    public CourseService(CourseRepository courseRepository,
            HtmlSanitizerService htmlSanitizerService) {
        this.courseRepository = courseRepository;
        this.htmlSanitizerService = htmlSanitizerService;
    }

    /**
     * Saves or updates a course.
     * (The overloaded method with MultipartFile was removed because
     * ImageService now handles disk storage directly).
     *
     * @param course the course to save
     * @return the saved course
     */
    public Course save(Course course) {
        if (course.getDescription() != null) {
            course.setDescription(htmlSanitizerService.sanitize(course.getDescription()));
        }

        if (course.getCourseName() != null) {
            course.setCourseName(course.getCourseName().trim());
        }

        if (course.getTeacher() != null) {
            course.setTeacher(course.getTeacher().trim());
        }

        return courseRepository.save(course);
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
     * Retrieves all courses from the database using pagination.
     *
     * @param pageable pagination information
     * @return a paginated list of courses
     */
    public Page<Course> findAll(Pageable pageable) {
        return courseRepository.findAll(pageable);
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

    /**
     * Links a main image to a course.
     *
     * @param id    course identifier
     * @param image the image entity to link
     * @return the updated course
     */
    public Course addImageToCourse(long id, Image image) {
        Course course = courseRepository.findById(id).orElseThrow();
        course.setImage(image); // Set the main one-to-one image
        return courseRepository.save(course);
    }

    /**
     * Unlinks the main image from a course.
     *
     * @param courseId course identifier
     * @param image    the image entity to unlink
     * @return the updated course
     */
    public Course removeImageCourse(long courseId, Image image) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        course.setImage(null); // Unlink the main image
        return courseRepository.save(course);
    }

    /**
     * Replaces a course entity with a new one while preserving existing
     * relationships like images or comments.
     * 
     * @param id            course identifier
     * @param updatedCourse the new course data
     * @return the updated course saved in the database
     */
    public Course replaceCourse(long id, Course updatedCourse) {
        Course course = courseRepository.findById(id).orElseThrow();

        updatedCourse.setId(id);
        updatedCourse.setImage(course.getImage());
        updatedCourse.setImages(course.getImages());

        if (updatedCourse.getDescription() != null) {
            updatedCourse.setDescription(htmlSanitizerService.sanitize(updatedCourse.getDescription()));
        }

        if (updatedCourse.getCourseName() != null) {
            updatedCourse.setCourseName(updatedCourse.getCourseName().trim());
        }

        if (updatedCourse.getTeacher() != null) {
            updatedCourse.setTeacher(updatedCourse.getTeacher().trim());
        }

        return courseRepository.save(updatedCourse);
    }
}