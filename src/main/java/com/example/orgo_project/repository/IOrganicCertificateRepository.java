package com.example.orgo_project.repository;

import com.example.orgo_project.entity.OrganicCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrganicCertificateRepository extends JpaRepository<OrganicCertificate, Integer> {
    List<OrganicCertificate> findByProductId(Integer productId);
}
