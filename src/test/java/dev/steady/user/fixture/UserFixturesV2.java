package dev.steady.user.fixture;

import dev.steady.user.domain.Position;
import dev.steady.user.domain.User;
import org.instancio.Instancio;

import static org.instancio.Select.field;

public class UserFixturesV2 {

    public static User generateUser(Position position) {
        return Instancio.of(User.class)
                .set(field(User::getPosition), position)
                .create();
    }

}
