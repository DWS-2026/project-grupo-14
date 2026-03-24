package es.codeurjc.AcademiaElSoto.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.AcademiaElSoto.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // Busca si existe un usuario con ese nombre
    Optional<User> findByUserName(String userName);
    
    // Busca si existe un usuario con ese correo
    Optional<User> findByEmail(String email);
    
    // Busca si coincide el nombre O el correo (ideal para el Login)
    Optional<User> findByUserNameOrEmail(String userName, String email);
}