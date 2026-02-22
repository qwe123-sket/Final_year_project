package com.example.final_year_project.dto.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NoteCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 256)
    private String title;

    @Size(max = 10000)
    private String content;

    private String coverImage;

    @Size(max = 5, message = "At most 5 tags")
    private List<String> tags;
}
