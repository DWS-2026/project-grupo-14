package es.codeurjc.AcademiaElSoto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.AcademiaElSoto.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // No code is needed here; JpaRepository already includes .save().
    List<Course> findByTeacher(String teacher);

    List<Course> findByCourseName(String courseName);
}
