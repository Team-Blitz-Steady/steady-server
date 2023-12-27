package dev.steady.review.service;

import dev.steady.global.exception.InvalidStateException;
import dev.steady.review.domain.Card;
import dev.steady.review.domain.Review;
import dev.steady.review.domain.UserCard;
import dev.steady.review.domain.repository.CardRepository;
import dev.steady.review.domain.repository.ReviewRepository;
import dev.steady.review.domain.repository.UserCardRepository;
import dev.steady.review.dto.response.CardsResponse;
import dev.steady.review.dto.response.ReviewInfoResponse;
import dev.steady.review.dto.response.ReviewMyResponse;
import dev.steady.review.dto.response.ReviewSteadyResponse;
import dev.steady.review.dto.response.ReviewSwitchResponse;
import dev.steady.review.dto.response.RevieweeResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.review.fixture.ReviewFixture.createAnotherCard;
import static dev.steady.review.fixture.ReviewFixture.createCard;
import static dev.steady.review.fixture.ReviewFixture.createReview;
import static dev.steady.review.fixture.ReviewFixture.createReviewCreateRequest;
import static dev.steady.review.fixture.ReviewFixture.createUserCard;
import static dev.steady.steady.domain.Participant.createMember;
import static dev.steady.steady.domain.SteadyStatus.FINISHED;
import static dev.steady.steady.fixture.SteadyFixtures.createSteady;
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
    private PositionRepository positionRepository;
    @Autowired
    private StackRepository stackRepository;

    private User leader;
    private User reviewerUser;
    private User revieweeUser;
    private List<Stack> stacks;

    @BeforeEach
    void setUp() {
        var position = positionRepository.save(createPosition());
        this.leader = userRepository.save(createFirstUser(position));
        this.reviewerUser = userRepository.save(createSecondUser(position));
        this.revieweeUser = userRepository.save(createThirdUser(position));
        this.stacks = stackRepository.saveAll(
                IntStream.range(0, 3)
                        .mapToObj(i -> createStack())
                        .toList()
        );
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
    void createReviewTest() {
        // given
        var userInfo = createUserInfo(reviewerUser.getId());

        var steady = createSteady(leader, stacks, FINISHED);
        ReflectionTestUtils.setField(steady, "finishedAt", LocalDate.now());
        var savedSteady = steadyRepository.save(steady);

        var reviewer = participantRepository.save(createMember(reviewerUser, savedSteady));
        var reviewee = participantRepository.save(createMember(revieweeUser, savedSteady));

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
    void createReviewAfterReviewEnabledPeriodTest() {
        // given
        var userInfo = createUserInfo(reviewerUser.getId());
        var steady = createSteady(leader, stacks, FINISHED);
        var finishedAt = LocalDate.now().minusMonths(3);
        ReflectionTestUtils.setField(steady, "finishedAt", finishedAt);
        var savedSteady = steadyRepository.save(steady);

        participantRepository.save(createMember(reviewerUser, savedSteady));
        var reviewee = participantRepository.save(createMember(revieweeUser, savedSteady));
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
    void createUserCardsTest() {
        // given
        List<Card> cards = IntStream.range(0, 3)
                .mapToObj(i -> createCard())
                .toList();
        List<Card> savedCards = cardRepository.saveAll(cards);

        var steady = steadyRepository.save(createSteady(reviewerUser, stacks, FINISHED));
        var reviewee = participantRepository.save(createMember(revieweeUser, steady));

        // when
        List<Long> cardsId = List.of(savedCards.get(0).getId(), savedCards.get(1).getId());
        var request = createReviewCreateRequest(
                reviewee.getUserId(),
                cardsId
        );

        reviewService.createUserCards(request);
        List<UserCard> userCards = userCardRepository.findAllByUser(reviewee.getUser());

        // then
        assertAll(
                () -> assertThat(userCards).hasSameSizeAs(cardsId)
        );
    }


    @Test
    @DisplayName("리뷰이는 본인의 리뷰 코멘트를 비공개로 설정할 수 있다.")
    void switchReviewIsPublicTest() {
        // given
        var userInfo = createUserInfo(revieweeUser.getId());
        var steady = steadyRepository.save(createSteady(leader, stacks, FINISHED));
        var reviewer = participantRepository.save(createMember(reviewerUser, steady));
        var reviewee = participantRepository.save(createMember(revieweeUser, steady));
        var review = reviewRepository.save(createReview(reviewer, reviewee, steady));

        // when
        ReviewSwitchResponse response = reviewService.switchReviewIsPublic(review.getId(), userInfo);

        // then
        Review foundReview = reviewRepository.getById(review.getId());
        assertThat(foundReview.isPublic()).isEqualTo(response.isPublic());
    }

    @Test
    @DisplayName("인증된 사용자는 본인이 받은 카드 수와 리뷰 코멘트를 조회할 수 있다.")
    void getMyReviewsTest() {
        // given
        var userInfo = createUserInfo(revieweeUser.getId());
        var steady = steadyRepository.save(createSteady(leader, stacks, FINISHED));
        var reviewer = participantRepository.save(createMember(reviewerUser, steady));
        var reviewee = participantRepository.save(createMember(revieweeUser, steady));
        var review = reviewRepository.save(createReview(reviewer, reviewee, steady));
        var savedCard = cardRepository.save(createCard());
        reviewRepository.save(review);
        userCardRepository.save(createUserCard(revieweeUser, savedCard));

        var allReviews = reviewRepository.getAllReviewsByRevieweeUser(revieweeUser);
        var cardsCount = userCardRepository.getCardCountByUser(revieweeUser);
        // when
        ReviewMyResponse response = reviewService.getMyCardsAndReviews(userInfo);

        // then
        assertAll(
                () -> assertThat(response.reviews()).hasSameSizeAs(allReviews),
                () -> assertThat(response.userCards()).hasSameSizeAs(cardsCount)
        );
    }

    @Test
    @DisplayName("리뷰할 스테디 정보와 리뷰이 목록을 조회할 수 있다.")
    void getReviewInfoTest() {
        // given
        var userInfo = createUserInfo(reviewerUser.getId());

        var steady = createSteady(leader, stacks, FINISHED);
        var finishedAt = LocalDate.now();
        var reviewDeadline = finishedAt.plusMonths(2L);

        ReflectionTestUtils.setField(steady, "finishedAt", finishedAt);
        var savedSteady = steadyRepository.save(steady);

        var reviewer = participantRepository.save(createMember(reviewerUser, savedSteady));
        var reviewee = participantRepository.save(createMember(revieweeUser, savedSteady));

        // when
        ReviewInfoResponse response = reviewService.getReviewInfo(savedSteady.getId(), userInfo);
        ReviewSteadyResponse steadyResponse = response.steady();

        // then
        assertAll(
                () -> assertThat(steadyResponse.steadyId()).isEqualTo(steady.getId()),
                () -> assertThat(steadyResponse.finishedAt()).isEqualTo(finishedAt),
                () -> assertThat(steadyResponse.reviewDeadline()).isEqualTo(reviewDeadline),
                () -> assertThat(steadyResponse.participatedAt()).isEqualTo(reviewer.getCreatedAt().toLocalDate()),
                () -> assertThat(response.reviewees()).contains(RevieweeResponse.from(reviewee))
        );
    }

    @Test
    @DisplayName("모든 카드를 가져올 수 있다.")
    void getAllCardsTest() {
        // given
        cardRepository.save(createCard());
        cardRepository.save(createAnotherCard());
        var cards = cardRepository.findAll();

        // when
        CardsResponse response = reviewService.getAllCards();

        // then
        assertThat(response.cards()).hasSameSizeAs(cards);
    }

}
