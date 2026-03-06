package es.codeurjc.AcademiaElSoto.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.AcademiaElSoto.model.Carrito;

public interface chartRepository extends JpaRepository<Carrito, Long> {
    // No necesitas escribir código aquí, JpaRepository ya tiene .save()
}