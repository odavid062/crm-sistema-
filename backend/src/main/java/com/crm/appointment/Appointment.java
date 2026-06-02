package com.crm.appointment;

import com.crm.common.BaseTenantEntity;
import com.crm.contact.Contact;
import com.crm.deal.Deal;
import com.crm.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Appointment extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default private AppointmentType type = AppointmentType.MEETING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(nullable = false)
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Builder.Default private boolean allDay = false;

    @Column(columnDefinition = "TEXT") private String location;
    @Column(columnDefinition = "TEXT") private String meetingUrl;
    @Builder.Default private String color = "#6366f1";

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "contact_id")
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "deal_id")
    private Deal deal;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private User createdBy;

    private Integer reminderMinutes;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
