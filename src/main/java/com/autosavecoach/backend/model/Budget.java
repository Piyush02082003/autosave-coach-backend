package com.autosavecoach.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.YearMonth;

@Entity
@Table(name = "budgets")
@Data
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Category category;

    private Double amount;

    @Column(name = "budget_month")
    private YearMonth month;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
