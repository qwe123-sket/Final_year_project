package com.example.final_year_project.dto.reply;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReplyCreateRequest {

    @NotBlank(message = "Reply content is required")
    @Size(max = 2000)
    private String content;
}
