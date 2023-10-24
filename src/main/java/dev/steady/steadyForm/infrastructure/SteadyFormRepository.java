package dev.steady.steadyForm.infrastructure;

import dev.steady.steadyForm.domain.SteadyForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SteadyFormRepository extends JpaRepository<SteadyForm, Long> {

}
