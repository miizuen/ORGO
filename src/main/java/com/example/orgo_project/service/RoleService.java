package com.example.orgo_project.service;

import com.example.orgo_project.entity.Role;
import com.example.orgo_project.enums.RoleName;
import com.example.orgo_project.repository.IRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService implements IRoleService{

    @Autowired
    private IRoleRepository roleRepository;

    @Override
    public void save(Role role) {
        roleRepository.save(role);
    }

    @Override
    public Role findByRollName(RoleName roleName) {
        return roleRepository.findByRoleName(roleName);
    }
}
