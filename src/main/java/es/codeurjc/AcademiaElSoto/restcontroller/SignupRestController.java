package es.codeurjc.AcademiaElSoto.restcontroller;

import java.net.URI;
import java.util.List;

// A09: Logger for security monitoring and registration auditing
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.AcademiaElSoto.dto.UserRequestDto;
import es.codeurjc.AcademiaElSoto.dto.UserResponseDto;
import es.codeurjc.AcademiaElSoto.mapper.UserMapper;
import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class SignupRestController {

    // A09: Logger initialization
    private static final Logger log = LoggerFactory.getLogger(SignupRestController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public SignupRestController(UserService userService, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody UserRequestDto userRequestDto) {

        // A09: Log the start of a registration attempt
        log.info("SIGNUP: New registration attempt for username: {}", userRequestDto.getUserName());

        if (userService.existsByUserName(userRequestDto.getUserName())) {
            // A09: Logging conflicts to detect potential user enumeration
            log.warn("SIGNUP FAILED: Username '{}' is already taken.", userRequestDto.getUserName());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userService.existsByEmail(userRequestDto.getEmail())) {
            log.warn("SIGNUP FAILED: Email '{}' is already registered.", userRequestDto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // A08: Data Integrity - Using DTO prevents Mass Assignment of unauthorized fields (like roles)
        User user = userMapper.toEntity(userRequestDto);
        
        // Secure password hashing
        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        
        // A08: Integrity Check - Hardcoding the default role to 'USER' prevents privilege escalation
        user.setRoles(List.of("USER"));
        
        // Initializing user state with a new Cart
        user.setCart(new Cart("Cart of " + userRequestDto.getUserName(), 0));

        User savedUser = userService.saveUser(user);

        // A09: Logging successful user creation
        log.info("SIGNUP SUCCESS: User '{}' registered with ID {}.", savedUser.getUserName(), savedUser.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(userMapper.toDTO(savedUser));
    }
}