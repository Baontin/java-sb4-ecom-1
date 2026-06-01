package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductDTO createProduct(Long categoryId, ProductDTO productDTO);
    ProductResponse getAllProducts();
    ProductResponse getProductsByCategory(Long categoryId);
    ProductResponse getProductsByKeyword(String keyword);
    ProductDTO updateProduct(ProductDTO productDTO, Long productId);
    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;
    ProductDTO deleteProduct(Long productId);
}
