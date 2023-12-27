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
import dev.steady.user.dto.response.PutObjectUrlResponse;
import dev.steady.user.dto.response.StackResponse;
import dev.steady.user.dto.response.StacksResponse;
import dev.steady.user.dto.response.UserDetailResponse;
import dev.steady.user.dto.response.UserMyDetailResponse;
import dev.steady.user.dto.response.UserOtherDetailResponse;
import org.instancio.Instancio;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static dev.steady.review.fixture.ReviewFixture.createUserCardResponses;
import static org.instancio.Select.field;

public class UserFixturesV2 {

    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://team-13-image-bucket.s3.ap-northeast-2.amazonaws.com/profile/steady_default_profile.svg";

    public static User generateUser(Position position) {
        return Instancio.of(User.class)
                .ignore(field(User::getId))
                .ignore(field(User::isDeleted))
                .set(field(User::getProfileImage), DEFAULT_PROFILE_IMAGE_URL)
                .set(field(User::getPosition), position)
                .create();
    }

    public static User generateUserEntity() {
        return Instancio.of(User.class)
                .ignore(field(User::isDeleted))
                .set(field(User::getId), 1L)
                .set(field(User::getProfileImage), DEFAULT_PROFILE_IMAGE_URL)
                .set(field(User::getPosition), generatePositionEntity())
                .create();
    }

    public static List<Stack> generateStacks() {
        return List.of(generateStack(), generateStack());
    }

    public static UserCreateRequest createUserCreateRequest() {
        return new UserCreateRequest(
                1L,
                "닉네임",
                1L,
                List.of(1L, 2L)
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

    public static Position generatePosition() {
        return Instancio.of(Position.class)
                .ignore(field(Position::getId))
                .create();
    }

    public static Position generatePositionEntity() {
        return Instancio.of(Position.class)
                .set(field(Position::getId), 1L)
                .set(field(Position::getName), "포지션")
                .create();
    }

    public static List<Position> createPositions() {
        return List.of(generatePosition(), generatePosition());
    }

    public static Stack generateStack() {
        return Instancio.of(Stack.class)
                .ignore(field(Stack::getId))
                .create();
    }

    public static Stack generateStackEntity() {
        return Instancio.of(Stack.class)
                .set(field(Stack::getId), 1L)
                .set(field(Stack::getName), "Java")
                .create();
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

    public static PutObjectUrlResponse createProfileUploadUrlResponse() {
        String presignedUrl = UriComponentsBuilder
                .fromUriString("bucket-name.s3.region.amazonaws.com/path/{fileName}")
                .queryParam("X-Amz-Algorithm", "{Algorithm}")
                .queryParam("X-Amz-Date", "{Date}")
                .queryParam("X-Amz-SignedHeaders", "{SignedHeaders}")
                .queryParam("X-Amz-Credential", "{Credential}")
                .queryParam("X-Amz-Expires", "{Expires}")
                .queryParam("X-Amz-Signature", "{Signature}")
                .build().toString();
        String objectUrl = "https:{bucket_name}.s3.{region}.com/{key}";
        return PutObjectUrlResponse.of(presignedUrl, objectUrl);
    }

}
