package com.fintracker_backend.fintracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


import org.hibernate.annotations.CreationTimestamp;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN, CUSTOMER

    @CreationTimestamp
    private LocalDateTime createdAt;
}
