package dev.steady.steady.domain;

import dev.steady.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Steady steady;

    @Column(name = "is_leader", nullable = false)
    private boolean isLeader;

    @Builder
    private Participant(User user, Steady steady, boolean isLeader) {
        this.user = user;
        this.steady = steady;
        this.isLeader = isLeader;
    }

}
