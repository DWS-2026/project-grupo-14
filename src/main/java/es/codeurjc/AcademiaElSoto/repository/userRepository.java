package es.codeurjc.AcademiaElSoto.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.AcademiaElSoto.model.Usuario;

public interface userRepository extends JpaRepository<Usuario, Long> {
    // No necesitas escribir código aquí, JpaRepository ya tiene .save()
}