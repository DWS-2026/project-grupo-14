package es.codeurjc.AcademiaElSoto.restcontroller;

import java.net.URI;
import java.sql.Blob;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping("/api/v1/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper mapper;

    @GetMapping
    public Page<UserResponseDto> getUsers(Pageable pageable) {
        // Usamos el .map() de la página para convertir cada User en UserResponseDto
        return userService.getUsers(pageable).map(this::toDto);
    }

    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable Long id) {
        // Lanza NoSuchElementException si el id no existe
        User user = userService.findById(id).orElseThrow();
        return toDto(user);
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {

        
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
    public UserResponseDto updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDto userRequestDto) {

        
        User existingUser = userService.findById(id).orElseThrow();

        mapper.updateEntity(userRequestDto, existingUser);

        if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        }

        User updatedUser = userService.saveUser(existingUser);

        return toDto(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        
        User existingUser = userService.findById(id).orElseThrow();
        userService.deleteById(existingUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Object> getUserImage(@PathVariable Long id) throws Exception {
        
        User user = userService.findById(id).orElseThrow();
        Blob image = user.getProfileImage();

        if (image == null) {
            throw new NoSuchElementException();
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(new InputStreamResource(image.getBinaryStream()));
    }

    

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