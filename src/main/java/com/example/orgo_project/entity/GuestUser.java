package com.example.orgo_project.entity;

import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "sessionId", unique = true)
    private String sessionId;
}
