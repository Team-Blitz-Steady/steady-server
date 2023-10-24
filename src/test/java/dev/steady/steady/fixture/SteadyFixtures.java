package dev.steady.steady.fixture;

import dev.steady.steadyForm.domain.SteadyForm;
import dev.steady.steady.domain.Promotion;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyMode;
import dev.steady.steady.domain.SteadyType;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

public class SteadyFixtures {

    public static SteadyCreateRequest createSteadyRequest() {
        return SteadyCreateRequest.builder()
                .name("테스트 스테디")
                .type(SteadyType.STUDY)
                .recruit_count(6)
                .steadyMode(SteadyMode.ONLINE)
                .estimate(LocalDate.now().plusDays(7))
                .deadline(LocalDate.now().plusDays(14))
                .title("스테디원 모집합니다")
                .content("많관부")
                .formId(1L)
                .build();
    }

    public static Steady createSteady(SteadyCreateRequest request, Promotion promotion, SteadyForm steadyForm) {
        Steady steady = request.toEntity(promotion, steadyForm);
        ReflectionTestUtils.setField(steady, "id", 1L);
        return steady;
    }

    public static Promotion createPromotion() {
        int promotionCount = 3;
        Promotion promotion = new Promotion(promotionCount);
        return promotion;
    }

}
