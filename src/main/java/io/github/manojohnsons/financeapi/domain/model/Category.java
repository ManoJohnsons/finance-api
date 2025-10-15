package io.github.manojohnsons.financeapi.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tab_categories")
@NoArgsConstructor
@Getter
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(name = "hex_color")
    @Setter
    private String hexColor;

    @Setter
    private String icon;

    @Column(name = "monthly_goal")
    @Setter
    private BigDecimal monthlyGoal;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Category(String name, String hexColor, String icon, User user) {
        this.name = name;
        this.hexColor = hexColor;
        this.icon = icon;
        this.user = user;
    }
}
