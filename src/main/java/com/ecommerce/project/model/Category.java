package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "categories")
/* @Data: A shortcut for
    @ToString,
    @EqualsAndHashCode,
    @Getter on all fields,
    @Setter on all non-final fields,
    and @RequiredArgsConstructor!
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank
    @Size(min = 5, message = "Category name must contain atleast 5 characters")
    @Column(unique = true)
    private String categoryName;

}
