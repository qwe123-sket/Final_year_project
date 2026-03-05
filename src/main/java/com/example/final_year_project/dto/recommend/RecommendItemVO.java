package com.example.final_year_project.dto.recommend;

import lombok.Data;

import java.util.List;

@Data
public class RecommendItemVO {

    private Long noteId;
    private String title;
    private String content;
    private String authorName;
    private String coverImage;
    private Long viewCount;
    private Long likeCount;
    private List<String> tags;
    private Double score;
    private String recallSource;
}
