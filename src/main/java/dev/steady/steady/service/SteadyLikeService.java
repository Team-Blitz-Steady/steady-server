package dev.steady.steady.service;

import dev.steady.global.auth.UserInfo;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyLike;
import dev.steady.steady.domain.repository.SteadyLikeRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.steady.dto.response.SteadyLikeResponse;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SteadyLikeService {

    private final SteadyLikeRepository steadyLikeRepository;
    private final UserRepository userRepository;
    private final SteadyRepository steadyRepository;

    @Transactional
    public SteadyLikeResponse updateSteadyLike(Long steadyId, UserInfo userInfo) {
        User user = userRepository.getUserBy(userInfo.userId());
        Steady steady = steadyRepository.getSteady(steadyId);
        Optional<SteadyLike> steadyLike = steadyLikeRepository.findByUserAndSteady(user, steady);

        switchLike(steadyLike, steady, user);

        boolean isLiked = steadyLike.isEmpty();
        return new SteadyLikeResponse(steady.getLikeCount(), isLiked);
    }

    private void switchLike(Optional<SteadyLike> steadyLike, Steady steady, User user) {
        if (steadyLike.isPresent()) {
            steadyLikeRepository.delete(steadyLike.get());
            steady.decreaseLikeCount();
            return;
        }
        steadyLikeRepository.save(new SteadyLike(user, steady));
        steady.increaseLikeCount();
    }

}
