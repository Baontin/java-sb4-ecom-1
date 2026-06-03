package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long productId;

    @NotBlank
    @Size(min = 3, message = "Name must contain atleast 3 characters")
    private String productName;

    @NotBlank
    @Size(min = 6, message = "Description must contain atleast 6 characters")
    private String description;

    private String image;
    private Integer quantity;
    private Double price;
    private Double discount;
    private Double specialPrice;
}
