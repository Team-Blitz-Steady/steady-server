package dev.steady.steady.application;

import dev.steady.steadyForm.domain.SteadyForm;
import dev.steady.steadyForm.fixture.SteadyFormFixtures;
import dev.steady.steadyForm.infrastructure.SteadyFormRepository;
import dev.steady.global.auth.AuthContext;
import dev.steady.steady.domain.Promotion;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.fixture.SteadyFixtures;
import dev.steady.steady.infrastructure.SteadyRepository;
import dev.steady.user.domain.User;
import dev.steady.user.fixture.UserFixtures;
import dev.steady.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SteadyServiceTest {

    @InjectMocks
    private SteadyService steadyService;

    @Mock
    private SteadyRepository steadyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SteadyFormRepository steadyFormRepository;

    @Mock
    private AuthContext authContext;

    @Test
    @DisplayName("스터디 생성 요청을 통해 스터디를 생성할 수 있다")
    void createSteadyTest() {
        // given
        User user = UserFixtures.createUser();
        SteadyCreateRequest steadyRequest = SteadyFixtures.createSteadyRequest();
        SteadyForm steadyForm = SteadyFormFixtures.createForm(user);
        Promotion promotion = SteadyFixtures.createPromotion();
        Steady steady = SteadyFixtures.createSteady(steadyRequest, promotion, steadyForm);

        given(steadyFormRepository.findById(any())).willReturn(Optional.ofNullable(steadyForm));
        given(authContext.getUserId()).willReturn(user.getId());
        given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
        given(steadyRepository.save(any())).willReturn(steady);

        // when
        Long returnedId = steadyService.create(steadyRequest);

        // then
        assertThat(returnedId).isEqualTo(steady.getId());
    }

}
