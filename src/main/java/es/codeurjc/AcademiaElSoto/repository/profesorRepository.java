package es.codeurjc.AcademiaElSoto.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.AcademiaElSoto.model.Profesores;

public interface profesorRepository extends JpaRepository<Profesores, Long> {
    // No necesitas escribir código aquí, JpaRepository ya tiene .save()
}