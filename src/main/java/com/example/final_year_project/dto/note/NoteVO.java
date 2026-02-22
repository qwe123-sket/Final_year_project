package com.example.final_year_project.dto.note;

import com.example.final_year_project.entity.enums.NoteStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class NoteVO {

    private Long id;
    private Long userId;
    private String authorName;
    private String title;
    private String content;
    private String coverImage;
    private NoteStatus status;
    private String rejectReason;
    private Long viewCount;
    private Long likeCount;
    private Boolean liked;
    private Boolean favorited;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
}
