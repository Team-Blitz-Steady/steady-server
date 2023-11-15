package dev.steady.review.service;

import dev.steady.global.auth.UserInfo;
import dev.steady.global.exception.InvalidStateException;
import dev.steady.review.domain.Card;
import dev.steady.review.domain.Review;
import dev.steady.review.domain.UserCard;
import dev.steady.review.domain.repository.CardRepository;
import dev.steady.review.domain.repository.ReviewRepository;
import dev.steady.review.domain.repository.UserCardRepository;
import dev.steady.review.dto.ReviewCreateRequest;
import dev.steady.steady.domain.Participant;
import dev.steady.steady.domain.Participants;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static dev.steady.review.exception.ReviewErrorCode.REVIEWEE_EQUALS_REVIEWER;
import static dev.steady.review.exception.ReviewErrorCode.REVIEW_DUPLICATE;
import static dev.steady.review.exception.ReviewErrorCode.REVIEW_NOT_ENABLED_PERIOD;
import static dev.steady.review.exception.ReviewErrorCode.STEADY_NOT_FINISHED;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SteadyRepository steadyRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final UserCardRepository userCardRepository;

    @Transactional
    public Long createReview(Long steadyId, ReviewCreateRequest request, UserInfo userInfo) {
        Long reviewerId = userInfo.userId();
        Long revieweeId = request.revieweeId();
        if (Objects.equals(revieweeId, reviewerId)) {
            throw new InvalidStateException(REVIEWEE_EQUALS_REVIEWER);
        }

        Steady steady = getSteady(steadyId);
        Participants participants = steady.getParticipants();

        if (!steady.isFinished()) {
            throw new InvalidStateException(STEADY_NOT_FINISHED);
        }

        if (steady.isReviewEnabled()) {
            throw new InvalidStateException(REVIEW_NOT_ENABLED_PERIOD);
        }

        Participant reviewer = participants.getParticipantByUserId(reviewerId);
        Participant reviewee = participants.getParticipantByUserId(revieweeId);

        if (isAlreadyReviewed(reviewer, reviewee, steady)) {
            throw new InvalidStateException(REVIEW_DUPLICATE);
        }

        Review review = request.toEntity(reviewer, reviewee, steady);
        Review savedReview = reviewRepository.save(review);

        return savedReview.getId();
    }

    @Transactional
    public void createUserCards(ReviewCreateRequest request) {
        User reviewee = userRepository.getUserBy(request.revieweeId());
        List<Card> cards = getCards(request.cardIds());
        List<UserCard> userCards = cards.stream()
                .map(card -> new UserCard(reviewee, card))
                .toList();

        userCardRepository.saveAll(userCards);
    }

    private List<Card> getCards(List<Long> cardIds) {
        return cardIds.stream()
                .map(this::getCard)
                .toList();
    }

    private Card getCard(Long cardId) {
        return cardRepository.getById(cardId);
    }

    private Steady getSteady(Long steadyId) {
        return steadyRepository.getSteady(steadyId);
    }

    private boolean isAlreadyReviewed(Participant reviewer, Participant reviewee, Steady steady) {
        return reviewRepository.existsByReviewerAndRevieweeAndSteady(
                reviewer,
                reviewee,
                steady
        );
    }

}
