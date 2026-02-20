package com.example.final_year_project.entity.enums;

/**
 * 笔记审核状态
 */
public enum NoteStatus {
    /** 待审核 */
    PENDING,
    /** 已通过 */
    APPROVED,
    /** 已拒绝（含敏感词/违规） */
    REJECTED
}
