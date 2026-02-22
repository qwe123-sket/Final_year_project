package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.entity.Notification;
import com.example.final_year_project.security.CurrentUser;
import com.example.final_year_project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notifService;

    @GetMapping
    public Result<List<Notification>> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        Long userId = CurrentUser.getUserId();
        return Result.ok(notifService.getRecent(userId, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        Long userId = CurrentUser.getUserId();
        return Result.ok(Map.of("count", notifService.getUnreadCount(userId)));
    }

    @PostMapping("/read-all")
    public Result<Void> readAll() {
        Long userId = CurrentUser.getUserId();
        notifService.markAllRead(userId);
        return Result.ok();
    }
}
