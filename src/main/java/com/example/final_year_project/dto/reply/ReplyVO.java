package com.example.final_year_project.dto.reply;

import lombok.Data;

import java.time.Instant;

@Data
public class ReplyVO {

    private Long id;
    private Long noteId;
    private Long userId;
    private String username;
    private String content;
    private Instant createdAt;
}
