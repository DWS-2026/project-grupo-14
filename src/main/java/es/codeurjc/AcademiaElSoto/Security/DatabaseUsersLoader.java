package es.codeurjc.AcademiaElSoto.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.model.Cart;
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
            User user = new User("user", "UserApellido", "user@email.com",
                    passwordEncoder.encode("pass"), "USER");
            user.setCart(new Cart("Cart of user", 0));
            userRepository.save(user);
        }

        if (userRepository.findByUserName("admin").isEmpty()) {
            User admin = new User("admin", "AdminApellido", "admin@email.com",
                    passwordEncoder.encode("adminpass"), "USER", "ADMIN");
            admin.setCart(new Cart("Cart of admin", 0));
            userRepository.save(admin);
        }
    }
}
