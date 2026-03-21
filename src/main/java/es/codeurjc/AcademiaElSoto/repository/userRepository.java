package es.codeurjc.AcademiaElSoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.AcademiaElSoto.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
}
