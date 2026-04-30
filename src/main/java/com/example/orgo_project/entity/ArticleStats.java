package com.example.orgo_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "article_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;
    
    @Column(nullable = false)
    private Integer views = 0;
    
    @Column(nullable = false)
    private Integer likes = 0;
    
    @Column(nullable = false)
    private LocalDate date;
}
