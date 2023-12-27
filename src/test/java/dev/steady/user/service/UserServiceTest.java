package dev.steady.user.service;

import dev.steady.auth.domain.Account;
import dev.steady.auth.domain.repository.AccountRepository;
import dev.steady.review.domain.Review;
import dev.steady.review.domain.UserCard;
import dev.steady.review.domain.repository.CardRepository;
import dev.steady.review.domain.repository.ReviewRepository;
import dev.steady.review.domain.repository.UserCardRepository;
import dev.steady.steady.domain.repository.ParticipantRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.domain.UserStack;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import dev.steady.user.domain.repository.UserStackRepository;
import dev.steady.user.dto.request.UserCreateRequest;
import dev.steady.user.dto.response.UserMyDetailResponse;
import dev.steady.user.dto.response.UserOtherDetailResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.stream.IntStream;

import static dev.steady.auth.fixture.AccountFixture.createAccount;
import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.review.fixture.ReviewFixture.createCard;
import static dev.steady.review.fixture.ReviewFixture.createReview;
import static dev.steady.review.fixture.ReviewFixture.createUserCard;
import static dev.steady.steady.domain.Participant.createLeader;
import static dev.steady.steady.domain.Participant.createMember;
import static dev.steady.steady.domain.SteadyStatus.RECRUITING;
import static dev.steady.steady.fixture.SteadyFixtures.createSteady;
import static dev.steady.user.fixture.UserFixtures.createFirstUser;
import static dev.steady.user.fixture.UserFixtures.createPosition;
import static dev.steady.user.fixture.UserFixtures.createSecondUser;
import static dev.steady.user.fixture.UserFixtures.createStack;
import static dev.steady.user.fixture.UserFixtures.createUserUpdateRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private UserStackRepository userStackRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserCardRepository userCardRepository;

    @Autowired
    private SteadyRepository steadyRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private List<Stack> stacks;
    private Position position;

    @BeforeEach
    void setUp() {
        this.position = positionRepository.save(createPosition());
        this.stacks = stackRepository.saveAll(
                IntStream.range(0, 3)
                        .mapToObj(i -> createStack())
                        .toList()
        );
    }

    @AfterEach
    void tearDown() {
        userStackRepository.deleteAll();
        userCardRepository.deleteAll();
        reviewRepository.deleteAll();
        participantRepository.deleteAll();
        steadyRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        positionRepository.deleteAll();
        stackRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 정보를 입력 받아 유저를 생성한다.")
    void createUser() {
        // given
        var stacksId = stacks.stream().map(Stack::getId).toList();

        // when
        var request = new UserCreateRequest(1L, "Nickname", position.getId(), stacksId);
        var userId = userService.createUser(request);

        User user = transactionTemplate.execute(status -> {
            var foundUser = userRepository.getUserBy(userId);
            foundUser.getPosition().getName();
            return foundUser;
        });
        var userStacks = userStackRepository.findAllByUser(user);

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(userId),
                () -> assertThat(user.getPosition().getName()).isEqualTo(position.getName()),
                () -> assertThat(userStacks).hasSameSizeAs(request.stacksId())
        );
    }

    @Test
    @DisplayName("내 프로필을 조회할 수 있다.")
    void getMyUserDetail() {
        // given
        var savedUser = userRepository.save(createFirstUser(position));
        var userInfo = createUserInfo(savedUser.getId());
        var account = createAccount(savedUser);
        var savedAccount = accountRepository.save(account);
        var userStacks = userStackRepository.findAllByUser(savedUser);

        // when
        UserMyDetailResponse response = userService.getMyUserDetail(userInfo);

        // then
        assertAll(
                () -> assertThat(response.platform()).isEqualTo(savedAccount.getPlatform()),
                () -> assertThat(response.userId()).isEqualTo(savedUser.getId()),
                () -> assertThat(response.nickname()).isEqualTo(savedUser.getNickname()),
                () -> assertThat(response.profileImage()).isEqualTo(savedUser.getProfileImage()),
                () -> assertThat(response.position().name()).isEqualTo(savedUser.getPosition().getName()),
                () -> assertThat(response.stacks()).hasSameSizeAs(userStacks)
        );
    }

    @Test
    @DisplayName("인증된 사용자의 정보를 수정할 수 있다.")
    void updateUser() {
        // given
        var savedUser = userRepository.save(createFirstUser(position));
        var userInfo = createUserInfo(savedUser.getId());
        var newPosition = positionRepository.save(createPosition());
        var stacksId = stacks.stream().map(Stack::getId).toList();

        var request = createUserUpdateRequest(newPosition.getId(), stacksId);

        // when
        userService.updateUser(request, userInfo);

        // then
        var user = userRepository.getUserBy(userInfo.userId());
        var userStacks = userStackRepository.findAllByUser(user);
        assertAll(
                () -> assertThat(user.getNickname()).isEqualTo(request.nickname()),
                () -> assertThat(user.getProfileImage()).isEqualTo(request.profileImage()),
                () -> assertThat(user.getBio()).isEqualTo(request.bio()),
                () -> assertThat(user.getPosition().getId()).isEqualTo(newPosition.getId()),
                () -> assertThat(userStacks).hasSameSizeAs(request.stacksId())
        );
    }

    @Test
    @DisplayName("다른 사용자의 정보와 카드, 리뷰 코멘트를 조회할 수 있다. ")
    void getOtherUserDetail() {
        // given
        var reviewerUser = userRepository.save(createFirstUser(position));
        var revieweeUser = userRepository.save(createSecondUser(position));
        var savedSteady = steadyRepository.save(createSteady(reviewerUser, stacks, RECRUITING));
        var reviewer = participantRepository.save(createLeader(reviewerUser, savedSteady));
        var reviewee = participantRepository.save(createMember(revieweeUser, savedSteady));

        var review = createReview(reviewer, reviewee, savedSteady);
        var savedCard = cardRepository.save(createCard());
        reviewRepository.save(review);
        userCardRepository.save(createUserCard(revieweeUser, savedCard));

        var userStacks = userStackRepository.findAllByUser(revieweeUser);
        var reviews = reviewRepository.getPublicCommentsByRevieweeUser(revieweeUser);
        var userCards = userCardRepository.getCardCountByUser(revieweeUser);

        // when
        UserOtherDetailResponse response = userService.getOtherUserDetail(revieweeUser.getId());

        // then
        assertAll(
                () -> assertThat(response.user().userId()).isEqualTo(revieweeUser.getId()),
                () -> assertThat(response.user().nickname()).isEqualTo(revieweeUser.getNickname()),
                () -> assertThat(response.user().profileImage()).isEqualTo(revieweeUser.getProfileImage()),
                () -> assertThat(response.user().bio()).isEqualTo(revieweeUser.getBio()),
                () -> assertThat(response.user().position().id()).isEqualTo(revieweeUser.getPosition().getId()),
                () -> assertThat(response.user().stacks()).hasSameSizeAs(userStacks),
                () -> assertThat(response.reviews()).hasSameSizeAs(reviews),
                () -> assertThat(response.userCards()).hasSameSizeAs(userCards)
        );
    }

    @Test
    @DisplayName("유저의 탈퇴 요청을 처리할 수 있다.")
    void withdrawUserTesT() {
        // given
        var stacksId = stacks.stream().map(Stack::getId).toList();
        var request = new UserCreateRequest(1L, "Nickname", position.getId(), stacksId);
        var userId = userService.createUser(request);

        var user = transactionTemplate.execute(status -> userRepository.getUserBy(userId));

        var userInfo = createUserInfo(user.getId());

        var leader = userRepository.save(createFirstUser(position));
        var steady = steadyRepository.save(createSteady(leader, stacks, RECRUITING));
        var reviewer = participantRepository.save(createLeader(leader, steady));
        var reviewee = participantRepository.save(createMember(user, steady));

        var review = createReview(reviewer, reviewee, steady);
        var savedCard = cardRepository.save(createCard());
        reviewRepository.save(review);
        userCardRepository.save(createUserCard(user, savedCard));

        // when
        userService.withdrawUser(userInfo);

        // then
        int expectedSize = 0;
        User withdrawUser = userRepository.getUserBy(userId);
        List<UserStack> userStacks = userStackRepository.findAllByUser(user);
        List<UserCard> userCards = userCardRepository.findAllByUser(user);
        List<Review> reviews = reviewRepository.findAll();
        Account account = accountRepository.findByUser(user);
        assertAll(
                () -> assertThat(withdrawUser.getNickname()).startsWith("탈퇴한 유저"),
                () -> assertThat(withdrawUser.getBio()).isNull(),
                () -> assertThat(withdrawUser.getPosition()).isNull(),
                () -> assertThat(withdrawUser.isDeleted()).isTrue(),
                () -> assertThat(userStacks).hasSize(expectedSize),
                () -> assertThat(userCards).hasSize(expectedSize),
                () -> assertThat(reviews).hasSize(expectedSize),
                () -> assertThat(account).isNull()
        );
    }

}
