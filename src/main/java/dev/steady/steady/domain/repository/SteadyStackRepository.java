package dev.steady.steady.domain.repository;

import dev.steady.steady.domain.SteadyStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SteadyStackRepository extends JpaRepository<SteadyStack, Long> {

    List<SteadyStack> findBySteadyId(Long id);

}
