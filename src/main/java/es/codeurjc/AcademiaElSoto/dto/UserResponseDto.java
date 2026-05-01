package es.codeurjc.AcademiaElSoto.dto;

import java.util.List;

public class UserResponseDto {

    private Long id;
    private String userName;
    private String lastName;
    private String email;
    private List<String> roles;
    private List<CourseBasicDto> purchasedCourses; 

    public UserResponseDto() {
    }

    public UserResponseDto(Long id, String userName, String lastName, String email, 
                           List<String> roles, List<CourseBasicDto> purchasedCourses) {
        this.id = id;
        this.userName = userName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
        this.purchasedCourses = purchasedCourses;
    }

    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<CourseBasicDto> getPurchasedCourses() {
        return purchasedCourses;
    }
}