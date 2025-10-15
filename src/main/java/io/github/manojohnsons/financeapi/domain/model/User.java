package io.github.manojohnsons.financeapi.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tab_users")
@NoArgsConstructor
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(nullable = false, unique = true)
    /* No @Setter here to guarantee immutability in the email field, strictly following the project design.
    * But maybe in the future, having a functionality to change the email might be a good idea, despite the complexity.
    */
    private String email;

    @Column(nullable = false)
    @Setter
    private String password;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
