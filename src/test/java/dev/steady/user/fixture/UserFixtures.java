package dev.steady.user.fixture;

import dev.steady.auth.domain.Platform;
import dev.steady.review.dto.response.UserCardResponse;
import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.dto.request.UserCreateRequest;
import dev.steady.user.dto.request.UserUpdateRequest;
import dev.steady.user.dto.response.PositionResponse;
import dev.steady.user.dto.response.PositionsResponse;
import dev.steady.user.dto.response.StackResponse;
import dev.steady.user.dto.response.StacksResponse;
import dev.steady.user.dto.response.UserDetailResponse;
import dev.steady.user.dto.response.UserMyDetailResponse;
import dev.steady.user.dto.response.UserOtherDetailResponse;

import java.util.List;

import static dev.steady.review.fixture.ReviewFixture.createUserCardResponses;

public class UserFixtures {

    public static Position createPosition() {
        return Position.builder()
                .name("백엔드")
                .build();
    }

    public static Position createAnotherPosition() {
        return Position.builder()
                .name("프론트엔드")
                .build();
    }

    public static List<Position> createPositions() {
        return List.of(createPosition(), createAnotherPosition());
    }

    public static Stack createStack() {
        return Stack.builder()
                .name("Java")
                .imageUrl("www")
                .build();
    }

    public static Stack createAnotherStack() {
        return Stack.builder()
                .name("JavaScript")
                .imageUrl("www")
                .build();
    }

    public static List<Stack> createStacks() {
        return List.of(createStack(), createAnotherStack());
    }

    public static User createFirstUser(Position position) {
        return User.builder()
                .profileImage("123")
                .nickname("weonest")
                .bio("나에요")
                .position(position)
                .build();
    }

    public static User createSecondUser(Position position) {
        User user = User.builder()
                .profileImage("1234")
                .nickname("Jun")
                .bio("저에요")
                .position(position)
                .build();
        return user;
    }

    public static User createThirdUser(Position position) {
        User user = User.builder()
                .profileImage("1234")
                .nickname("Young")
                .bio("저에요")
                .position(position)
                .build();
        return user;
    }

    public static UserCreateRequest createUserCreateRequest() {
        return new UserCreateRequest(
                1L,
                "닉네임",
                1L,
                List.of(1L, 2L)
        );
    }

    public static StacksResponse createStackResponses() {
        return new StacksResponse(List.of(
                new StackResponse(1L, "Java", "www.java.com"),
                new StackResponse(2L, "JavaScript", "www.javascript.com")
        ));
    }

    public static PositionsResponse createPositionResponses() {
        return new PositionsResponse(List.of(
                new PositionResponse(1L, "백엔드"),
                new PositionResponse(2L, "프론트엔드")
        ));
    }

    public static UserUpdateRequest createUserUpdateRequest(Long positionId, List<Long> stacksId) {
        return new UserUpdateRequest(
                "new_image.jpg",
                "newNickname",
                "newBio",
                positionId,
                stacksId
        );
    }

    public static UserMyDetailResponse createUserMyDetailResponse(Platform platform) {
        return UserMyDetailResponse.builder()
                .platform(platform)
                .userId(1L)
                .nickname("꼬부기")
                .profileImage("profile.png")
                .bio("안녕하세요")
                .position(new PositionResponse(1L, "백엔드"))
                .stacks(List.of(
                        new StackResponse(1L, "Java", "java.jpg"),
                        new StackResponse(2L, "JavaScript", "javascript.jpeg")))
                .build();
    }

    public static UserOtherDetailResponse createUserOtherDetailResponse() {
        UserDetailResponse userDetailResponse = UserDetailResponse.builder()
                .userId(2L)
                .nickname("쿠키")
                .profileImage("default_profile_image.png")
                .bio("개발해요")
                .position(new PositionResponse(1L, "프론트엔드"))
                .stacks(List.of(
                        new StackResponse(1L, "React", "react.jpg"),
                        new StackResponse(2L, "JavaScript", "javascript.jpeg")))
                .build();

        List<UserCardResponse> userCardResponses = createUserCardResponses();
        List<String> reviewComments = List.of(
                "항상 성실하게 참여하는 모습 보기 좋습니다."
        );

        return UserOtherDetailResponse.of(
                userDetailResponse,
                userCardResponses,
                reviewComments
        );
    }

    public static UserUpdateRequest createUserUpdateRequest() {
        return new UserUpdateRequest(
                "new_image.jpeg",
                "새로운 꼬부기",
                "프로필 수정했어요.",
                1L,
                List.of(1L, 2L)
        );
    }

}
