package com.example.final_year_project.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    private UserStatsVO stats;
    private List<DailyCount> dailyNotes;
    private List<DailyCount> dailyViews;
    private List<TagCount> tagDistribution;

    @Data
    @AllArgsConstructor
    public static class DailyCount {
        private String date;
        private long count;
    }

    @Data
    @AllArgsConstructor
    public static class TagCount {
        private String name;
        private long count;
    }
}
