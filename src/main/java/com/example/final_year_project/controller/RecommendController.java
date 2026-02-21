package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.dto.recommend.RecommendItemVO;
import com.example.final_year_project.security.CurrentUser;
import com.example.final_year_project.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    // 未登录用户也能看推荐列表（此时 userId 传 0 走 fallback 逻辑）
    @GetMapping("/list")
    public Result<List<RecommendItemVO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long uid = CurrentUser.getUserIdOrNull();
        return Result.ok(recommendService.getRecommendList(uid != null ? uid : 0L, page, size));
    }
}
