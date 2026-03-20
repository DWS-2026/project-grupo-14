package es.codeurjc.AcademiaElSoto.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.AcademiaElSoto.model.Comentario;
import es.codeurjc.AcademiaElSoto.model.Curso;
import es.codeurjc.AcademiaElSoto.repository.comentRepository;
import es.codeurjc.AcademiaElSoto.repository.cursoRepository;

@Controller
public class CommentController {

    @Autowired
    private comentRepository comentarioRepository;

    @Autowired
    private cursoRepository cursoRepository;

    @PostMapping("/curso/{id}/comentario")
    public String guardarComentario(@PathVariable long id,
                                    @RequestParam String usuario,
                                    @RequestParam String descripcion) {

        Optional<Curso> opCurso = cursoRepository.findById(id);

        if (opCurso.isPresent()) {
            Curso curso = opCurso.get();

            Comentario comentario = new Comentario();
            comentario.setUsuario(usuario);
            comentario.setDescripcion(descripcion);
            comentario.setCurso(curso);
            comentario.setFechaPublicacion(java.time.LocalDateTime.now());

            comentarioRepository.save(comentario);
        }

        return "redirect:/cursos";
    }
}