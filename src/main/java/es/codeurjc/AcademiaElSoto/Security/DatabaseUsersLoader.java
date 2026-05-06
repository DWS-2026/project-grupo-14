package es.codeurjc.AcademiaElSoto.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // <-- Necessary for environment variables
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.repository.UserRepository;

@Component
public class DatabaseUsersLoader {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Inject the normal user password from application.properties
    @Value("${default.user.password}")
    private String userPassword;

    // Inject the admin password from application.properties
    @Value("${default.admin.password}")
    private String adminPassword;

    @PostConstruct
    private void initDatabase() {

        // Create a default normal user if it doesn't exist
        if (userRepository.findByUserName("user").isEmpty()) {
            userRepository.save(
                    new User("user", "UserApellido", "user@email.com",
                            passwordEncoder.encode(userPassword), "USER"));
        }

        // Create a default admin user if it doesn't exist
        if (userRepository.findByUserName("admin").isEmpty()) {
            userRepository.save(
                    new User("admin", "AdminApellido", "admin@email.com",
                            passwordEncoder.encode(adminPassword), "USER", "ADMIN"));
        }
    }
}