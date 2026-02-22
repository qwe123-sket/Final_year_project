package com.example.final_year_project.service;

import com.example.final_year_project.entity.Notification;
import com.example.final_year_project.repository.NotificationRepository;
import com.example.final_year_project.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notifRepo;
    private final NotificationWebSocketHandler wsHandler;

    // 存库并推送给在线用户
    public void send(Long toUserId, String type, String content,
                     Long relatedNoteId, Long fromUserId, String fromUsername) {
        Notification notif = Notification.builder()
                .userId(toUserId)
                .type(type)
                .content(content)
                .relatedNoteId(relatedNoteId)
                .fromUserId(fromUserId)
                .fromUsername(fromUsername)
                .build();
        notifRepo.save(notif);

        try {
            String json = String.format(
                    "{\"id\":%d,\"type\":\"%s\",\"content\":\"%s\",\"relatedNoteId\":\"%s\",\"fromUsername\":\"%s\",\"createdAt\":\"%s\"}",
                    notif.getId(),
                    escapeJson(type),
                    escapeJson(content),
                    relatedNoteId != null ? relatedNoteId : "",
                    escapeJson(fromUsername != null ? fromUsername : ""),
                    notif.getCreatedAt().toString()
            );
            wsHandler.sendToUser(toUserId, json);
        } catch (Exception e) {
            log.warn("Failed to push WS notification: {}", e.getMessage());
        }
    }

    public List<Notification> getRecent(Long userId, int page, int size) {
        Page<Notification> p = notifRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return p.getContent();
    }

    public long getUnreadCount(Long userId) {
        return notifRepo.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notifRepo.markAllRead(userId);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        // 简单转义，不处理 \r \t 等
    }
}
