package com.uniai.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private  String firstName;
    private  String lastName;

    @Size(min = 2, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @Size(min = 2, max = 100)
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @Size(min = 2, max = 100)
    private String password;

    private boolean isVerified;
    private boolean isTwoFacAuth;
}
