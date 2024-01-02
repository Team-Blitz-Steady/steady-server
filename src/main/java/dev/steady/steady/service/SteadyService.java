package dev.steady.steady.service;

import dev.steady.application.domain.Application;
import dev.steady.application.domain.repository.ApplicationRepository;
import dev.steady.application.dto.response.SliceResponse;
import dev.steady.global.auth.UserInfo;
import dev.steady.global.exception.InvalidStateException;
import dev.steady.steady.dto.RankCondition;
import dev.steady.steady.domain.Participant;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyPosition;
import dev.steady.steady.domain.SteadyQuestion;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.domain.SteadyViewLog;
import dev.steady.steady.domain.repository.ParticipantRepository;
import dev.steady.steady.domain.repository.SteadyLikeRepository;
import dev.steady.steady.domain.repository.SteadyPositionRepository;
import dev.steady.steady.domain.repository.SteadyQuestionRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.steady.domain.repository.ViewCountLogRepository;
import dev.steady.steady.dto.FilterConditionDto;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.dto.request.SteadyQuestionUpdateRequest;
import dev.steady.steady.dto.request.SteadyUpdateRequest;
import dev.steady.steady.dto.response.MySteadyQueryResponse;
import dev.steady.steady.dto.response.MySteadyResponse;
import dev.steady.steady.dto.response.PageResponse;
import dev.steady.steady.dto.response.ParticipantsResponse;
import dev.steady.steady.dto.response.SteadyDetailResponse;
import dev.steady.steady.dto.response.SteadyQueryResponse;
import dev.steady.steady.dto.response.SteadyQuestionsResponse;
import dev.steady.steady.dto.response.SteadyRankResponse;
import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static dev.steady.application.domain.ApplicationStatus.WAITING;
import static dev.steady.steady.exception.SteadyErrorCode.STEADY_IS_NOT_EMPTY;

@Service
@RequiredArgsConstructor
public class SteadyService {

    private final UserRepository userRepository;
    private final StackRepository stackRepository;
    private final SteadyRepository steadyRepository;
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final ViewCountLogRepository viewCountLogRepository;
    private final SteadyQuestionRepository steadyQuestionRepository;
    private final SteadyPositionRepository steadyPositionRepository;
    private final SteadyLikeRepository steadyLikeRepository;
    private final ParticipantRepository participantRepository;

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
    public PageResponse<SteadyQueryResponse> getSteadies(UserInfo userInfo, FilterConditionDto conditionDto, Pageable pageable) {
        Page<Steady> steadies = steadyRepository.findAllByFilterCondition(userInfo, conditionDto, pageable);
        Page<SteadyQueryResponse> searchResponses = steadies.map(SteadyQueryResponse::from);
        return PageResponse.from(searchResponses);
    }

    @Transactional(readOnly = true)
    public List<SteadyRankResponse> getPopularStudy(RankCondition condition) {
        List<Steady> steadies = steadyRepository.findPopularStudyInCondition(condition);
        return steadies.stream()
                .map(SteadyRankResponse::from)
                .toList();
    }

    @Transactional
    public SteadyDetailResponse getDetailSteady(Long steadyId, UserInfo userInfo) {
        Steady steady = steadyRepository.getSteady(steadyId);
        List<SteadyPosition> positions = steadyPositionRepository.findBySteadyId(steady.getId());

        boolean isLeader = false;
        Long applicationId = null;
        boolean isLiked = false;

        if (userInfo.isAuthenticated()) {
            User user = userRepository.getUserBy(userInfo.userId());
            isLeader = steady.isLeader(user);
            isLiked = steadyLikeRepository.findByUserAndSteady(user, steady).isPresent();

            if (!isLeader) {
                applicationId = findSubmittedApplicationId(user, steady);
            }

            processViewCountLog(user, steady);
        }

        return SteadyDetailResponse.of(steady, positions, isLeader, applicationId, isLiked);
    }

    @Transactional(readOnly = true)
    public SteadyQuestionsResponse getSteadyQuestions(Long steadyId) {
        Steady steady = steadyRepository.getSteady(steadyId);
        List<SteadyQuestion> steadyQuestions = steadyQuestionRepository.findBySteadyId(steadyId);
        return SteadyQuestionsResponse.of(steady, steadyQuestions);
    }

