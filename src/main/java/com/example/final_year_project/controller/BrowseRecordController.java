package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.dto.browse.BrowseRecordRequest;
import com.example.final_year_project.security.CurrentUser;
import com.example.final_year_project.service.BrowseRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/browse")
@RequiredArgsConstructor
public class BrowseRecordController {

    private final BrowseRecordService browseRecordService;

    @PostMapping("/record")
    public Result<Void> record(@Valid @RequestBody BrowseRecordRequest req) {
        Long userId = CurrentUser.getUserId();
        browseRecordService.record(userId, req);
        return Result.ok();
    }
}
