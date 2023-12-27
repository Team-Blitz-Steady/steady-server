package dev.steady.review.fixture;

import dev.steady.review.domain.Card;
import dev.steady.review.domain.Review;
import dev.steady.review.domain.UserCard;
import dev.steady.review.dto.request.ReviewCreateRequest;
import dev.steady.review.dto.response.CardResponse;
import dev.steady.review.dto.response.CardsResponse;
import dev.steady.review.dto.response.ReviewDetailResponse;
import dev.steady.review.dto.response.ReviewInfoResponse;
import dev.steady.review.dto.response.ReviewMyResponse;
import dev.steady.review.dto.response.ReviewSteadyResponse;
import dev.steady.review.dto.response.ReviewSwitchResponse;
import dev.steady.review.dto.response.RevieweeResponse;
import dev.steady.review.dto.response.ReviewsBySteadyResponse;
import dev.steady.review.dto.response.UserCardResponse;
import dev.steady.steady.domain.Participant;
import dev.steady.steady.domain.Steady;
import dev.steady.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static dev.steady.steady.fixture.SteadyFixtures.createSteady;

public class ReviewFixture {

    public static ReviewCreateRequest createReviewCreateRequest(Long revieweeId, List<Long> cardsId) {
        return new ReviewCreateRequest(
                revieweeId,
                cardsId,
                "리뷰 한 줄 코멘트"
        );
    }

    public static Card createCard() {
        return new Card(
                "이 팀원은 출석을 열심히 잘 했어요!",
                "attendance.jpg"
        );
    }

    public static Card createAnotherCard() {
        return new Card(
                "이 팀원은 열정적으로 참여했어요!",
                "passion.jpg"
        );
    }

    public static Review createReview(Participant reviewer, Participant reviewee, Steady steady) {
        return Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .comment("다음에도 저랑 같이 공부해요!")
                .build();
    }

    public static UserCard createUserCard(User user, Card card) {
        return new UserCard(user, card);
    }

    public static List<UserCardResponse> createUserCardResponses() {
        return List.of(
                new UserCardResponse(1L, "이 팀원은 출석을 열심히 잘 했어요!", 3L),
                new UserCardResponse(2L, "이 팀원은 스테디에 열정적이었어요!", 2L),
                new UserCardResponse(3L, "이 팀원은 지식 공유에 적극적이었어요!", 0L),
                new UserCardResponse(4L, "이 팀원과 의사소통이 잘돼요!", 0L),
                new UserCardResponse(5L, "이 팀원이랑 나중에도 같이 스테디 하고 싶어요!", 1L)
        );
    }

    public static ReviewCreateRequest createReviewCreateRequest() {
        return new ReviewCreateRequest(
                2L,
                List.of(1L, 2L),
                "열심히 하는 모습 보기 좋습니다."
        );
    }

    public static ReviewSwitchResponse createReviewSwitchResponse(boolean isPublic) {
        return new ReviewSwitchResponse(isPublic);
    }

    public static ReviewMyResponse createReviewMyResponse() {
        ReviewDetailResponse reviewDetailResponse = new ReviewDetailResponse(
                1L,
                "열심히 하는 모습 보기 좋습니다.",
                true,
                LocalDateTime.now()
        );
        ReviewsBySteadyResponse reviewsBySteadyResponse = new ReviewsBySteadyResponse(
                1L,
                "블리츠",
                List.of(reviewDetailResponse)
        );
        return new ReviewMyResponse(
                createUserCardResponses(),
                List.of(reviewsBySteadyResponse)
        );
    }

    public static ReviewInfoResponse createReviewInfoResponse() {
        Steady steady = createSteady();
        LocalDate finishedAt = LocalDate.of(2023, 12, 2);
        ReviewSteadyResponse reviewSteadyResponse = ReviewSteadyResponse.builder()
                .steadyId(steady.getId())
                .name(steady.getName())
                .steadyMode(steady.getSteadyMode())
                .steadyType(steady.getType())
                .participants(3)
                .finishedAt(finishedAt)
                .participatedAt(LocalDate.of(2023, 10, 12))
                .reviewDeadline(finishedAt.plusMonths(2L))
                .build();

        List<RevieweeResponse> revieweeResponses = List.of(
                new RevieweeResponse(2L, "꼬부기", "profile_image1.jpg"),
                new RevieweeResponse(3L, "파이리", "profile_image2.jpg"),
                new RevieweeResponse(3L, "메타몽", "profile_image2.jpg")
        );

        return ReviewInfoResponse.of(reviewSteadyResponse, revieweeResponses);
    }

    public static CardsResponse createCardsResponse() {
        return new CardsResponse(
                List.of(
                        new CardResponse(1L, "이 팀원은 출석을 열심히 잘 했어요!", "attendance.jpg"),
                        new CardResponse(2L, "이 팀원은 스테디에 열정적이었어요!", "passion.jpg")
                )
        );
    }

}
