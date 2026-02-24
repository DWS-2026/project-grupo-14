package es.codeurjc.AcademiaElSoto.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import es.codeurjc.AcademiaElSoto.model.Usuario;
import es.codeurjc.AcademiaElSoto.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    @Autowired
    private UserService userService;

    // Guardar usuario
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return userService.guardarUsuario(usuario);
    }

    // Obtener todos los usuarios
    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return userService.obtenerUsuarios();
    }

}