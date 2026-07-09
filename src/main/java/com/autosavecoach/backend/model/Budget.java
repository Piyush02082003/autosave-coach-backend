package com.autosavecoach.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "budgets")
@Data
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Category category;

    private Double amount;

    @Column(name = "budget_month")
    private YearMonth month;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
