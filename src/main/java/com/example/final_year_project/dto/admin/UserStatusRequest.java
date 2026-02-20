package com.example.final_year_project.dto.admin;

import com.example.final_year_project.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusRequest {

    @NotNull(message = "User status is required")
    private UserStatus status;
}
