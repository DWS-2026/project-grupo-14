package es.codeurjc.AcademiaElSoto.repository;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.AcademiaElSoto.model.Usuario;

@Controller
public class SessionController {

	@Autowired
	private Usuario usuario;

	private String infoCompartida;

	

	@PostMapping("/user")
	public String user(Model model, HttpSession session,
			@RequestParam String userName,
			@RequestParam String apellidos,
			@RequestParam String email,
			@RequestParam String password) {

		
		Usuario usuario = new Usuario(); // crear usuario

		usuario.setNombre(userName);
		usuario.setEmail(email);
		usuario.setPassword(password);
		usuario.setApellidos(apellidos);

		session.setAttribute("usuario", usuario); // guardar en sesión

		return "auth/resultado_formulario";
	}

	@GetMapping("/mostrarResultados")
		public String mostrarResultados(HttpSession session, Model model) {

		
		Usuario usuario = (Usuario) session.getAttribute("usuario");

		if (usuario != null) {
			model.addAttribute("userName", usuario.getNombre());
			model.addAttribute("apellidos", usuario.getApellidos());
			model.addAttribute("email", usuario.getEmail());
		}

		return "user";
	}
}
