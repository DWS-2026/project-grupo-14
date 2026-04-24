package es.codeurjc.AcademiaElSoto.dto;

public class CommentResponseDto {

    private Long id;
    private String description;
    private String user;
    private String publicationDate;
    private Long courseId;
    private String courseName;

    public CommentResponseDto() {
    }

    public CommentResponseDto(Long id, String description, String user, String publicationDate,
            Long courseId, String courseName) {
        this.id = id;
        this.description = description;
        this.user = user;
        this.publicationDate = publicationDate;
        this.courseId = courseId;
        this.courseName = courseName;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getUser() {
        return user;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }
}