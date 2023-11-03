package dev.steady.user.service;

import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.domain.UserStack;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import dev.steady.user.domain.repository.UserStackRepository;
import dev.steady.user.dto.request.UserCreateRequest;
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

    @Transactional
    public Long createUser(UserCreateRequest request) {
        Position position = getPosition(request.positionId());

        User user = request.toEntity(position);
        User savedUser = userRepository.save(user);

        List<UserStack> userStacks = createUserStacks(request.stackIds(), savedUser);
        userStackRepository.saveAll(userStacks);

        return savedUser.getId();
    }

    @Transactional(readOnly = true)
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Transactional(readOnly = true)
    public Stack getStack(Long stackId) {
        return stackRepository.findById(stackId).orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public List<Stack> getStacks(List<Long> stackIds) {
        return stackIds.stream()
                .map(this::getStack)
                .toList();
    }

    @Transactional(readOnly = true)
    public Position getPosition(Long positionId) {
        return positionRepository.findById(positionId)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Transactional
    public List<UserStack> createUserStacks(List<Long> stackIds, User user) {
        List<Stack> stacks = getStacks(stackIds);
        List<UserStack> userStacks = stacks.stream()
                .map(stack -> new UserStack(user, stack))
                .toList();
        return userStacks;
    }

}