package dev.steady.steady.fixture;

import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.dto.request.SteadySearchRequest;
import dev.steady.steady.dto.request.SteadyUpdateRequest;
import org.instancio.Instancio;

import java.util.List;

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

}
