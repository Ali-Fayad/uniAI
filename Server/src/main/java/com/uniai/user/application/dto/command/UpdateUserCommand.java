package com.uniai.user.application.dto.command;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserCommand {

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Username may contain only letters, numbers, and underscores")
    private String username;

    @Size(max = 100)
    @Pattern(regexp = "^\\S(?:.*\\S)?$", message = "First name must not have leading or trailing whitespace")
    private String firstName;

    @Size(max = 100)
    @Pattern(regexp = "^\\S(?:.*\\S)?$", message = "Last name must not have leading or trailing whitespace")
    private String lastName;

    @Email
    @Size(max = 100)
    private String email;

    private Boolean enableTwoFactor;
}
