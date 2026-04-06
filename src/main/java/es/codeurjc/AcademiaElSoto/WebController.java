package es.codeurjc.AcademiaElSoto;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    /**
     * Displays the main home page.
     */
    @GetMapping("/index")
    public String index() {
        return "index";
    }

    /**
     * Redirect root access to the home page view.
     */
    @GetMapping("/")
    public String barra() {
        return "index";
    }

    /**
     * Displays the teachers page.
     */
    @GetMapping("/teachers")
    public String teachers() {
        return "teachers";
    }

    /**
     * Displays the information page.
     */
    @GetMapping("/information")
    public String information() {
        return "information";
    }

    /**
     * Displays the custom 403 error page.
     */
    @GetMapping("/403")
    public String error403() {
        return "error/403";
    }

    /**
     * Displays the custom 404 error page.
     */
    @GetMapping("/404")
    public String error404() {
        return "error/404";
    }

    /**
     * Displays the custom 500 error page.
     */
    @GetMapping("/500")
    public String error500() {
        return "error/500";
    }

    /**
     * Displays the main admin panel page.
     */
    @GetMapping("/admin")
    public String admin() {
        return "admin/admin";
    }

    /**
     * Displays the page to create a new course from the admin panel.
     */
    @GetMapping("/admin/courses/new")
    public String adminCreateCourse() {
        return "admin/admin_create_course";
    }
}