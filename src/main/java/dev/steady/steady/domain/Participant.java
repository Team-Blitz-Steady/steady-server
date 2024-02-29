package dev.steady.steady.domain;

import dev.steady.global.entity.BaseEntity;
import dev.steady.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "steady_id")
    private Steady steady;

    @Column(name = "is_leader", nullable = false)
    private boolean isLeader;

    private Participant(User user, Steady steady, boolean isLeader) {
        this.user = user;
        this.steady = steady;
        this.isLeader = isLeader;
    }

    public static Participant createLeader(User user, Steady steady) {
        return new Participant(user, steady, true);
    }

    public static Participant createMember(User user, Steady steady) {
        return new Participant(user, steady, false);
    }

    public void withdraw() {
        steady.getParticipants().getAllParticipants().remove(this);
        steady.updateNumberOfParticipants();
    }


    public Long getUserId() {
        return this.user.getId();
    }

}
