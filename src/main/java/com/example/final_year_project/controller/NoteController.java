package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.dto.note.NoteCreateRequest;
import com.example.final_year_project.dto.note.NoteUpdateRequest;
import com.example.final_year_project.dto.note.NoteVO;
import com.example.final_year_project.common.PageRequest;
import com.example.final_year_project.security.CurrentUser;
import com.example.final_year_project.service.NoteService;
import com.example.final_year_project.service.NoteLikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final NoteLikeService noteLikeService;

    @PostMapping
    public Result<NoteVO> create(@Valid @RequestBody NoteCreateRequest req) {
        return Result.ok(noteService.create(CurrentUser.getUserId(), req));
    }

    @PutMapping("/{id}")
    public Result<NoteVO> update(@PathVariable Long id, @Valid @RequestBody NoteUpdateRequest req) {
        return Result.ok(noteService.update(CurrentUser.getUserId(), id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noteService.delete(CurrentUser.getUserId(), id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<NoteVO> getById(@PathVariable Long id) {
        return Result.ok(noteService.getById(id, CurrentUser.getUserIdOrNull()));
    }

    @GetMapping("/list")
    public Result<Result.PageData<NoteVO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(noteService.listApproved(page, PageRequest.clampSize(size)));
    }

    @GetMapping("/search")
    public Result<Result.PageData<NoteVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(noteService.search(keyword, page, PageRequest.clampSize(size)));
    }

    @GetMapping("/my")
    public Result<Result.PageData<NoteVO>> myNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(noteService.listByUser(CurrentUser.getUserId(), page, PageRequest.clampSize(size)));
    }

    @PostMapping("/{id}/view")
    public Result<Void> recordView(@PathVariable Long id) {
        noteService.incrementViewCount(id);
        return Result.ok();
    }

    /* ---------- 点赞 ---------- */

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        noteLikeService.like(CurrentUser.getUserId(), id);
        return Result.ok();
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlike(@PathVariable Long id) {
        noteLikeService.unlike(CurrentUser.getUserId(), id);
        return Result.ok();
    }

    /* ---------- 热门排行 ---------- */

    @GetMapping("/trending")
    public Result<Result.PageData<NoteVO>> trending(
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(noteService.getTrending(period, page, PageRequest.clampSize(size)));
    }

    /* ---------- 热门标签 ---------- */

    @GetMapping("/tags/hot")
    public Result<List<Map<String, Object>>> hotTags(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(noteService.getHotTags(Math.min(limit, 50)));
    }
}
