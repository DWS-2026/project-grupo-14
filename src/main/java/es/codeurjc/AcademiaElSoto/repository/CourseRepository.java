package es.codeurjc.AcademiaElSoto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.AcademiaElSoto.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Returns all courses taught by the given teacher.
     */
    List<Course> findByTeacher(String teacher);

    /**
     * Returns all courses whose course name matches the given value.
     */
    List<Course> findByCourseName(String courseName);
}