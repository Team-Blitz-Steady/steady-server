package dev.steady.template.service;

import dev.steady.global.exception.ForbiddenException;
import dev.steady.global.exception.NotFoundException;
import dev.steady.template.domain.Question;
import dev.steady.template.domain.Template;
import dev.steady.template.domain.repository.QuestionRepository;
import dev.steady.template.domain.repository.TemplateRepository;
import dev.steady.template.dto.request.CreateTemplateRequest;
import dev.steady.template.dto.request.UpdateTemplateRequest;
import dev.steady.user.domain.Position;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.template.fixture.TemplateFixture.createAnotherTemplate;
import static dev.steady.template.fixture.TemplateFixture.createTemplate;
import static dev.steady.user.fixture.UserFixtures.createFirstUser;
import static dev.steady.user.fixture.UserFixtures.createPosition;
import static dev.steady.user.fixture.UserFixtures.createSecondUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class TemplateServiceTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Position position;
    private User user;

    @BeforeEach
    void setUp() {
        this.position = positionRepository.save(createPosition());
        this.user = userRepository.save(createFirstUser(position));
    }

    @AfterEach
    void tearDown() {
        questionRepository.deleteAll();
        templateRepository.deleteAll();
        userRepository.deleteAll();
        positionRepository.deleteAll();
    }

    @DisplayName("템플릿 양식을 입력받아 템플릿을 생성한다.")
    @Test
    void createTemplateTest() {
        var userInfo = createUserInfo(user.getId());
        var title = "Sample Title";
        var questions = List.of("Q1", "Q2");
        var request = new CreateTemplateRequest(title, questions);

        Long templateId = templateService.createTemplate(request, userInfo);

        List<Question> savedQuestions = questionRepository.findAll();
        Template template = templateRepository.findById(templateId).get();
        assertAll(
                () -> assertThat(template).isNotNull(),
                () -> assertThat(savedQuestions).hasSameSizeAs(questions)
        );
    }

    @DisplayName("사용자의 계정 정보를 받아 템플릿 목록을 조회한다.")
    @Test
    void getTemplatesTest() {
        var template1 = createTemplate(user);
        var template2 = createAnotherTemplate(user);
        templateRepository.saveAll(List.of(template1, template2));
        var userInfo = createUserInfo(user.getId());

        var responses = templateService.getTemplates(userInfo);

        assertThat(responses.templates()).hasSize(2)
                .extracting("title")
                .containsExactlyInAnyOrder(template1.getTitle(), template2.getTitle());
    }

    @DisplayName("템플릿 식별자를 통해 템플릿을 상세조회한다.")
    @Test
    void getDetailTemplateTest() {
        var template = createTemplate(user);
        var savedTemplate = templateRepository.save(template);
        var userInfo = createUserInfo(user.getId());

        var response = templateService.getDetailTemplate(userInfo, savedTemplate.getId());

        assertThat(response)
                .extracting("id", "title", "questions")
                .containsExactly(savedTemplate.getId(),
                        savedTemplate.getTitle(),
                        savedTemplate.getContents());
    }

    @DisplayName("템플릿 식별자를 통해 템플릿을 상세조회할 때 작성자가 아니라면 예외가 발생한다.")
    @Test
    void getDetailTemplateFailTest() {
        var anotherUser = userRepository.save(createSecondUser(position));
        var template = createTemplate(user);
        var savedTemplate = templateRepository.save(template);
        var userInfo = createUserInfo(anotherUser.getId());

        assertThatThrownBy(() -> templateService.getDetailTemplate(userInfo, savedTemplate.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("템플릿 수정 요청을 받아서 기존 템플릿을 업데이트한다.")
    @Test
    void updateTemplateTest() {
        var template = createTemplate(user);
        var savedTemplate = templateRepository.save(template);

        var questions = List.of("변경된 질문1", "변경된 질문2", "변경된 질문3");
        var title = "변경된 제목";
        var request = new UpdateTemplateRequest(title, questions);
        templateService.updateTemplate(savedTemplate.getId(), request, createUserInfo(user.getId()));

        Template updatedTemplate = transactionTemplate.execute(status -> {
            var findTemplate = templateRepository.findById(savedTemplate.getId()).get();
            findTemplate.getContents();
            return findTemplate;
        });
        assertAll(
                () -> assertThat(updatedTemplate.getTitle()).isEqualTo(title),
                () -> assertThat(updatedTemplate.getContents()).isEqualTo(questions));
    }

    @DisplayName("템플릿 작성자는 템플릿을 삭제할 수 있다.")
    @Test
    void deleteTemplateTest() {
        //given
        var template = createTemplate(user);
        var savedTemplate = templateRepository.save(template);

        //when
        templateService.deleteTemplate(createUserInfo(user.getId()), savedTemplate.getId());

        //then
        assertThatThrownBy(() -> templateRepository.getById(savedTemplate.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("템플릿 작성자가 아니면 템플릿을 삭제할 수 없다.")
    @Test
    void deleteTemplateFailTest() {
        //given
        var otherUser = createSecondUser(position);
        var savedUser2 = userRepository.save(otherUser);
        var template = createTemplate(user);
        var savedTemplate = templateRepository.save(template);
        var userInfo = createUserInfo(savedUser2.getId());

        //when
        //then
        assertThatThrownBy(() -> templateService.deleteTemplate(userInfo, savedTemplate.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

}
