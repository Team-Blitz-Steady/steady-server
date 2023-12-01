package dev.steady.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.steady.application.controller.ApplicationController;
import dev.steady.application.service.ApplicationService;
import dev.steady.auth.config.JwtProperties;
import dev.steady.auth.controller.OAuthController;
import dev.steady.auth.domain.JwtResolver;
import dev.steady.auth.oauth.service.OAuthService;
import dev.steady.auth.service.AccountService;
import dev.steady.global.auth.AuthContext;
import dev.steady.notification.controller.NotificationController;
import dev.steady.notification.service.NotificationService;
import dev.steady.review.controller.ReviewController;
import dev.steady.review.service.ReviewService;
import dev.steady.steady.controller.SteadyController;
import dev.steady.steady.controller.SteadyLikeController;
import dev.steady.steady.service.SteadyLikeService;
import dev.steady.steady.service.SteadyService;
import dev.steady.storage.controller.StorageImageController;
import dev.steady.storage.service.StorageService;
import dev.steady.template.controller.TemplateController;
import dev.steady.template.service.TemplateService;
import dev.steady.user.controller.PositionController;
import dev.steady.user.controller.StackController;
import dev.steady.user.controller.UserController;
import dev.steady.user.service.PositionService;
import dev.steady.user.service.StackService;
import dev.steady.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest({
        OAuthController.class,
        UserController.class,
        SteadyController.class,
        SteadyLikeController.class,
        TemplateController.class,
        ApplicationController.class,
        StackController.class,
        PositionController.class,
        NotificationController.class,
        ReviewController.class,
        StorageImageController.class,
        AuthContext.class,
        JwtResolver.class,
        JwtProperties.class,
        PropertiesConfig.class
})
@ExtendWith(RestDocumentationExtension.class)
public abstract class ControllerTestConfig {

    protected final static String TOKEN = "Bearer aaaaaa.bbbbbb.cccccc";

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected MockMvc mockMvc;
    @MockBean
    protected OAuthService oAuthService;
    @MockBean
    protected AccountService accountService;
    @MockBean
    protected UserService userService;
    @MockBean
    protected StackService stackService;
    @MockBean
    protected PositionService positionService;
    @MockBean
    protected SteadyService steadyService;
    @MockBean
    protected SteadyLikeService steadyLikeService;
    @MockBean
    protected TemplateService templateService;
    @MockBean
    protected ApplicationService applicationService;
    @MockBean
    protected NotificationService notificationService;
    @MockBean
    protected ReviewService reviewService;
    @MockBean
    protected StorageService storageService;
    @MockBean
    protected JwtResolver jwtResolver;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }

}
