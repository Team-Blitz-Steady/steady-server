package dev.steady.steadyForm.fixture;

import dev.steady.steadyForm.domain.SteadyForm;
import dev.steady.user.domain.User;
import org.springframework.test.util.ReflectionTestUtils;

public class SteadyFormFixtures {

    public static SteadyForm createForm(User user) {
        SteadyForm steadyForm = SteadyForm.builder()
                .user(user)
                .name("설문 양식")
                .build();
        ReflectionTestUtils.setField(steadyForm, "id", 1L);
        return steadyForm;
    }

}
