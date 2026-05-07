package es.codeurjc.AcademiaElSoto.restcontroller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// A09: Using SLF4J for security event monitoring and operational auditing
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.AcademiaElSoto.dto.UserRequestDto;
import es.codeurjc.AcademiaElSoto.dto.UserResponseDto;
import es.codeurjc.AcademiaElSoto.mapper.UserMapper;
import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Image;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.service.ImageService;
import es.codeurjc.AcademiaElSoto.service.UserService;
import es.codeurjc.AcademiaElSoto.service.AuthorizationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    // A09: Logger initialization for system-wide traceability
    private static final Logger log = LoggerFactory.getLogger(UserRestController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private ImageService imageService;

    @Autowired
    private AuthorizationService authorizationService;

    // --- BASIC CRUD METHODS ---

    @GetMapping
    public Page<UserResponseDto> getUsers(Pageable pageable, Authentication authentication) {
        // A01: Administrative role verification
        if (!authorizationService.isAdmin(authentication)) {
            log.warn("SECURITY ALERT: Unauthorized attempt to list all users by '{}'", authentication.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrative privileges required");
        }

        return userService.getUsers(pageable).map(this::toDto);
    }

    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable Long id, Authentication authentication) {
        try {
            // A01: Broken Access Control check - Verify if requester owns the profile or is Admin
            authorizationService.checkUserAccess(id, authentication);
        } catch (ResponseStatusException e) {
            log.error("SECURITY ALERT: User '{}' denied access to profile ID: {}", authentication.getName(), id);
            throw e;
        }

        User user = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toDto(user);
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        if (userService.existsByUserName(userRequestDto.getUserName())) {
            log.warn("SIGNUP ATTEMPT FAILED: Username '{}' is already in use.", userRequestDto.getUserName());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userService.existsByEmail(userRequestDto.getEmail())) {
            log.warn("SIGNUP ATTEMPT FAILED: Email '{}' is already in use.", userRequestDto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // A08: Integrity - DTO mapping prevents Mass Assignment of sensitive fields (like ROLES)
        User user = toEntity(userRequestDto);

        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        user.setRoles(List.of("USER")); // Defaulting to lowest privilege
        user.setCart(new Cart("Cart for " + userRequestDto.getUserName(), 0));

        User savedUser = userService.saveUser(user);
        
        // A09: Audit log for new user registration
        log.info("API ACTION: New user '{}' registered with ID {}", savedUser.getUserName(), savedUser.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(toDto(savedUser));
    }

    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDto userRequestDto,
            Authentication authentication) {
        try {
            authorizationService.checkUserAccess(id, authentication);
        } catch (ResponseStatusException e) {
            log.error("SECURITY ALERT: User '{}' denied update access for profile ID: {}", authentication.getName(), id);
            throw e;
        }

        User existingUser = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean wasLocked = !existingUser.isAccountNonLocked();

        // A08: Data Integrity - Controlled mapping via DTO
        mapper.updateEntity(userRequestDto, existingUser);

        if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        }

        // A01: Business Rule - Non-admins cannot unlock their own accounts if they were locked
        if (!authorizationService.isAdmin(authentication) && wasLocked) {
            existingUser.setAccountNonLocked(false);
        }

        User updatedUser = userService.saveUser(existingUser);
        
        // A09: Logging successful profile updates
        log.info("API ACTION: User profile ID {} updated by '{}'.", id, authentication.getName());

        return toDto(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id, Authentication authentication) {
        try {
            authorizationService.checkUserAccess(id, authentication);
        } catch (ResponseStatusException e) {
            log.error("SECURITY ALERT: User '{}' denied deletion access for profile ID: {}", authentication.getName(), id);
            throw e;
        }

        User existingUser = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (existingUser.getProfileImage() != null) {
            imageService.deleteImage(existingUser.getProfileImage().getId());
        }

        userService.deleteById(existingUser.getId());
        
        // A09: Important log for account termination auditing
        log.warn("API ALERT: User ID {} ('{}') has been permanently deleted by '{}'.", 
                 id, existingUser.getUserName(), authentication.getName());

        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // --- DISK IMAGE SYSTEM (SECURED) ---

    @PostMapping("/{id}/image")
    public ResponseEntity<Object> uploadUserImage(@PathVariable Long id,
            @RequestParam("imageFile") MultipartFile imageFile, Authentication authentication) throws IOException {
        try {
            authorizationService.checkUserAccess(id, authentication);
        } catch (ResponseStatusException e) {
            log.error("SECURITY ALERT: Unauthorized image upload attempt by '{}' for user ID: {}", authentication.getName(), id);
            throw e;
        }

        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getProfileImage() != null) {
            imageService.replaceImageFile(user.getProfileImage().getId(), imageFile);
            log.info("IMAGE SYSTEM: Profile image replaced for user ID: {}", id);
        } else {
            Image newImage = imageService.createImage(imageFile);
            user.setProfileImage(newImage);
            userService.saveUser(user);
            log.info("IMAGE SYSTEM: New profile image uploaded for user ID: {}", id);
        }

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/users/{id}/image")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getUserImage(@PathVariable Long id) throws MalformedURLException {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getProfileImage() != null) {
            // A08: Using Internal IDs for file fetching prevents Path Traversal
            Resource file = imageService.getImageFile(user.getProfileImage().getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteUserImage(@PathVariable Long id, Authentication authentication) {
        try {
            authorizationService.checkUserAccess(id, authentication);
        } catch (ResponseStatusException e) {
            log.error("SECURITY ALERT: Unauthorized image deletion attempt by '{}' for user ID: {}", authentication.getName(), id);
            throw e;
        }

        User user = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getProfileImage() != null) {
            Long imageId = user.getProfileImage().getId();
            user.setProfileImage(null);
            userService.saveUser(user);
            imageService.deleteImage(imageId);
            log.warn("IMAGE SYSTEM: Profile image deleted for user ID: {}", id);
        }

        return ResponseEntity.noContent().build();
    }

    // --- MAPPERS ---

    private UserResponseDto toDto(User user) {
        return mapper.toDTO(user);
    }

    private User toEntity(UserRequestDto userRequestDto) {
        return mapper.toEntity(userRequestDto);
    }

    private Collection<UserResponseDto> toDTOs(Collection<User> users) {
        return mapper.toDTOs(users);
    }
}