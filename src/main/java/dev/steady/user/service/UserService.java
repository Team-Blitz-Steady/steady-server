package dev.steady.user.service;

import dev.steady.auth.domain.Account;
import dev.steady.auth.domain.Platform;
import dev.steady.auth.domain.repository.AccountRepository;
import dev.steady.global.auth.UserInfo;
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
import dev.steady.user.dto.response.UserMyDetailResponse;
import dev.steady.user.dto.response.UserNicknameExistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StackRepository stackRepository;
    private final PositionRepository positionRepository;
    private final UserStackRepository userStackRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public UserMyDetailResponse getMyUserDetail(UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        List<UserStack> userStacks = userStackRepository.findAllByUser(user);
        Platform platform = getAccount(user).getPlatform();
        return UserMyDetailResponse.from(platform, user, userStacks);
    }

    @Transactional
    public Long createUser(UserCreateRequest request) {
        Position position = getPosition(request.positionId());

        User user = request.toEntity(position);
        User savedUser = userRepository.save(user);

        List<UserStack> userStacks = createUserStacks(request.stackIds(), savedUser);
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
        List<UserStack> userStacks = createUserStacks(request.stackIds(), user);
        userStackRepository.saveAll(userStacks);
    }

    @Transactional(readOnly = true)
    public UserNicknameExistResponse existsByNickname(String nickname) {
        return new UserNicknameExistResponse(userRepository.existsByNickname(nickname));
    }

    private Stack getStack(Long stackId) {
        return stackRepository.getById(stackId);
    }

    private List<Stack> getStacks(List<Long> stackIds) {
        return stackIds.stream()
                .map(this::getStack)
                .toList();
    }

    private Position getPosition(Long positionId) {
        return positionRepository.getById(positionId);
    }

    private List<UserStack> createUserStacks(List<Long> stackIds, User user) {
        List<Stack> stacks = getStacks(stackIds);
        return stacks.stream()
                .map(stack -> new UserStack(user, stack))
                .toList();
    }

    private Account getAccount(User user) {
        return accountRepository.findByUser(user);
    }

}
