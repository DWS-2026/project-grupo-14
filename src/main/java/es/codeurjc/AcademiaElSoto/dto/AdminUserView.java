package es.codeurjc.AcademiaElSoto.dto;

public class AdminUserView {

    private Long id;
    private String fullName;
    private String email;
    private int totalPurchasedCourses;
    private String purchasedCourseNames;

    public AdminUserView(Long id, String fullName, String email, int totalPurchasedCourses, String purchasedCourseNames) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.totalPurchasedCourses = totalPurchasedCourses;
        this.purchasedCourseNames = purchasedCourseNames;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public int getTotalPurchasedCourses() {
        return totalPurchasedCourses;
    }

    public String getPurchasedCourseNames() {
        return purchasedCourseNames;
    }
}