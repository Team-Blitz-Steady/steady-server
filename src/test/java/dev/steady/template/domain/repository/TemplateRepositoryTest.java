package dev.steady.template.domain.repository;

import dev.steady.global.config.JpaConfig;
import dev.steady.global.config.QueryDslConfig;
import dev.steady.template.domain.Template;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.UserRepository;
import dev.steady.user.fixture.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static dev.steady.template.fixture.TemplateFixture.createTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaConfig.class, QueryDslConfig.class})
class TemplateRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private PositionRepository positionRepository;

    @DisplayName("템플릿을 생성한 유저의 ID로 조회할 수 있다.")
    @Test
    void findTemplateUserIdTest() {
        var position = positionRepository.save(UserFixtures.createPosition());
        var user1 = UserFixtures.createFirstUser(position);
        var savedUser1 = userRepository.save(user1);

        var user2 = UserFixtures.createSecondUser(position);
        var savedUser2 = userRepository.save(user2);

        var template1 = createTemplate(savedUser1);
        var template2 = createTemplate(savedUser1);
        var template3 = createTemplate(savedUser2);
        templateRepository.saveAll(List.of(template1, template2, template3));

        List<Template> templates = templateRepository.findByUserId(savedUser1.getId());

        assertThat(templates.size()).isEqualTo(2);
    }

}
