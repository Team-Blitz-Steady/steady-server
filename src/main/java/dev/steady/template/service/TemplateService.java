package dev.steady.template.service;

import dev.steady.global.auth.AuthContext;
import dev.steady.template.domain.Question;
import dev.steady.template.domain.Template;
import dev.steady.template.domain.repository.QuestionRepository;
import dev.steady.template.domain.repository.TemplateRepository;
import dev.steady.template.dto.request.CreateTemplateRequest;
import dev.steady.template.dto.request.UpdateTemplateRequest;
import dev.steady.template.dto.resonse.TemplateDetailResponse;
import dev.steady.template.dto.resonse.TemplateResponses;
import dev.steady.user.domain.User;
import dev.steady.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createTemplate(CreateTemplateRequest request, AuthContext authContext) {
        User user = userRepository.getUserBy(authContext.getUserId());

        Template template = Template.create(user, request.title(), request.questions());
        Template savedTemplate = templateRepository.save(template);

        return savedTemplate.getId();
    }

    @Transactional(readOnly = true)
    public TemplateResponses getTemplates(AuthContext authContext) {
        User user = userRepository.getUserBy(authContext.getUserId());

        List<Template> templates = templateRepository.findByUserId(user.getId());
        return TemplateResponses.from(templates);
    }

    @Transactional(readOnly = true)
    public TemplateDetailResponse getDetailTemplate(AuthContext authContext, Long templateId) {
        User user = userRepository.getUserBy(authContext.getUserId());

        Template template = templateRepository.findById(templateId)
                .orElseThrow(IllegalArgumentException::new);
        template.validateOwner(user);
        return TemplateDetailResponse.from(template);
    }

    @Transactional
    public void updateTemplate(Long templateId, UpdateTemplateRequest request, AuthContext authContext) {
        User user = userRepository.getUserBy(authContext.getUserId());
        Template template = templateRepository.findById(templateId)
                .orElseThrow(IllegalArgumentException::new);

        template.update(user, request.title(), request.questions());
        templateRepository.save(template);
    }

    private List<Question> createQuestions(List<String> questions, Template template) {
        return questions.stream()
                .map(question -> new Question(template, question))
                .toList();
    }

}
