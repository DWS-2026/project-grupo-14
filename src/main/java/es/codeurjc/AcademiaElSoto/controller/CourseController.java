package es.codeurjc.AcademiaElSoto.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import es.codeurjc.AcademiaElSoto.model.Carrito;
import es.codeurjc.AcademiaElSoto.model.Curso;
import es.codeurjc.AcademiaElSoto.model.Usuario;
import es.codeurjc.AcademiaElSoto.repository.cursoRepository;
import es.codeurjc.AcademiaElSoto.repository.userRepository;
import es.codeurjc.AcademiaElSoto.repository.cartRepository;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.rowset.serial.SerialBlob;
import java.util.List;
import es.codeurjc.AcademiaElSoto.repository.comentRepository;

import org.springframework.http.MediaType;

import jakarta.annotation.PostConstruct;

@Controller
public class CourseController {

    @Autowired
    private cursoRepository cursoRepository;

    @Autowired
    private userRepository usuarioRepository;

    @Autowired
    private cartRepository carritoRepository;

    @Autowired
    private comentRepository comentarioRepository;

    // Datos temporales para probar que funciona

    @PostConstruct
    public void init() {
        
    }

    @GetMapping("/cursos")
    public String showCourses(Model model) {

        model.addAttribute("cursos", cursoRepository.findAll());

        return "cursos";
    }

    @GetMapping("/curso/{id}")
    public String showCourse(Model model, @PathVariable long id) {

        Optional<Curso> op = cursoRepository.findById(id);

        if (op.isPresent()) {
            Curso curso = op.get();
            model.addAttribute("curso", curso);
            model.addAttribute("hasImage", curso.getImageFile() != null);
            model.addAttribute("comentarios", comentarioRepository.findByCursoIdOrderByFechaPublicacionDesc(id));

            return "bd_course/show_course";
        } else {
            return "bd_course/course_not_found";
        }
    }

    @PostMapping("/curso/new")
    public String newCourse(Model model, Curso curso,
            @RequestParam MultipartFile imagen) {

        try {
            if (!imagen.isEmpty()) {
                curso.setImageFile(new SerialBlob(imagen.getBytes()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cursoRepository.save(curso);

        return "bd_course/saved_course";
    }

    @GetMapping("/admin_estadisticas")
    public String mostrarEstadisticasAdmin(Model model) {
        // Obtenemos todos los cursos de la base de datos
        List<Curso> cursos = cursoRepository.findAll();
        
        // Se los pasamos a la vista Mustache con la clave "cursos"
        model.addAttribute("cursos", cursos);
        
        return "admin_estadisticas"; // Nombre de tu archivo HTML
    }

    @GetMapping("/curso/{id}/image")
    public ResponseEntity<Object> getImage(@PathVariable long id) throws Exception {

        Curso curso = cursoRepository.findById(id).orElseThrow();

        if (curso.getImageFile() != null) {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(curso.getImageFile().getBinaryStream()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/editcurso/{id}")
    public String editCourse(Model model, @PathVariable long id) {

        Optional<Curso> op = cursoRepository.findById(id);

        if (op.isPresent()) {
            Curso curso = op.get();
            model.addAttribute("curso", curso);
            return "bd_course/edit_course_page";
        } else {
            return "bd_course/course_not_found";
        }
    }

    @PostMapping("/editedcurso/{id}")
    public String editCourseProcess(Model model, @PathVariable long id, Curso editedCurso) {

        Optional<Curso> op = cursoRepository.findById(editedCurso.getId());

        if (op.isPresent()) {
            editedCurso.setId(id); // aseguramos el id correcto
            cursoRepository.save(editedCurso);
            model.addAttribute("curso", editedCurso);
            return "bd_course/edited_course";
        } else {
            return "bd_course/course_not_found";
        }
    }

    @PostMapping("/curso/{id}/delete")
    public String deleteCourse(Model model, @PathVariable long id) {

        Optional<Curso> curso = cursoRepository.findById(id);

        if (curso.isPresent()) {
            cursoRepository.deleteById(id);
            return "bd_course/deleted_course";
        } else {
            return "bd_course/course_not_found";
        }
    }

    @PostMapping("/curso/{id}/add-carrito")
    public String añadirAlCarrito(Model model, @PathVariable long id) {

        Optional<Curso> opCurso = cursoRepository.findById(id);

        // ¡TRUCO! Cogemos la lista de todos los usuarios y nos quedamos con el primero
        // que haya
        List<Usuario> usuarios = usuarioRepository.findAll();

        // Si el curso existe y hay al menos un usuario en la base de datos...
        if (opCurso.isPresent() && !usuarios.isEmpty()) {

            Curso curso = opCurso.get();
            Usuario usuario = usuarios.get(0); // Cogemos al usuario de prueba (sea cual sea su ID)

            // 1. Obtenemos el carrito del usuario. Si no tiene, le creamos uno nuevo.
            Carrito carrito = usuario.getCarrito();
            if (carrito == null) {
                carrito = new Carrito("Carrito de " + usuario.getNombre(), 0);
                carrito.setUsuario(usuario);
                usuario.setCarrito(carrito);
            }

            // 2. Añadimos el curso al carrito
            carrito.addCurso(curso);

            // 3. Guardamos los cambios
            usuarioRepository.save(usuario);

            // Redirigimos a la página de cursos para que pueda seguir comprando
            return "redirect:/cursos";

        } else {
            return "bd_course/course_not_found";
        }
    }

}
