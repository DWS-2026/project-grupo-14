package es.codeurjc.AcademiaElSoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.AcademiaElSoto.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    // No code is needed here; JpaRepository already includes .save().
}
