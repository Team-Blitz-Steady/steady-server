package dev.steady.user.fixture;

import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import org.instancio.Instancio;

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

}
