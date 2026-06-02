package com.crm.notification;

import com.crm.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Notificações do sistema")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    @Operation(summary = "Listar minhas notificações")
    public ResponseEntity<Page<Notification>> myNotifications(Pageable pageable) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Contagem de não lidas")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long count = notificationRepository.countByUserIdAndReadAtIsNull(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar notificação como lida")
    public ResponseEntity<Notification> markRead(@PathVariable UUID id) {
        return notificationRepository.findById(id).map(n -> {
            n.setReadAt(LocalDateTime.now());
            return ResponseEntity.ok(notificationRepository.save(n));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marcar todas como lidas")
    public ResponseEntity<Void> markAllRead() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), Pageable.unpaged())
                .forEach(n -> {
                    if (n.getReadAt() == null) {
                        n.setReadAt(LocalDateTime.now());
                        notificationRepository.save(n);
                    }
                });
        return ResponseEntity.ok().build();
    }
}
