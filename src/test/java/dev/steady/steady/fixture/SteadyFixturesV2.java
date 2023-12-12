package dev.steady.steady.fixture;

import dev.steady.steady.dto.request.SteadyCreateRequest;
import org.instancio.Instancio;

import java.util.List;

import static org.instancio.Select.field;

public class SteadyFixturesV2 {

    public static SteadyCreateRequest generateSteadyCreateRequest() {
        return Instancio.of(SteadyCreateRequest.class)
                .generate(field(SteadyCreateRequest::name), gen -> gen.string().prefix("테스트 스테디"))
                .generate(field(SteadyCreateRequest::title), gen -> gen.string().prefix("스테디 제목"))
                .generate(field(SteadyCreateRequest::participantLimit), gen -> gen.ints().range(2, 10))
                .generate(field(SteadyCreateRequest::deadline), gen -> gen.temporal().localDate().future())
                .set(field(SteadyCreateRequest::stacks), List.of(1L))
                .set(field(SteadyCreateRequest::positions), List.of(1L))
                .create();
    }

}
