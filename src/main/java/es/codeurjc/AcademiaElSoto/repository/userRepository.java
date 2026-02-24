package es.codeurjc.AcademiaElSoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.AcademiaElSoto.model.Usuario;

public interface UserRepository extends JpaRepository<Usuario, Long> {

    Usuario findByEmail(String email);

}