package dev.steady.review.domain;

import dev.steady.steady.domain.Steady;
import dev.steady.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id")
    private User reviewee;

    @ManyToOne(fetch = FetchType.LAZY)
    private Steady steady;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private boolean isPublic;

    @Builder
    private Review(User reviewer,
                   User reviewee,
                   Steady steady,
                   String comment,
                   boolean isPublic) {
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.steady = steady;
        this.comment = comment;
        this.isPublic = isPublic;
    }

}
