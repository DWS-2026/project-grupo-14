package es.codeurjc.AcademiaElSoto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.AcademiaElSoto.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Returns all comments that belong to a specific course,
     * ordered by publication date from newest to oldest.
     */
    List<Comment> findByCourseIdOrderByPublicationDateDesc(Long courseId);

    /**
     * Returns all comments written by a specific user.
     */
    List<Comment> findByUser(String user);
}