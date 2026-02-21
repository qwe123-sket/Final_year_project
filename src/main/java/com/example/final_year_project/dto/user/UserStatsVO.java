package com.example.final_year_project.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsVO {

    private long notesPublished;
    private long totalViews;
    private long totalLikes;
    private long totalFavorited;
    private long favoritesCount;
}
