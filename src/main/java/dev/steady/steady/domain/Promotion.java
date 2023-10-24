package dev.steady.steady.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EntityListeners;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Embeddable
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Promotion {

    @Column(name = "promotion_count", nullable = false)
    private int promotion_count;

    @CreatedDate
    @Column(name = "promoted_at", nullable = false)
    private LocalDateTime promoted_at;

    public Promotion(int promotion_count) {
        this.promotion_count = promotion_count;
    }

}
