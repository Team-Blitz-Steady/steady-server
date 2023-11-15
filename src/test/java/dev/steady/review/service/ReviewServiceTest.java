package dev.steady.review.service;

import dev.steady.global.exception.InvalidStateException;
import dev.steady.review.domain.Card;
import dev.steady.review.domain.Review;
import dev.steady.review.domain.UserCard;
import dev.steady.review.domain.repository.CardRepository;
import dev.steady.review.domain.repository.ReviewRepository;
import dev.steady.review.domain.repository.UserCardRepository;
import dev.steady.steady.domain.repository.ParticipantRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.review.fixture.ReviewFixture.createCard;
import static dev.steady.review.fixture.ReviewFixture.createReviewCreateRequest;
import static dev.steady.steady.domain.Participant.createMember;
import static dev.steady.steady.fixture.SteadyFixtures.createFinishedSteady;
import static dev.steady.user.fixture.UserFixtures.createFirstUser;
import static dev.steady.user.fixture.UserFixtures.createPosition;
import static dev.steady.user.fixture.UserFixtures.createSecondUser;
import static dev.steady.user.fixture.UserFixtures.createStack;
import static dev.steady.user.fixture.UserFixtures.createThirdUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private SteadyRepository steadyRepository;
    @Autowired
    private ParticipantRepository participantRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UserCardRepository userCardRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private StackRepository stackRepository;

    private Stack stack;
    private User leader;
    private User reviewerUser;
    private User revieweeUser;

    @BeforeEach
    void setUp() {
        var position = positionRepository.save(createPosition());
        this.stack = stackRepository.save(createStack());
        this.leader = userRepository.save(createFirstUser(position));
        this.reviewerUser = userRepository.save(createSecondUser(position));
        this.revieweeUser = userRepository.save(createThirdUser(position));
    }

    @AfterEach
    void tearDown() {
        userCardRepository.deleteAll();
        reviewRepository.deleteAll();
        participantRepository.deleteAll();
        steadyRepository.deleteAll();
        userRepository.deleteAll();
        cardRepository.deleteAll();
        stackRepository.deleteAll();
        positionRepository.deleteAll();
    }

    @DisplayName("종료된 스테디에 대하여 리뷰를 생성한다.")
    @Test
    void createReview() {
        // given
        var userInfo = createUserInfo(reviewerUser.getId());
        var steady = steadyRepository.save(createFinishedSteady(leader, stack, LocalDate.now()));

        var reviewer = participantRepository.save(createMember(reviewerUser, steady));
        var reviewee = participantRepository.save(createMember(revieweeUser, steady));

        var request = createReviewCreateRequest(
                reviewee.getUserId(),
                List.of(1L, 2L)
        );

        // when
        var reviewId = reviewService.createReview(steady.getId(), request, userInfo);

        // then
        Review review = reviewRepository.getById(reviewId);
        assertAll(
                () -> assertThat(review.getId()).isEqualTo(reviewId),
                () -> assertThat(review.getReviewer().getId()).isEqualTo(reviewer.getId()),
                () -> assertThat(review.getReviewee().getId()).isEqualTo(reviewee.getId())
        );
    }

    @Test
    @DisplayName("리뷰 가능 기간이 지나면 리뷰를 생성할 수 없다.")
    void createReviewAfterReviewEnabledPeriod() {
        // given
        var userInfo = createUserInfo(reviewerUser.getId());
        var finishedAt = LocalDate.now().minusMonths(3);
        var steady = steadyRepository.save(createFinishedSteady(leader, stack, finishedAt));
        var reviewer = participantRepository.save(createMember(reviewerUser, steady));
        var reviewee = participantRepository.save(createMember(revieweeUser, steady));
        var request = createReviewCreateRequest(
                reviewee.getUserId(),
                List.of(1L, 2L)
        );

        // when, then
        assertThatThrownBy(
                () -> reviewService.createReview(steady.getId(), request, userInfo)
        ).isInstanceOf(InvalidStateException.class);
    }

    @Test
    @DisplayName("사용자의 리뷰 카드를 생성할 수 있다.")
    void createUserCards() {
        // given
        List<Card> cards = IntStream.range(0, 3)
                .mapToObj(i -> createCard())
                .toList();
        cardRepository.saveAll(cards);

        var steady = steadyRepository.save(createFinishedSteady(reviewerUser, stack, LocalDate.now()));
        var reviewee = participantRepository.save(createMember(revieweeUser, steady));

        // when
        List<Long> cardIds = List.of(1L, 2L);
        var request = createReviewCreateRequest(
                reviewee.getUserId(),
                cardIds
        );

        reviewService.createUserCards(request);
        List<UserCard> userCards = userCardRepository.findAllByUser(reviewee.getUser());

        // then
        assertAll(
                () -> assertThat(userCards).hasSameSizeAs(cardIds)
        );
    }

}
