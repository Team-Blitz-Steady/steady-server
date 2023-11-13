package dev.steady.user.domain.repository;

import dev.steady.user.domain.User;
import dev.steady.user.domain.UserStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStackRepository extends JpaRepository<UserStack, Long> {

    List<UserStack> findAllByUser(User user);

    void deleteAllByUser(User user);

}
