package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// (Bean registration): this annotation help it automatically considered as a spring managed component.
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // extend jpaRepo to use pre-built method inside JPA.
    // <Category, Long> said that will help us interact with Category in DB.


    /* Note: findByCategoryName works because:
        * findBy → tells Spring Data JPA it's a query
        * CategoryName → must match the field name in your Category entity (camelCase)*/
    Category findByCategoryName(String categoryName);
}
