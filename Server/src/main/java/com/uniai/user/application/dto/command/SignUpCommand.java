package com.uniai.user.application.dto.command;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpCommand {

    @Size(min = 2, max = 50)
    private String username;

    private String firstName;
    private String lastName;

    @Size(min = 2, max = 100)
    private String email;

    @Size(min = 2, max = 100)
    private String password;
}
