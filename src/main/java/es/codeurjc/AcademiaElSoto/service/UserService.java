package es.codeurjc.AcademiaElSoto.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.repository.UserRepository;

/**
 * Service layer for user-related operations.
 * This class centralizes database access related to users.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Saves a new user or updates an existing one.
     *
     * @param user the user to save
     * @return the saved user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Retrieves all users from the database.
     *
     * @return a list of all users
     */
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}