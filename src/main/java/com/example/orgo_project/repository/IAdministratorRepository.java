package com.example.orgo_project.repository;

import com.example.orgo_project.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAdministratorRepository extends JpaRepository<Administrator, Integer> {
    Optional<Administrator> findFirstByUsernameOrderByIdAsc(String username);
}
