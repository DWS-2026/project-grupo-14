package es.codeurjc.AcademiaElSoto.controller;

import es.codeurjc.AcademiaElSoto.model.Carrito;
import es.codeurjc.AcademiaElSoto.model.Curso;
import es.codeurjc.AcademiaElSoto.repository.cartRepository;
import es.codeurjc.AcademiaElSoto.repository.cursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Importante importar HttpSession (dependiendo de tu versión de Spring Boot puede ser javax o jakarta)
import jakarta.servlet.http.HttpSession; 
import java.util.Optional;

@Controller
public class CartController {

    @Autowired
    private cartRepository cartRepository;

    @Autowired
    private cursoRepository cursoRepository;

    @PostMapping("/carrito/add")
    public String agregarAlCarrito(@RequestParam("cursoId") Long cursoId, HttpSession session) {
        
        // 1. Buscamos el curso en la base de datos por su ID
        Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);

        if (cursoOpt.isPresent()) {
            Curso curso = cursoOpt.get();
            Carrito carrito = null;

            // 2. Buscamos si el usuario ya tiene un carrito guardado en su sesión actual
            Long carritoId = (Long) session.getAttribute("carritoId");

            if (carritoId != null) {
                // Si ya tiene un ID de carrito, lo recuperamos de la base de datos
                carrito = cartRepository.findById(carritoId).orElse(null);
            }

            // 3. Si no tiene carrito (es el primer curso que añade) o no se encontró en la BD
            if (carrito == null) {
                carrito = new Carrito("Carrito de la compra", 0);
            }

            // 4. Añadimos el curso a la lista del carrito 
            // (Llamamos al método addCurso() que creamos en la entidad Carrito)
            carrito.addCurso(curso);

            // 5. Guardamos el carrito en la base de datos para que se actualice la lista y el precio
            cartRepository.save(carrito);

            // 6. ¡Muy importante! Guardamos el ID de este carrito en la sesión del usuario
            // Así, cuando añada un segundo curso, sabremos cuál es su carrito
            session.setAttribute("carritoId", carrito.getId());
        }

        // 7. Redirigimos al usuario de vuelta a la página de cursos
        return "redirect:/cursos"; 
    }
}