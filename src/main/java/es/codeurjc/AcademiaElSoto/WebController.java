package es.codeurjc.AcademiaElSoto;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    /*
     @GetMapping("/route")
     public String methodName() {
         // When the user accesses http://localhost:8080/route
         // the browser opens view.html
         return "view";
     }
     */

    @GetMapping("/index")
    public String index() {
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

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/admin_create_course")
    public String adminCreateCourse() {
        return "admin_create_course";
    }

    @GetMapping("/admin_edit_course")
    public String adminEditCourse() {
        return "admin_edit_course";
    }

    /*
    @GetMapping("/admin_statistics")
    public String adminStatistics() {
        return "admin_statistics";
    }
    */

    @GetMapping("/admin_users")
    public String adminUsers() {
        return "admin_users";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }
}
