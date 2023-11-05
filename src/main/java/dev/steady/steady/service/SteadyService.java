package dev.steady.steady.service;

import dev.steady.application.domain.repository.ApplicationRepository;
import dev.steady.global.auth.UserInfo;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyPosition;
import dev.steady.steady.domain.SteadyQuestion;
import dev.steady.steady.domain.repository.SteadyPositionRepository;
import dev.steady.steady.domain.repository.SteadyQuestionRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.steady.dto.SearchConditionDto;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.dto.request.SteadyUpdateRequest;
import dev.steady.steady.dto.response.PageResponse;
import dev.steady.steady.dto.response.SteadyDetailResponse;
import dev.steady.steady.dto.response.SteadySearchResponse;
import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

import static dev.steady.application.domain.ApplicationStatus.WAITING;

@Service
@RequiredArgsConstructor
public class SteadyService {

    private final UserRepository userRepository;
    private final StackRepository stackRepository;
    private final SteadyRepository steadyRepository;
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final SteadyQuestionRepository steadyQuestionRepository;
    private final SteadyPositionRepository steadyPositionRepository;

    @Transactional
    public Long create(SteadyCreateRequest request, UserInfo userinfo) {
        Long userId = userinfo.userId();
        User user = userRepository.getUserBy(userId);
        List<Stack> stacks = getStacks(request.stacks());
        Steady steady = request.toEntity(user, stacks);
        Steady savedSteady = steadyRepository.save(steady);

        List<SteadyPosition> steadyPositions = createSteadyPositions(request.positions(), savedSteady);
        steadyPositionRepository.saveAll(steadyPositions);

        List<SteadyQuestion> steadyQuestions = createSteadyQuestions(request.questions(), savedSteady);
        steadyQuestionRepository.saveAll(steadyQuestions);

        return savedSteady.getId();
    }

    @Transactional(readOnly = true)
    public PageResponse<SteadySearchResponse> getSteadies(Pageable pageable) {
        Page<Steady> steadies = steadyRepository.findAll(pageable);
        Page<SteadySearchResponse> searchResponses = steadies.map(SteadySearchResponse::from);
        return PageResponse.from(searchResponses);
    }

    @Transactional(readOnly = true)
    public PageResponse<SteadySearchResponse> getSteadies(SearchConditionDto conditionDto, Pageable pageable) {
        Page<Steady> steadies = steadyRepository.findAllBySearchCondition(conditionDto, pageable);
        Page<SteadySearchResponse> searchResponses = steadies.map(SteadySearchResponse::from);
        return PageResponse.from(searchResponses);
    }

    @Transactional(readOnly = true)
    public SteadyDetailResponse getDetailSteady(Long steadyId, UserInfo userInfo) {
        Steady steady = steadyRepository.getSteady(steadyId);
        List<SteadyPosition> positions = steadyPositionRepository.findBySteadyId(steady.getId());

        boolean isLeader = false;
        boolean isWaitingApplication = false;

        if (userInfo.isAuthenticated()) {
            User user = userRepository.getUserBy(userInfo.userId());
            isLeader = steady.isLeader(user);

            if (!isLeader) {
                isWaitingApplication = isWaitingApplication(user, steady);
            }
        }

        return SteadyDetailResponse.of(steady, positions, isLeader, isWaitingApplication);
    }

    @Transactional
    public Long updateSteady(Long steadyId, UserInfo userInfo, SteadyUpdateRequest request) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        steady.validateLeader(user);
        List<Stack> stacks = getStacks(request.stacks());
        steady.update(request.name(),
                request.bio(),
                request.type(),
                request.status(),
                request.participantLimit(),
                request.steadyMode(),
                request.scheduledPeriod(),
                request.deadline(),
                request.title(),
                request.content(),
                stacks);
        return steadyId;
    }

    @Transactional
    public void promoteSteady(Long steadyId, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        steady.validateLeader(user);
        steady.usePromotion();
    }

    @Transactional
    public void finishSteady(Long steadyId, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        steady.validateLeader(user);
        steady.finish();
    }

    @Transactional
    public void deleteSteady(Long steadyId, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        if (steady.isLeader(user) && !steady.hasParticipants()) {
            steadyPositionRepository.deleteBySteadyId(steadyId);
            steadyQuestionRepository.deleteBySteadyId(steadyId);
            steadyRepository.delete(steady);
            return;
        }
        throw new IllegalArgumentException();
    }

    private boolean isWaitingApplication(User user, Steady steady) {
        return applicationRepository.findBySteadyIdAndUserIdAndStatus(steady.getId(), user.getId(), WAITING)
                .stream()
                .findFirst()
                .isPresent();
    }

    private List<Stack> getStacks(List<Long> stacks) {
        return stacks.stream()
                .map(this::getStack)
                .toList();
    }

    private Stack getStack(Long stackId) {
        return stackRepository.findById(stackId)
                .orElseThrow(IllegalArgumentException::new);
    }

    private List<SteadyPosition> createSteadyPositions(List<Long> positions, Steady steady) {
        return IntStream.range(0, positions.size())
                .mapToObj(index -> createSteadyPosition(positions, steady, index))
                .toList();
    }

    private SteadyPosition createSteadyPosition(List<Long> positions, Steady steady, int index) {
        Position position = positionRepository.findById(positions.get(index))
                .orElseThrow(IllegalArgumentException::new);

        return SteadyPosition.builder()
                .position(position)
                .steady(steady)
                .build();
    }

    private List<SteadyQuestion> createSteadyQuestions(List<String> questions, Steady steady) {
        return IntStream.range(0, questions.size())
                .mapToObj(index -> SteadyQuestion.builder()
                        .content(questions.get(index))
                        .sequence(index + 1)
                        .steady(steady)
                        .build())
                .toList();
    }

}
