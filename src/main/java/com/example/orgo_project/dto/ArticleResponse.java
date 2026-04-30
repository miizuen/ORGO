package com.example.orgo_project.dto;

import com.example.orgo_project.enums.ArticleStatus;
import com.example.orgo_project.enums.ArticleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleResponse {
    private Integer id;
    private Integer expertId;
    private String expertName;
    private String expertAvatar;
    private String readTime;
    private String title;
    private ArticleType type;
    private String content;
    private String summary;
    private ArticleStatus status;
    private String thumbnail;
    private String category;
    private String rejectNote;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private Integer totalViews;
    private Integer totalLikes;
    private List<Integer> productIds;
}
