package com.example.final_year_project.dto.user;

import com.example.final_year_project.entity.enums.UserRole;
import com.example.final_year_project.entity.enums.UserStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private UserRole role;
    private UserStatus status;
    private Instant createdAt;
}
