package dev.steady.steady.fixture;

import dev.steady.steady.domain.Steady;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.dto.request.SteadySearchRequest;
import dev.steady.steady.dto.request.SteadyUpdateRequest;
import dev.steady.user.domain.User;
import org.instancio.Instancio;

import java.time.LocalDate;
import java.util.List;

import static dev.steady.steady.domain.ScheduledPeriod.FIVE_MONTH;
import static dev.steady.steady.domain.SteadyMode.ONLINE;
import static dev.steady.steady.domain.SteadyType.STUDY;
import static org.instancio.Select.field;

public class SteadyFixturesV2 {

    public static SteadyCreateRequest generateSteadyCreateRequest(Long stackId, Long positionId) {
        return Instancio.of(SteadyCreateRequest.class)
                .generate(field(SteadyCreateRequest::name), gen -> gen.string().prefix("테스트 스테디"))
                .generate(field(SteadyCreateRequest::title), gen -> gen.string().prefix("스테디 제목"))
                .generate(field(SteadyCreateRequest::participantLimit), gen -> gen.ints().range(2, 10))
                .generate(field(SteadyCreateRequest::deadline), gen -> gen.temporal().localDate().future())
                .set(field(SteadyCreateRequest::stacks), List.of(stackId))
                .set(field(SteadyCreateRequest::positions), List.of(positionId))
                .create();
    }

    public static SteadyUpdateRequest generateSteadyUpdateRequest(Long stackId, Long positionId) {
        return Instancio.of(SteadyUpdateRequest.class)
                .generate(field(SteadyUpdateRequest::name), gen -> gen.string().prefix("테스트 스테디"))
                .generate(field(SteadyUpdateRequest::title), gen -> gen.string().prefix("스테디 제목"))
                .generate(field(SteadyUpdateRequest::participantLimit), gen -> gen.ints().range(2, 10))
                .generate(field(SteadyUpdateRequest::deadline), gen -> gen.temporal().localDate().future())
                .set(field(SteadyUpdateRequest::stacks), List.of(stackId))
                .set(field(SteadyUpdateRequest::positions), List.of(positionId))
                .create();
    }

    public static Steady createSteady(User leader) {
        return Steady.builder()
                .name("테스트 스테디")
                .bio("우리 스터디는 정말 열심히 합니다.")
                .contact("geonhee33@gmail.com")
                .type(STUDY)
                .participantLimit(5)
                .steadyMode(ONLINE)
                .scheduledPeriod(FIVE_MONTH)
                .deadline(LocalDate.of(2030, 1, 2))
                .title("스테디 제목")
                .content("스테디 본문")
                .leader(leader)
                .build();
    }

    public static SteadySearchRequest createDefaultSteadySearchRequest() {
        return new SteadySearchRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "false",
                null);
    }

    public static SteadySearchRequest createOrderByDeadLineSteadySearchRequest() {
        return new SteadySearchRequest(null,
                        null,
                        "asc",
                        "deadline",
                        null,
                        null,
                        null,
                        null,
                        "false",
                        null);
    }

}
