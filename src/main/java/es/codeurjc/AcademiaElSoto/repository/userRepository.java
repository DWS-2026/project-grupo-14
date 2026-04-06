package es.codeurjc.AcademiaElSoto.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.AcademiaElSoto.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username.
     * Returns an Optional because the user may not exist.
     */
    Optional<User> findByUserName(String userName);

    /**
     * Finds a user by email.
     * Returns an Optional because the user may not exist.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user whose username or email matches the given values.
     * Useful for authentication flows where the user may log in
     * using either username or email.
     */
    Optional<User> findByUserNameOrEmail(String userName, String email);
}