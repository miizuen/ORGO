package com.example.orgo_project.service;

import com.example.orgo_project.entity.Product;
import com.example.orgo_project.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Optional<Product> getProductById(Integer id) {
        return productRepository.findById(id);
    }
    
    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }
}
