package es.codeurjc.AcademiaElSoto.dto;

import java.util.List;

public class CartResponseDto {

    private Long id;
    private String product;
    private int price;
    private String userName; 
    private List<CourseResponseDto> courses;

    public CartResponseDto() {
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public List<CourseResponseDto> getCourses() { return courses; }
    public void setCourses(List<CourseResponseDto> courses) { this.courses = courses; }
}