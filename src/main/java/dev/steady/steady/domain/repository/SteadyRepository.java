package dev.steady.steady.domain.repository;

import dev.steady.global.exception.NotFoundException;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.infrastructure.SteadyQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import static dev.steady.steady.exception.SteadyErrorCode.STEADY_NOT_FOUND;

public interface SteadyRepository extends JpaRepository<Steady, Long>, SteadyQueryRepository {

    default Steady getSteady(Long steadyId) {
        return findById(steadyId)
                .orElseThrow(() -> new NotFoundException(STEADY_NOT_FOUND));
    }

}
