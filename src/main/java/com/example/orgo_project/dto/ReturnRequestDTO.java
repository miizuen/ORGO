package com.example.orgo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestDTO {
    private Integer orderId;
    private String reason;
    private String evidenceImage;
}