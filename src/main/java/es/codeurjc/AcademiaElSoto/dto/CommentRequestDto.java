package es.codeurjc.AcademiaElSoto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CommentRequestDto {

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private String user;

    @NotNull(message = "Course id is required")
    private Long courseId;

    public CommentRequestDto() {
    }

    public String getDescription() {
        return description;
    }

    public String getUser() {
        return user;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}