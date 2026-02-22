package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.dto.reply.ReplyCreateRequest;
import com.example.final_year_project.dto.reply.ReplyVO;
import com.example.final_year_project.repository.NoteRepository;
import com.example.final_year_project.repository.UserRepository;
import com.example.final_year_project.security.CurrentUser;
import com.example.final_year_project.service.NotificationService;
import com.example.final_year_project.service.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes/{noteId}/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;
    private final NotificationService notificationService;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @PostMapping
    public Result<ReplyVO> create(@PathVariable Long noteId, @Valid @RequestBody ReplyCreateRequest req) {
        Long userId = CurrentUser.getUserId();
        ReplyVO vo = replyService.create(userId, noteId, req);
        // notify note author
        noteRepository.findById(noteId).ifPresent(note -> {
            if (!note.getUserId().equals(userId)) {
                String fromName = userRepository.findById(userId)
                        .map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                        .orElse("Someone");
                notificationService.send(note.getUserId(), "REPLY",
                        fromName + " replied to your note \"" + note.getTitle() + "\"",
                        noteId, userId, fromName);
            }
        });
        return Result.ok(vo);
    }

    @GetMapping
    public Result<List<ReplyVO>> list(
            @PathVariable Long noteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(replyService.listByNote(noteId, page, size));
    }
}
