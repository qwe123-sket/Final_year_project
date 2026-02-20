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

    /**
     * 个性化推荐列表（算法接口预留，当前为占位实现）
     */
    @GetMapping("/list")
    public Result<List<RecommendItemVO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = CurrentUser.getUserIdOrNull();
        List<RecommendItemVO> list = recommendService.getRecommendList(userId != null ? userId : 0L, page, size);
        return Result.ok(list);
    }
}
