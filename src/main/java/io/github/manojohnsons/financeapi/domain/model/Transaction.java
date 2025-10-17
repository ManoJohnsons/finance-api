package io.github.manojohnsons.financeapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.github.manojohnsons.financeapi.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tab_transactions")
@NoArgsConstructor
@Getter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private String description;

    @Column(nullable = false)
    @Setter
    private BigDecimal amount;

    @Column(nullable = false)
    @Setter
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public Transaction(String description, BigDecimal amount, LocalDate date, TransactionType type, User user,
            Category category) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.user = user;
        this.category = category;
    }

    public void disassociateCategory() {
        this.category = null;
    }

    public void changeCategory(Category newCategory) {
        this.category = newCategory;
    }
}
