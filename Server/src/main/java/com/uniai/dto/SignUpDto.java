package com.uniai.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpDto {
    @Size(min = 2, max = 50)
    private String username;

    private  String firstName;
    private  String lastName;

    @Size(min = 2, max = 100)
    private String email;

    @Size(min = 2, max = 100)
    private String password;
}
