package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Data
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Зашифрованный номер
    @Column(name = "card_number_encrypted")
    private String cardNumberEncrypted;

    // Маскированный номер для отображения
    @Column(name = "card_number_masked")
    private String cardNumberMasked;

    @NotBlank
    private String cardHolderName;

    @NotNull
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.ACTIVE;

    @NotNull
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CardStatus {
        ACTIVE, BLOCKED, EXPIRED
    }
}