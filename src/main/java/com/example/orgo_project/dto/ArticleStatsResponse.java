package com.example.orgo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleStatsResponse {
    private LocalDate date;
    private Integer views;
    private Integer likes;
}
