package dev.steady.steady.infrastructure;

import dev.steady.steady.domain.Steady;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SteadyRepository extends JpaRepository<Steady, Long> {

}
