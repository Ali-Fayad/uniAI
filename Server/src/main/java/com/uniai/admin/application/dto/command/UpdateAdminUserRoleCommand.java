package com.uniai.admin.application.dto.command;

import com.uniai.user.domain.valueobject.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminUserRoleCommand {

    @NotNull
    private UserRole role;
}
