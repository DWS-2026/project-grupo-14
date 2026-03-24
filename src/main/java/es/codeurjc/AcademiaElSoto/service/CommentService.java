package es.codeurjc.AcademiaElSoto.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.AcademiaElSoto.model.Comment;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.repository.CommentRepository;
import es.codeurjc.AcademiaElSoto.repository.CourseRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CourseRepository courseRepository;

    // Guardar un comentario nuevo asociado a un curso
    public void saveComment(Long courseId, String user, String description) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (courseOpt.isPresent()) {
            String cleanUser = user == null ? "" : user.trim();
            String cleanDescription = description == null ? "" : description.trim();

            if (!cleanUser.isEmpty() && !cleanDescription.isEmpty() && cleanDescription.length() <= 500) {
                Comment comment = new Comment();
                comment.setUser(cleanUser);
                comment.setDescription(cleanDescription);
                comment.setCourse(courseOpt.get());
                comment.setPublicationDate(LocalDateTime.now());

                commentRepository.save(comment);
            }
        }
    }

    // Obtener todos los comentarios
    public List<Comment> findAllComments() {
        return commentRepository.findAll();
    }

    // Buscar comentario por ID
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    // Eliminar comentario por ID
    public boolean deleteById(Long id) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isPresent()) {
            commentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Editar comentario existente
    public boolean editComment(Long id, Comment editedComment) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isPresent()) {
            Comment originalComment = commentOpt.get();

            String cleanUser = editedComment.getUser() == null ? "" : editedComment.getUser().trim();
            String cleanDescription = editedComment.getDescription() == null ? "" : editedComment.getDescription().trim();

            if (cleanUser.isEmpty() || cleanDescription.isEmpty() || cleanDescription.length() > 500) {
                return false;
            }

            originalComment.setUser(cleanUser);
            originalComment.setDescription(cleanDescription);
            

            commentRepository.save(originalComment);
            return true;
        }
        return false;
    }
}