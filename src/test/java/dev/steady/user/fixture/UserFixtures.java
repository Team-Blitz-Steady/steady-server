package dev.steady.user.fixture;

import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.dto.request.UserCreateRequest;
import dev.steady.user.dto.request.UserUpdateRequest;
import dev.steady.user.dto.response.PositionResponse;
import dev.steady.user.dto.response.PositionsResponse;
import dev.steady.user.dto.response.StackResponse;
import dev.steady.user.dto.response.StacksResponse;

import java.util.List;

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

    public static UserUpdateRequest createUserUpdateRequest(Long positionId, List<Long> stackIds) {
        return new UserUpdateRequest(
                "new_image.jpg",
                "newNickname",
                "newBio",
                positionId,
                stackIds
        );
    }

}
