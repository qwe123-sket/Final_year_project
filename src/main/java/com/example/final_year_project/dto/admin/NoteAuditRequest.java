package com.example.final_year_project.dto.admin;

import com.example.final_year_project.entity.enums.NoteStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoteAuditRequest {

    @NotNull(message = "Audit status is required")
    private NoteStatus status;

    /** 拒绝时填写原因（敏感词/违规说明） */
    @Size(max = 512)
    private String rejectReason;
}
