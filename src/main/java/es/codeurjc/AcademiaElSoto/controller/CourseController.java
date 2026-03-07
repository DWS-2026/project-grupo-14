package es.codeurjc.AcademiaElSoto.controller;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.AcademiaElSoto.model.Curso;
import es.codeurjc.AcademiaElSoto.repository.cursoRepository;
import es.codeurjc.AcademiaElSoto.service.CourseService;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import jakarta.annotation.PostConstruct;

@Controller
public class CourseController {

    @Autowired
    private cursoRepository cursoRepository;

    @Autowired
    private CourseService courseService;

    //Datos temporales para probar que funciona 

    @PostConstruct
    public void init() {
        cursoRepository.save(new Curso("Paul", "IP", 120, "Curso de introducción a Programación"));
        cursoRepository.save(new Curso("Frenando", "DWS", 150, "Desarrollo de aplicaciones con Spring"));
    }

    //Mostrar todos los cursos
    @GetMapping("/cursos")
    public String showCourses(Model model) {

        model.addAttribute("cursos", cursoRepository.findAll());

        return "cursos";
    }

    //Mostrar un curso en detalle
    @GetMapping("/curso/{id}")
    public String showCourse(Model model, @PathVariable long id) {


        //En Java, Optional<T> es una clase contenedora que puede o no contener un valor del tipo T.

        Optional<Curso> op = cursoRepository.findById(id); //Con lo de Optional a veces devuelve un curso o a veces null si no hay ninguno.

        if (op.isPresent()) {
            Curso curso = op.get();
            model.addAttribute("curso", curso);
            model.addAttribute("hasImage", curso.getImageFile() != null);
            return "show_course";
        } else {
            return "bd_course/course_not_found";
        }
    }

    //Para descargar la imagen

    @GetMapping("/curso/{id}/image")
    public ResponseEntity<Object> downloadImage(@PathVariable long id) throws SQLException {

        Optional<Curso> op = cursoRepository.findById(id);

        if (op.isPresent() && op.get().getImageFile() != null) {

            Blob image = op.get().getImageFile();
            Resource imageFile = new InputStreamResource(image.getBinaryStream());

            MediaType mediaType = MediaTypeFactory
                    .getMediaType(imageFile)
                    .orElse(MediaType.IMAGE_JPEG);

            return ResponseEntity
                    .ok()
                    .contentType(mediaType)
                    .body(imageFile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/curso/new")
    public String newCourse(Model model, Curso curso, @RequestParam(required = false) MultipartFile image) {
    try {
        courseService.save(curso, image);
        model.addAttribute("curso", curso);
        model.addAttribute("hasImage", image != null && !image.isEmpty());
        return "bd_course/saved_course";
    } catch (IOException e) {
        model.addAttribute("error", "Error al guardar la imagen: " + e.getMessage());
        return "bd_course/course_not_found"; 
    }
}


    @GetMapping("/editcurso/{id}")
    public String editCourse(Model model, @PathVariable long id) {

        Optional<Curso> op = cursoRepository.findById(id);

        if (op.isPresent()) {
            Curso curso = op.get();
            model.addAttribute("curso", curso);
            model.addAttribute("hasImage", curso.getImageFile() != null);
            return "bd_course/edit_course_page";
        } else {
            return "bd_course/course_not_found";
        }
    }

    @PostMapping("/editcurso")
    public String editCourseProcess(Model model, Curso editedCurso, @RequestParam(required = false) MultipartFile image) {
        try {
            courseService.save(editedCurso, image);
            model.addAttribute("curso", editedCurso);
            model.addAttribute("hasImage", editedCurso.getImageFile() != null);
            return "bd_course/edited_course";
        } catch (IOException e) {
            model.addAttribute("error", "Error al guardar la imagen: " + e.getMessage());
            return "bd_course/error";
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


