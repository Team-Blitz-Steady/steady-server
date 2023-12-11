package dev.steady.steady.domain;

import dev.steady.global.entity.BaseEntity;
import dev.steady.global.exception.ForbiddenException;
import dev.steady.global.exception.InvalidStateException;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static dev.steady.steady.exception.SteadyErrorCode.ALREADY_FINISHED;
import static dev.steady.steady.exception.SteadyErrorCode.LEADER_PERMISSION_NEEDED;

@Entity
@Getter
@Table(name = "steadies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Steady extends BaseEntity {

    private static final long REVIEW_POLICY = 2L;
    private static final int DEFAULT_VIEW_COUNT = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String bio;

    @Column(nullable = false)
    private String contact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SteadyType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SteadyStatus status;

    @Column(nullable = false)
    private int numberOfParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SteadyMode steadyMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduledPeriod scheduledPeriod;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private int viewCount;

    private int likeCount;

    @Embedded
    private Promotion promotion;

    @Embedded
    private Participants participants;

    @OneToMany(mappedBy = "steady", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SteadyStack> steadyStacks = new ArrayList<>();

    private LocalDate finishedAt;

    @Version
    private int version;

    @Builder
    private Steady(String name,
                   String bio,
                   String contact,
                   SteadyType type,
                   int participantLimit,
                   SteadyMode steadyMode,
                   ScheduledPeriod scheduledPeriod,
                   LocalDate deadline,
                   String title,
                   String content,
                   User user,
                   List<Stack> stacks) {
        this.promotion = new Promotion();
        this.participants = createParticipants(user, participantLimit);
        this.numberOfParticipants = participants.getNumberOfParticipants();
        this.name = name;
        this.bio = bio;
        this.contact = contact;
        this.type = type;
        this.status = SteadyStatus.RECRUITING;
        this.steadyMode = steadyMode;
        this.scheduledPeriod = scheduledPeriod;
        this.deadline = deadline;
        this.title = title;
        this.content = content;
        this.viewCount = DEFAULT_VIEW_COUNT;
        this.likeCount = DEFAULT_LIKE_COUNT;
        this.steadyStacks = createSteadyStack(stacks);
    }

    public void update(User user,
                       String name,
                       String bio,
                       String contact,
                       SteadyType type,
                       SteadyStatus status,
                       int participantLimit,
                       SteadyMode steadyMode,
                       String scheduledPeriod,
                       LocalDate deadline,
                       String title,
                       String content,
                       List<Stack> stacks) {
        validateLeader(user);
        this.name = name;
        this.bio = bio;
        this.contact = contact;
        this.type = type;
        this.status = status;
        this.participants.updateParticipantLimit(participantLimit);
        this.steadyMode = steadyMode;
        this.scheduledPeriod = ScheduledPeriod.valueOf(scheduledPeriod);
        this.deadline = deadline;
        this.title = title;
        this.content = content;
        this.steadyStacks.clear();
        this.steadyStacks.addAll(createSteadyStack(stacks));
    }

    public void validateLeader(User user) {
        if (!isLeader(user)) {
            throw new ForbiddenException(LEADER_PERMISSION_NEEDED);
        }
    }

    public boolean isLeader(User user) {
        User leader = participants.getLeader();
        return leader.equals(user);
    }

    public void addParticipantByLeader(User leader, User member) {
        validateLeader(leader);
        participants.add(Participant.createMember(member, this));
        numberOfParticipants = participants.getNumberOfParticipants();
    }

    public void expelParticipantByLeader(User leader, Participant participant) {
        validateLeader(leader);
        if (finishedAt != null || isFinished()) {
            throw new InvalidStateException(ALREADY_FINISHED);
        }
        participant.delete();
        numberOfParticipants = participants.getNumberOfParticipants();
    }

    public void usePromotion(User user) {
        validateLeader(user);
        promotion.use();
    }

    public void finish(User user) {
        validateLeader(user);
        if (finishedAt != null) {
            throw new InvalidStateException(ALREADY_FINISHED);
        }
        this.status = SteadyStatus.FINISHED;
        this.finishedAt = LocalDate.now();
    }

    public boolean isReviewEnabled() {
        if (finishedAt == null || !isFinished()) {
            return false;
        }
        return finishedAt.plusMonths(REVIEW_POLICY).isAfter(LocalDate.now());
    }

    public int getPromotionCount() {
        return promotion.getPromotionCount();
    }

    public int getParticipantLimit() {
        return participants.getParticipantLimit();
    }

    public boolean isDeletable(User user) {
        validateLeader(user);
        return numberOfParticipants == 1;
    }

    public User getLeader() {
        return participants.getLeader();
    }

    public boolean isFinished() {
        return this.status == SteadyStatus.FINISHED;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount--;
    }

    private Participants createParticipants(User user, int participantLimit) {
        Participants participants = new Participants(participantLimit);
        participants.add(Participant.createLeader(user, this));
        return participants;
    }

    private List<SteadyStack> createSteadyStack(List<Stack> stacks) {
        return stacks.stream()
                .map(stack -> new SteadyStack(stack, this))
                .toList();
    }

}
