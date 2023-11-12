package dev.steady.user.domain.repository;

import dev.steady.global.exception.NotFoundException;
import dev.steady.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import static dev.steady.user.exception.UserErrorCode.USER_NOT_FOUND;

public interface UserRepository extends JpaRepository<User, Long> {

    default User getUserBy(Long userId) {
        return findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }

    boolean existsByNickname(String nickname);

}
