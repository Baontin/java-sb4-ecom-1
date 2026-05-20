package com.ecommerce.project.controller;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    /* Why categoryService field doesn't go with @Autowired
     * CategoryService is annotated with @Service.
     * Spring automatically registers it as a bean --> (Bean registration)
     *
     * (Dependency injection): when another class (here is CustomerController)
     * declares a dependency on CategoryService -> Spring looks in its context for a MATCHING BEAN
     *
     * EASY TO KNOW: (Bean registration) @Service tell Spring make CategoryService class as a bean
     * (Constructor injection) → tells Spring: “this controller needs that bean.”*/
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/public/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<String> createCategory(@Valid @RequestBody Category category) {
        categoryService.createCategory(category);
        return new ResponseEntity<>("Category added successfully!", HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {

        String status = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<String> updateCategory(@Valid
                                                 @RequestBody Category category,
                                                 @PathVariable Long categoryId) {

        Category savedCategory = categoryService.updateCategory(category, categoryId);
        return new ResponseEntity<>("Category with category id " + categoryId +
                                    " updated successfully!", HttpStatus.OK);
    }
}
