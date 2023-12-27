package dev.steady.template.domain;

import dev.steady.global.exception.ForbiddenException;
import dev.steady.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static dev.steady.template.fixture.TemplateFixture.createTemplate;
import static dev.steady.user.fixture.UserFixturesV2.generatePosition;
import static dev.steady.user.fixture.UserFixturesV2.generateUser;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateTest {

    @DisplayName("템플릿의 생성자가 아니면 예외가 발생한다.")
    @Test
    void validateOwnerTest() {
        var position = generatePosition();
        User user = generateUser(position);
        ReflectionTestUtils.setField(user, "id", 1L);
        Template template = createTemplate(user);

        assertThatThrownBy(() -> template.validateOwner(generateUser(position)))
                .isInstanceOf(ForbiddenException.class);
    }

}
