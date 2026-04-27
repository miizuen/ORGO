package com.example.orgo_project.dto;

import com.example.orgo_project.enums.ArticleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ArticleRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotNull(message = "Type is required")
    private ArticleType type;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String thumbnail;
    
    private String category;
    
    private List<Long> productIds;
}
