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

/**
 * Service layer for comment-related operations.
 * This class handles creation, retrieval, update,
 * and deletion of comments.
 */
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Saves a new comment associated with a specific course.
     * The method validates the user name and description before saving.
     *
     * @param courseId the id of the course the comment belongs to
     * @param user the username of the comment author
     * @param description the comment text
     */
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

    /**
     * Retrieves all comments from the database.
     *
     * @return a list of all comments
     */
    public List<Comment> findAllComments() {
        return commentRepository.findAll();
    }

    /**
     * Finds a comment by its id.
     *
     * @param id comment identifier
     * @return an Optional containing the comment if found
     */
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    /**
     * Deletes a comment by its id if it exists.
     *
     * @param id comment identifier
     * @return true if the comment was deleted, false otherwise
     */
    public boolean deleteById(Long id) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isPresent()) {
            commentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Updates an existing comment if it exists and the new values are valid.
     *
     * @param id comment identifier
     * @param editedComment object containing the new comment data
     * @return true if the comment was updated successfully, false otherwise
     */
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