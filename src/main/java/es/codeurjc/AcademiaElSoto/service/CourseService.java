package es.codeurjc.AcademiaElSoto.service;

import java.io.IOException;
import java.sql.Blob;
import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.AcademiaElSoto.model.Curso;
import es.codeurjc.AcademiaElSoto.repository.cursoRepository;

@Service
public class CourseService {

    @Autowired
    private cursoRepository cursoRepository;

    // Guardar o actualizar un curso sin imagen
    public void save(Curso curso) {
        cursoRepository.save(curso);
    }

    // Guardar o actualizar un curso con imagen (Blob)
    public void save(Curso curso, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Blob blob = new SerialBlob(imageFile.getBytes());
                curso.setImageFile(blob);
            } catch (Exception e) {
                throw new IOException("Error al crear el Blob de la imagen", e);
            }
        }
        this.save(curso);
    }

    // Buscar todos los cursos
    public List<Curso> findAll() {
        return cursoRepository.findAll();
    }

    // Buscar por id
    public Optional<Curso> findById(Long id) {
        return cursoRepository.findById(id);
    }

    // Buscar por profesor
    public List<Curso> findByProfesor(String profesor) {
        return cursoRepository.findByProfesor(profesor);
    }

    // Buscar por nombre de curso
    public List<Curso> findByNombreCurso(String nombreCurso) {
        return cursoRepository.findByNombreCurso(nombreCurso);
    }

    // Eliminar un curso por id
    public void deleteById(Long id) {
        cursoRepository.deleteById(id);
    }

}
