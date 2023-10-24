package dev.steady.steady.application;

import dev.steady.steadyForm.domain.SteadyForm;
import dev.steady.steadyForm.infrastructure.SteadyFormRepository;
import dev.steady.global.auth.AuthContext;
import dev.steady.steady.domain.Participant;
import dev.steady.steady.domain.Promotion;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.infrastructure.SteadyRepository;
import dev.steady.user.domain.User;
import dev.steady.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SteadyService {

    private final static int INITIAL_PROMOTION_COUNT = 3;

    private final SteadyRepository steadyRepository;
    private final UserRepository userRepository;
    private final SteadyFormRepository steadyFormRepository;
    private final AuthContext authContext;

    @Transactional
    public Long create(SteadyCreateRequest request) {
        SteadyForm steadyForm = steadyFormRepository.findById(request.steadyFormId())
                .orElseThrow(() -> new IllegalArgumentException());
        Promotion promotion = new Promotion(INITIAL_PROMOTION_COUNT);
        Steady steady = request.toEntity(promotion, steadyForm);

        Long userId = authContext.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException());
        createSteadyLeader(steady, user);

        Steady saved = steadyRepository.save(steady);
        return saved.getId();
    }

    private void createSteadyLeader(Steady steady, User user) {
        Participant participant = Participant.builder()
                .user(user)
                .steady(steady)
                .isLeader(true)
                .build();
        steady.addParticipant(participant);
    }

}
