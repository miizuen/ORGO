package com.example.orgo_project.service;

import com.example.orgo_project.entity.Role;
import com.example.orgo_project.enums.RoleName;

public interface IRoleService {
    void save(Role role);
    Role findByRollName(RoleName roleName);
}
