package com.example.orgo_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "KhachVangLai")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuestUser {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "sessionId")
    private String sessionId;
}
