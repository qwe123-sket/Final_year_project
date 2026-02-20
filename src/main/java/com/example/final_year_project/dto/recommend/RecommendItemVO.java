package com.example.final_year_project.dto.recommend;

import lombok.Data;

/**
 * 推荐列表项（算法侧返回笔记ID列表后，后端组装为笔记摘要）
 */
@Data
public class RecommendItemVO {

    private Long noteId;
    private String title;
    private String authorName;
    private Long viewCount;
    /** 可选：推荐分数等，由算法接口约定 */
    private Double score;
}