    @Transactional(readOnly = true)
    public ParticipantsResponse getSteadyParticipants(Long steadyId) {
        Steady steady = steadyRepository.getSteady(steadyId);
        List<Participant> participants = steady.getParticipants().getAllParticipants();
        return ParticipantsResponse.from(participants);
    }

    @Transactional
    public void updateSteady(Long steadyId, SteadyUpdateRequest request, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        List<Stack> stacks = getStacks(request.stacks());
        steady.update(user,
                request.name(),
                request.bio(),
                request.contact(),
                request.type(),
                request.status(),
                request.participantLimit(),
                request.steadyMode(),
                request.scheduledPeriod(),
                request.deadline(),
                request.title(),
                request.content(),
                stacks);
        updateSteadyPositions(steady, request.positions());
    }

    @Transactional
    public void updateSteadyQuestions(Long steadyId, SteadyQuestionUpdateRequest request, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        steady.validateLeader(user);

        steadyQuestionRepository.deleteBySteadyId(steadyId);
        List<SteadyQuestion> steadyQuestions = createSteadyQuestions(request.questions(), steady);
        steadyQuestionRepository.saveAll(steadyQuestions);
    }

    @Transactional
    public void expelParticipant(Long steadyId, Long memberId, UserInfo userInfo) {
        Steady steady = steadyRepository.getSteady(steadyId);
        User leader = userRepository.getUserBy(userInfo.userId());
        User member = userRepository.getUserBy(memberId);
        Participant participant = participantRepository.findByUserAndSteady(member, steady);
        steady.expelParticipantByLeader(leader, participant);
    }

    @Transactional
    public void promoteSteady(Long steadyId, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        steady.usePromotion(user);
    }

    @Transactional
    public void finishSteady(Long steadyId, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        steady.finish(user);
    }

    @Transactional
    public void deleteSteady(Long steadyId, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        if (steady.isDeletable(user)) {
            steadyPositionRepository.deleteBySteadyId(steadyId);
            steadyQuestionRepository.deleteBySteadyId(steadyId);
            steadyRepository.delete(steady);
            return;
        }
        throw new InvalidStateException(STEADY_IS_NOT_EMPTY);
    }

    @Transactional(readOnly = true)
    public SliceResponse<MySteadyResponse> findMySteadies(SteadyStatus status, UserInfo userInfo, Pageable pageable) {
        User user = userRepository.getUserBy(userInfo.userId());

        Slice<MySteadyQueryResponse> mySteadies = steadyRepository.findMySteadies(status, user, pageable);
        Slice<MySteadyResponse> response = mySteadies.map(MySteadyResponse::from);
        return SliceResponse.from(response);
    }

    private void processViewCountLog(User user, Steady steady) {
        Optional<SteadyViewLog> viewLog =
                viewCountLogRepository.findFirstByUserIdAndSteadyIdOrderByCreatedAtDesc(user.getId(), steady.getId());

        if (shouldIncreaseViewCount(viewLog)) {
            steady.increaseViewCount();
            viewCountLogRepository.save(new SteadyViewLog(user.getId(), steady.getId()));
        }
    }

    private boolean shouldIncreaseViewCount(Optional<SteadyViewLog> viewLogOptional) {
        return !viewLogOptional.isPresent() || viewLogOptional.get().checkThreeHoursPassed();
    }

    private Long findSubmittedApplicationId(User user, Steady steady) {
        Optional<Application> application = applicationRepository
                .findBySteadyIdAndUserIdAndStatus(steady.getId(), user.getId(), WAITING);
        if (application.isPresent()) {
            return application.get().getId();
        }
        return null;
    }

    private List<Stack> getStacks(List<Long> stacks) {
        return stacks.stream()
                .map(this::getStack)
                .toList();
    }

    private Stack getStack(Long stackId) {
        return stackRepository.getById(stackId);
    }

    private List<SteadyPosition> createSteadyPositions(List<Long> positions, Steady steady) {
        return IntStream.range(0, positions.size())
                .mapToObj(index -> {
                    Position position = positionRepository.getById(positions.get(index));
                    return SteadyPosition.builder()
                            .position(position)
                            .steady(steady)
                            .build();
                })
                .toList();
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

    private void updateSteadyPositions(Steady steady, List<Long> positions) {
        steadyPositionRepository.deleteBySteadyId(steady.getId());
        List<SteadyPosition> steadyPositions = createSteadyPositions(positions, steady);
        steadyPositionRepository.saveAll(steadyPositions);
    }

}
