package dev.steady.steady.domain;

import dev.steady.steadyForm.domain.SteadyForm;
import dev.steady.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "steadies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Steady extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SteadyType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SteadyStatus status;

    @Column(name = "recruit_count", nullable = false)
    private int recruit_count;

    @Enumerated(EnumType.STRING)
    @Column(name = "steady_mode", nullable = false)
    private SteadyMode steadyMode;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Embedded
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    private SteadyForm steadyForm;

    @OneToMany(mappedBy = "steady", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    @Builder
    private Steady(String name,
                   SteadyType type,
                   SteadyStatus status,
                   int recruit_count,
                   SteadyMode steadyMode,
                   LocalDate openingDate,
                   LocalDate deadline,
                   String title,
                   String content,
                   Promotion promotion,
                   SteadyForm steadyForm) {
        this.name = name;
        this.type = type;
        this.status = status;
        this.recruit_count = recruit_count;
        this.steadyMode = steadyMode;
        this.openingDate = openingDate;
        this.deadline = deadline;
        this.title = title;
        this.content = content;
        this.promotion = promotion;
        this.steadyForm = steadyForm;
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }

}
