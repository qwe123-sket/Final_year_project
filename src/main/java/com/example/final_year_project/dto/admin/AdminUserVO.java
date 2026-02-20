package com.example.final_year_project.dto.admin;

import com.example.final_year_project.entity.enums.UserRole;
import com.example.final_year_project.entity.enums.UserStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class AdminUserVO {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private UserRole role;
    private UserStatus status;
    private Instant createdAt;
    private long noteCount;
}
