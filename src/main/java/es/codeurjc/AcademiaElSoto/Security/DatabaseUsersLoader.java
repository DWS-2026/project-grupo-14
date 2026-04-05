package es.codeurjc.AcademiaElSoto.Security;

import org.springframework.beans.factory.annotation.Autowired;
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

    @PostConstruct
    private void initDatabase() {

        if (userRepository.findByUserName("user").isEmpty()) {
            userRepository.save(
                    new User("user", "UserApellido", "user@email.com",
                            passwordEncoder.encode("pass"), "USER"));
        }

        if (userRepository.findByUserName("admin").isEmpty()) {
            userRepository.save(
                    new User("admin", "AdminApellido", "admin@email.com",
                            passwordEncoder.encode("adminpass"), "USER", "ADMIN"));
        }
    }
}
