package dev.steady.user.service;

import dev.steady.auth.domain.Platform;
import dev.steady.auth.domain.repository.AccountRepository;
import dev.steady.global.auth.UserInfo;
import dev.steady.review.domain.repository.ReviewRepository;
import dev.steady.review.domain.repository.UserCardRepository;
import dev.steady.review.dto.response.UserCardResponse;
import dev.steady.steady.domain.Participant;
import dev.steady.steady.domain.repository.ParticipantRepository;
import dev.steady.storage.service.StorageService;
import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.domain.UserStack;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import dev.steady.user.domain.repository.UserStackRepository;
import dev.steady.user.dto.request.UserCreateRequest;
import dev.steady.user.dto.request.UserUpdateRequest;
import dev.steady.user.dto.response.UserDetailResponse;
import dev.steady.user.dto.response.UserMyDetailResponse;
import dev.steady.user.dto.response.UserNicknameExistResponse;
import dev.steady.user.dto.response.UserOtherDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String PROFILE_IMAGE_KEY_PATTERN = "profile/%s";
    private final UserRepository userRepository;
    private final StackRepository stackRepository;
    private final PositionRepository positionRepository;
    private final UserStackRepository userStackRepository;
    private final AccountRepository accountRepository;
    private final UserCardRepository userCardRepository;
    private final ReviewRepository reviewRepository;
    private final ParticipantRepository participantRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public UserMyDetailResponse getMyUserDetail(UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        List<UserStack> userStacks = userStackRepository.findAllByUser(user);
        Platform platform = accountRepository.findByUser(user).getPlatform();
        return UserMyDetailResponse.of(platform, user, userStacks);
    }

    @Transactional
    public Long createUser(UserCreateRequest request) {
        Position position = getPosition(request.positionId());

        User user = request.toEntity(position);
        User savedUser = userRepository.save(user);

        List<UserStack> userStacks = createUserStacks(request.stacksId(), savedUser);
        userStackRepository.saveAll(userStacks);

        return savedUser.getId();
    }

    @Transactional
    public void updateUser(UserUpdateRequest request, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Position updatedPosition = positionRepository.getById(request.positionId());
        user.update(request.profileImage(),
                request.nickname(),
                request.bio(),
                updatedPosition
        );

        userStackRepository.deleteAllByUser(user);
        List<UserStack> userStacks = createUserStacks(request.stacksId(), user);
        userStackRepository.saveAll(userStacks);
    }

    @Transactional(readOnly = true)
    public UserOtherDetailResponse getOtherUserDetail(Long userId) {
        User user = userRepository.getUserBy(userId);

        if (user.isDeleted()) {
            return UserOtherDetailResponse.deletedUser();
        }

        List<UserStack> userStacks = userStackRepository.findAllByUser(user);
        UserDetailResponse userDetailResponse = UserDetailResponse.of(user, userStacks);
        List<UserCardResponse> userCardResponses = userCardRepository.getCardCountByUser(user);
        List<String> reviews = reviewRepository.getPublicCommentsByRevieweeUser(user);

        return UserOtherDetailResponse.of(
                userDetailResponse,
                userCardResponses,
                reviews
        );
    }

    @Transactional(readOnly = true)
    public UserNicknameExistResponse existsByNickname(String nickname) {
        return new UserNicknameExistResponse(userRepository.existsByNickname(nickname));
    }

    @Transactional
    public void withdrawUser(UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        user.withdraw();
        userStackRepository.deleteAllByUser(user);
        userCardRepository.deleteAllByUser(user);
        List<Participant> participants = participantRepository.findByUser(user);
        participants.forEach(reviewRepository::deleteAllByReviewee);
        accountRepository.deleteByUser(user);
    }

    private Stack getStack(Long stackId) {
        return stackRepository.getById(stackId);
    }

    private List<Stack> getStacks(List<Long> stacksId) {
        return stacksId.stream()
                .map(this::getStack)
                .toList();
    }

    private Position getPosition(Long positionId) {
        return positionRepository.getById(positionId);
    }

    private List<UserStack> createUserStacks(List<Long> stacksId, User user) {
        List<Stack> stacks = getStacks(stacksId);
        return stacks.stream()
                .map(stack -> new UserStack(user, stack))
                .toList();
    }

}
