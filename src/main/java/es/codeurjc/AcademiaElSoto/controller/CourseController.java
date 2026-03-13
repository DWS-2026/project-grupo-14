package es.codeurjc.AcademiaElSoto.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import es.codeurjc.AcademiaElSoto.model.Curso;
import es.codeurjc.AcademiaElSoto.repository.cursoRepository;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.rowset.serial.SerialBlob;

import org.springframework.http.MediaType;


import jakarta.annotation.PostConstruct;

@Controller
public class CourseController {

    @Autowired
    private cursoRepository cursoRepository;

    //Datos temporales para probar que funciona 

    @PostConstruct
    public void init() {
        cursoRepository.save(new Curso("Paul", "IP", 120, "Curso de introducción a Programación",12));
        cursoRepository.save(new Curso("Frenando", "DWS", 150, "Desarrollo de aplicaciones con Spring",13));
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
            editedCurso.setId(id);   // aseguramos el id correcto
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
}
