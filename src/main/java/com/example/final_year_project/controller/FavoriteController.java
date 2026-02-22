package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.dto.note.NoteVO;
import com.example.final_year_project.repository.NoteRepository;
import com.example.final_year_project.repository.UserRepository;
import com.example.final_year_project.security.CurrentUser;
import com.example.final_year_project.service.FavoriteService;
import com.example.final_year_project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final NotificationService notificationService;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @PostMapping("/{noteId}")
    public Result<Void> add(@PathVariable Long noteId) {
        Long userId = CurrentUser.getUserId();
        favoriteService.add(userId, noteId);
        // notify author
        noteRepository.findById(noteId).ifPresent(note -> {
            if (!note.getUserId().equals(userId)) {
                String fromName = userRepository.findById(userId)
                        .map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                        .orElse("Someone");
                notificationService.send(note.getUserId(), "FAVORITE",
                        fromName + " favorited your note \"" + note.getTitle() + "\"",
                        noteId, userId, fromName);
            }
        });
        return Result.ok();
    }

    @DeleteMapping("/{noteId}")
    public Result<Void> remove(@PathVariable Long noteId) {
        Long userId = CurrentUser.getUserId();
        favoriteService.remove(userId, noteId);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<Result.PageData<NoteVO>> myFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = CurrentUser.getUserId();
        return Result.ok(favoriteService.listMyFavorites(userId, page, size));
    }
}
