package dev.steady.steadyForm.domain;

import dev.steady.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "forms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SteadyForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    private SteadyForm(String name, User user) {
        this.name = name;
        this.user = user;
    }

}
