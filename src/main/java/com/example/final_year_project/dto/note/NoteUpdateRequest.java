package com.example.final_year_project.dto.note;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NoteUpdateRequest {

    @Size(max = 256)
    private String title;

    @Size(max = 10000)
    private String content;

    @Size(max = 5, message = "At most 5 tags")
    private List<String> tags;
}
