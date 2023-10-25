package dev.steady.steady.service;

import dev.steady.global.auth.AuthContext;
import dev.steady.steady.domain.Participant;
import dev.steady.steady.domain.Promotion;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyQuestion;
import dev.steady.steady.domain.repository.SteadyQuestionRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.user.domain.User;
import dev.steady.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SteadyService {


    private final SteadyRepository steadyRepository;
    private final UserRepository userRepository;
    private final SteadyQuestionRepository steadyQuestionRepository;
    private final AuthContext authContext;

    @Transactional
    public Long create(SteadyCreateRequest request) {
        Promotion promotion = new Promotion();
        Steady steady = request.toEntity(promotion);

        Long userId = authContext.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException());
        createSteadyLeader(steady, user);
        Steady savedSteady = steadyRepository.save(steady);

        List<SteadyQuestion> steadyQuestions = createSteadyQuestions(request.questions(), savedSteady);
        steadyQuestionRepository.saveAll(steadyQuestions);
        return savedSteady.getId();
    }

    private void createSteadyLeader(Steady steady, User user) {
        Participant participant = Participant.builder()
                .user(user)
                .steady(steady)
                .isLeader(true)
                .build();
        steady.addParticipant(participant);
    }

    private List<SteadyQuestion> createSteadyQuestions(List<String> questions, Steady steady) {
        return IntStream.range(0, questions.size())
                .mapToObj(index -> SteadyQuestion.builder()
                        .content(questions.get(index))
                        .order(index + 1)
                        .steady(steady)
                        .build())
                .toList();
    }

}