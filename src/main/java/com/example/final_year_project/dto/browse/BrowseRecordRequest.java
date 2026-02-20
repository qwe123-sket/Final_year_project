package com.example.final_year_project.dto.browse;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BrowseRecordRequest {

    @NotNull(message = "Note ID is required")
    private Long noteId;

    /** 浏览时长（秒） */
    @NotNull
    @Min(0)
    private Long browseDurationSeconds;
}
