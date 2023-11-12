package dev.steady.auth.domain;

import dev.steady.auth.exception.DuplicateRequestException;
import dev.steady.global.entity.BaseEntity;
import dev.steady.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static dev.steady.auth.exception.AuthErrorCode.ACCOUNT_USER_ALREADY_REGISTERED;

@Getter
@Entity
@Table(name = "accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(nullable = false)
    private String platformId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Account(Platform platform, String platformId) {
        this.platform = platform;
        this.platformId = platformId;
    }

    public boolean hasNoUser() {
        return this.user == null;
    }

    public void registerUser(User user) {
        if (this.hasNoUser()) {
            this.user = user;
        } else {
            throw new DuplicateRequestException(ACCOUNT_USER_ALREADY_REGISTERED);
        }
    }

}
