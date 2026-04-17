package es.codeurjc.AcademiaElSoto.dto;

public class CourseResponseDto {

    private Long id;
    private String courseName;
    private String teacher;
    private int price;
    private String description;
    private int students;

    public CourseResponseDto() {
    }

    public CourseResponseDto(Long id, String courseName, String teacher, int price, String description, int students) {
        this.id = id;
        this.courseName = courseName;
        this.teacher = teacher;
        this.price = price;
        this.description = description;
        this.students = students;
    }

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

    public int getStudents() {
        return students;
    }

    public void setStudents(int students) {
        this.students = students;
    }
}