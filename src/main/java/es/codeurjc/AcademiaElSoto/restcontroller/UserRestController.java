package es.codeurjc.AcademiaElSoto.restcontroller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // <-- Important for security
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    // --- BASIC CRUD METHODS (SECURED) ---

    @GetMapping
    public Page<UserResponseDto> getUsers(Pageable pageable, Authentication authentication) {
        // Check if user has ADMIN role
        if (!authorizationService.isAdmin(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can list all users");
        }

        return userService.getUsers(pageable).map(this::toDto);
    }

    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable Long id, Authentication authentication) {
        authorizationService.checkUserAccess(id, authentication); // <-- Security wall

        User user = userService.findById(id).orElseThrow();
        return toDto(user);
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        // Creating a user (registering) is usually public, no security wall needed here
        if (userService.existsByUserName(userRequestDto.getUserName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userService.existsByEmail(userRequestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = toEntity(userRequestDto);

        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        user.setRoles(List.of("USER"));
        user.setCart(new Cart("Carrito de " + userRequestDto.getUserName(), 0));

        User savedUser = userService.saveUser(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(toDto(savedUser));
    }

    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDto userRequestDto,
            Authentication authentication) {
        authorizationService.checkUserAccess(id, authentication); // <-- Security wall

        User existingUser = userService.findById(id).orElseThrow();

        mapper.updateEntity(userRequestDto, existingUser);

        if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        }

        User updatedUser = userService.saveUser(existingUser);

        return toDto(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id, Authentication authentication) {
        authorizationService.checkUserAccess(id, authentication); // <-- Security wall

        User existingUser = userService.findById(id).orElseThrow();

        if (existingUser.getProfileImage() != null) {
            imageService.deleteImage(existingUser.getProfileImage().getId());
        }

        userService.deleteById(existingUser.getId());

        // Create the response with the success message
        Map<String, String> response = Map.of("message", "User deleted successfully");

        return ResponseEntity.ok(response);
    }

    // --- DISK IMAGE SYSTEM (SECURED) ---

    @PostMapping("/{id}/image")
    public ResponseEntity<Object> uploadUserImage(@PathVariable Long id,
            @RequestParam("imageFile") MultipartFile imageFile, Authentication authentication) throws IOException {
        authorizationService.checkUserAccess(id, authentication); // <-- Security wall

        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.findById(id).orElseThrow();

        if (user.getProfileImage() != null) {
            imageService.replaceImageFile(user.getProfileImage().getId(), imageFile);
        } else {
            Image newImage = imageService.createImage(imageFile);
            user.setProfileImage(newImage);
            userService.saveUser(user);
        }

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/users/{id}/image")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getUserImage(@PathVariable Long id) throws MalformedURLException {
        // Downloading the image is public, so everyone can see avatars. No security
        // wall.
        User user = userService.findById(id).orElseThrow();

        if (user.getProfileImage() != null) {
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
        authorizationService.checkUserAccess(id, authentication); // <-- Security wall

        User user = userService.findById(id).orElseThrow();

        if (user.getProfileImage() != null) {
            Long imageId = user.getProfileImage().getId();

            user.setProfileImage(null);
            userService.saveUser(user);

            imageService.deleteImage(imageId);
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