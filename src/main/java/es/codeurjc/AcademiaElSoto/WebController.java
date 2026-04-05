package es.codeurjc.AcademiaElSoto;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/")
    public String barra() {
        return "index";
    }

    @GetMapping("/teachers")
    public String teachers() {
        return "teachers";
    }

    @GetMapping("/information")
    public String information() {
        return "information";
    }

    @GetMapping("/403")
    public String error403() {
        return "error/403";
    }

    @GetMapping("/404")
    public String error404() {
        return "error/404";
    }

    @GetMapping("/500")
    public String error500() {
        return "error/500";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin/admin";
    }

    @GetMapping("/admin/courses/new")
    public String adminCreateCourse() {
        return "admin/admin_create_course";
    }
}