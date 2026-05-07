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

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}