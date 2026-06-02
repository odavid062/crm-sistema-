package com.crm.contact;

import com.crm.common.BaseTenantEntity;
import com.crm.company.Company;
import com.crm.tag.Tag;
import com.crm.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "contacts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Contact extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;
    private String whatsapp;
    private String cpf;
    private LocalDate birthDate;
    private String position;
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContactStatus status = ContactStatus.LEAD;

    private String source;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    @Builder.Default private String country = "Brasil";
    @Column(columnDefinition = "TEXT") private String notes;
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "contact_tags",
            joinColumns = @JoinColumn(name = "contact_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
