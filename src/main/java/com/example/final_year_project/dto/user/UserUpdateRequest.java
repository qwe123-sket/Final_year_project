package com.example.final_year_project.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(max = 128)
    private String email;

    @Size(max = 64)
    private String nickname;

    @Size(max = 256)
    private String avatar;
}
