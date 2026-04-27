package com.example.orgo_project.dto;

import com.example.orgo_project.entity.Expert;
import com.example.orgo_project.enums.ExpertStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpertDTO {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String expertiseField;
    private String certificateFile;
    private ExpertStatus status;
    private String experienceDescription;
    private LocalDateTime createdAt;
    
    public static ExpertDTO fromEntity(Expert expert) {
        ExpertDTO dto = new ExpertDTO();
        dto.setId(expert.getId());
        dto.setExpertiseField(expert.getExpertiseField());
        dto.setCertificateFile(expert.getCertificateFile());
        dto.setStatus(expert.getStatus());
        dto.setExperienceDescription(expert.getExperienceDescription());
        dto.setCreatedAt(expert.getCreatedAt());
        
        if (expert.getAccount() != null) {
            dto.setUsername(expert.getAccount().getUsername());
            if (expert.getAccount().getUser() != null) {
                dto.setFullName(expert.getAccount().getUser().getFullName());
                dto.setEmail(expert.getAccount().getUser().getEmail());
            }
        }
        return dto;
    }
}
