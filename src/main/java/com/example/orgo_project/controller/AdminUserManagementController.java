package com.example.orgo_project.controller;

import com.example.orgo_project.dto.UserManagementResponse;
import com.example.orgo_project.enums.UserStatus;
import com.example.orgo_project.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserManagementController {
    
    private final UserManagementService userManagementService;
    
    @GetMapping
    public ResponseEntity<Page<UserManagementResponse>> getAllUsers(
            @RequestParam(required = false) UserStatus status,
            Pageable pageable) {
        
        if (status != null) {
            return ResponseEntity.ok(userManagementService.getUsersByStatus(status, pageable));
        }
        return ResponseEntity.ok(userManagementService.getAllUsers(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserManagementResponse> getUserDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(userManagementService.getUserDetails(id));
    }
    
    @PutMapping("/{id}/ban")
    public ResponseEntity<Void> banUser(@PathVariable Integer id) {
        userManagementService.banUser(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable Integer id) {
        userManagementService.unbanUser(id);
        return ResponseEntity.ok().build();
    }
}
