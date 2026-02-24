package es.codeurjc.AcademiaElSoto.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.AcademiaElSoto.model.Comentario;

public interface comentRepository extends JpaRepository<Comentario, Long> {
    // No necesitas escribir código aquí, JpaRepository ya tiene .save()
}