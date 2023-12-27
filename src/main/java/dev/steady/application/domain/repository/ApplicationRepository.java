package dev.steady.application.domain.repository;

import dev.steady.application.domain.Application;
import dev.steady.application.domain.ApplicationStatus;
import dev.steady.global.exception.NotFoundException;
import dev.steady.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static dev.steady.application.exception.ApplicationErrorCode.APPLICATION_NOT_FOUND;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    default Application getById(Long applicationId) {
        return findById(applicationId)
                .orElseThrow(() -> new NotFoundException(APPLICATION_NOT_FOUND));
    }

    Slice<Application> findAllByUser(User user, Pageable pageable);

    Optional<Application> findBySteadyIdAndUserId(Long steadyId, Long userId);

    Optional<Application> findBySteadyIdAndUserIdAndStatus(Long steadyId, Long userId, ApplicationStatus status);

    Slice<Application> findAllBySteadyIdAndStatus(Long steadyId, ApplicationStatus status, Pageable pageable);

}
