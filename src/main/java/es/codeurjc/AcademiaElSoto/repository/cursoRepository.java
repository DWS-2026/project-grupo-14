package es.codeurjc.AcademiaElSoto.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.AcademiaElSoto.model.Curso; 

public interface cursoRepository extends JpaRepository<Curso, Long> {
    // No necesitas escribir código aquí, JpaRepository ya tiene .save()
    List<Curso> findByProfesor(String profesor);

    List<Curso> findByNombreCurso(String nombreCurso);
}