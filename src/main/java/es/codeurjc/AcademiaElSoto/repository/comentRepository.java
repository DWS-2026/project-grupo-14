package es.codeurjc.AcademiaElSoto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.AcademiaElSoto.model.Comentario;

public interface comentRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByCursoIdOrderByFechaPublicacionDesc(Long cursoId);
}