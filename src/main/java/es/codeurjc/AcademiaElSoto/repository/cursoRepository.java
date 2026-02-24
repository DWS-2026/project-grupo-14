package es.codeurjc.AcademiaElSoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.AcademiaElSoto.model.Curso; // Asegúrate de que la ruta sea donde está tu clase Curso

public interface cursoRepository extends JpaRepository<Curso, Long> {
    // No necesitas escribir código aquí, JpaRepository ya tiene .save()
}