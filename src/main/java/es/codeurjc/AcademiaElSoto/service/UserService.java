package es.codeurjc.AcademiaElSoto.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import es.codeurjc.AcademiaElSoto.model.Usuario;
import es.codeurjc.AcademiaElSoto.repository.userRepository;

@Service
public class UserService {

    @Autowired
    private userRepository userRepository;

    public Usuario guardarUsuario(Usuario usuario) {
        return userRepository.save(usuario);
    }

    public List<Usuario> obtenerUsuarios() {
        return userRepository.findAll();
    }

}