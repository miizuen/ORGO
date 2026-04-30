package com.example.orgo_project.dto;

import com.example.orgo_project.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private String avatar;
}
