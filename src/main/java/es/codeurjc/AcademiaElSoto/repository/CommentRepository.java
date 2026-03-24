package es.codeurjc.AcademiaElSoto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.AcademiaElSoto.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByCourseIdOrderByPublicationDateDesc(Long courseId);

    List<Comment> findByUser(String user);
}
