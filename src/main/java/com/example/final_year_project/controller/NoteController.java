package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.dto.note.NoteCreateRequest;
import com.example.final_year_project.dto.note.NoteUpdateRequest;
import com.example.final_year_project.dto.note.NoteVO;
import com.example.final_year_project.common.PageRequest;
import com.example.final_year_project.security.CurrentUser;
import com.example.final_year_project.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public Result<NoteVO> create(@Valid @RequestBody NoteCreateRequest req) {
        Long userId = CurrentUser.getUserId();
        return Result.ok(noteService.create(userId, req));
    }

    @PutMapping("/{id}")
    public Result<NoteVO> update(@PathVariable Long id, @Valid @RequestBody NoteUpdateRequest req) {
        Long userId = CurrentUser.getUserId();
        return Result.ok(noteService.update(userId, id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = CurrentUser.getUserId();
        noteService.delete(userId, id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<NoteVO> getById(@PathVariable Long id) {
        Long currentUserId = CurrentUser.getUserIdOrNull();
        NoteVO vo = noteService.getById(id, currentUserId);
        return Result.ok(vo);
    }

    @GetMapping("/list")
    public Result<Result.PageData<NoteVO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safeSize = PageRequest.clampSize(size);
        return Result.ok(noteService.listApproved(page, safeSize));
    }

    @GetMapping("/search")
    public Result<Result.PageData<NoteVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safeSize = PageRequest.clampSize(size);
        return Result.ok(noteService.search(keyword, page, safeSize));
    }

    @GetMapping("/my")
    public Result<Result.PageData<NoteVO>> myNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safeSize = PageRequest.clampSize(size);
        Long userId = CurrentUser.getUserId();
        return Result.ok(noteService.listByUser(userId, page, safeSize));
    }

    @PostMapping("/{id}/view")
    public Result<Void> recordView(@PathVariable Long id) {
        noteService.incrementViewCount(id);
        return Result.ok();
    }
}
