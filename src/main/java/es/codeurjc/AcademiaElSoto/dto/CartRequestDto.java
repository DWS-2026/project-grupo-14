package es.codeurjc.AcademiaElSoto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class CartRequestDto {

    @NotBlank(message = "Product name is required")
    private String product;

    @PositiveOrZero(message = "Price must be zero or positive")
    private int price;

    @NotNull(message = "User id is required")
    private Long userId;

    public CartRequestDto() {
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}