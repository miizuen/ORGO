package com.example.orgo_project.repository;


import com.example.orgo_project.entity.Role;
import com.example.orgo_project.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Integer> {
    Role findByRoleName(RoleName rollName);
}
