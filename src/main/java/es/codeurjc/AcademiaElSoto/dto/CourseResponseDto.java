package es.codeurjc.AcademiaElSoto.dto;

import java.util.List;

public class CourseResponseDto {

    private Long id;
    private String courseName;
    private String teacher;
    private int price;
    private String description;
    private List<UserBasicDto> students;
    private List<Long> imageIds; 

    public CourseResponseDto() {
    }

    public CourseResponseDto(Long id, String courseName, String teacher, int price, String description, List<UserBasicDto> students) {
        this.id = id;
        this.courseName = courseName;
        this.teacher = teacher;
        this.price = price;
        this.description = description;
        this.students = students;
    }

    public List<Long> getImageIds() { return imageIds; }
    public void setImageIds(List<Long> imageIds) { this.imageIds = imageIds; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<UserBasicDto> getStudents() {
        return students;
    }

    public void setStudents(List<UserBasicDto> students) {
        this.students = students;
    }
}