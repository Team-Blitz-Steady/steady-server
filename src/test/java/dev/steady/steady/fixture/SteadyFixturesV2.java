package dev.steady.steady.fixture;

import dev.steady.application.dto.response.SliceResponse;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyPosition;
import dev.steady.steady.domain.SteadyQuestion;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.dto.request.SteadySearchRequest;
import dev.steady.steady.dto.request.SteadyUpdateRequest;
import dev.steady.steady.dto.response.MySteadyResponse;
import dev.steady.steady.dto.response.PageResponse;
import dev.steady.steady.dto.response.ParticipantResponse;
import dev.steady.steady.dto.response.ParticipantsResponse;
import dev.steady.steady.dto.response.SteadyQueryResponse;
import dev.steady.steady.dto.response.SteadyQuestionResponse;
import dev.steady.steady.dto.response.SteadyQuestionsResponse;
import dev.steady.steady.dto.response.SteadyRankResponse;
import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.fixture.UserFixturesV2;
import org.instancio.Instancio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static dev.steady.steady.domain.ScheduledPeriod.FIVE_MONTH;
import static dev.steady.steady.domain.SteadyMode.ONLINE;
import static dev.steady.steady.domain.SteadyStatus.CLOSED;
import static dev.steady.steady.domain.SteadyType.STUDY;
import static dev.steady.user.fixture.UserFixturesV2.generateStackEntity;
import static org.instancio.Select.field;

public class SteadyFixturesV2 {

    public static SteadyCreateRequest generateSteadyCreateRequest(Long stackId, Long positionId) {
        return Instancio.of(SteadyCreateRequest.class)
                .generate(field(SteadyCreateRequest::name), gen -> gen.string().prefix("스테디 이름"))
                .generate(field(SteadyCreateRequest::bio), gen -> gen.string().prefix("스테디 소개"))
                .generate(field(SteadyCreateRequest::title), gen -> gen.string().prefix("스테디 제목"))
                .generate(field(SteadyCreateRequest::content), gen -> gen.string().prefix("모집글 내용"))
                .generate(field(SteadyCreateRequest::participantLimit), gen -> gen.ints().range(2, 10))
                .generate(field(SteadyCreateRequest::deadline), gen -> gen.temporal().localDate().future())
                .set(field(SteadyCreateRequest::stacks), List.of(stackId))
                .set(field(SteadyCreateRequest::positions), List.of(positionId))
                .create();
    }

    public static SteadyUpdateRequest generateSteadyUpdateRequest(Long stackId, Long positionId) {
        return Instancio.of(SteadyUpdateRequest.class)
                .generate(field(SteadyUpdateRequest::name), gen -> gen.string().prefix("스테디 이름"))
                .generate(field(SteadyUpdateRequest::bio), gen -> gen.string().prefix("스테디 소개"))
                .generate(field(SteadyUpdateRequest::title), gen -> gen.string().prefix("스테디 제목"))
                .generate(field(SteadyUpdateRequest::content), gen -> gen.string().prefix("모집글 내용"))
                .generate(field(SteadyUpdateRequest::participantLimit), gen -> gen.ints().range(2, 10))
                .set(field(SteadyUpdateRequest::deadline), LocalDate.now().plusDays(7))
                .set(field(SteadyUpdateRequest::stacks), List.of(stackId))
                .set(field(SteadyUpdateRequest::positions), List.of(positionId))
                .create();
    }

    public static Steady createSteady(User leader, List<Stack> stacks) {
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
                .stacks(stacks)
                .build();
    }

    public static List<Steady> createSteadiesWithLikeCount(User leader, List<Stack> stacks, int likeCount) {
        return IntStream.rangeClosed(0, likeCount)
                .mapToObj(count -> createSteadyWithLikeCount(leader, stacks, count))
                .toList();
    }

    public static List<SteadyRankResponse> createSteadyRankResponses() {
        return List.of(
                new SteadyRankResponse(2L, "스테디 제목2", STUDY, CLOSED, LocalDate.of(2023, 12, 31), 10, 1),
                new SteadyRankResponse(2L, "스테디 제목2", STUDY, CLOSED, LocalDate.of(2023, 12, 15), 10, 3)
        );
    }

    public static Steady createSteadyWithStatus(User leader, List<Stack> stacks, SteadyStatus status) {
        Steady steady = Steady.builder()
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
                .stacks(stacks)
                .build();
        ReflectionTestUtils.setField(steady, "status", status);
        return steady;
    }

    public static Steady createSteadyEntity() {
        var leader = UserFixturesV2.generateUserEntity();
        var steady = createSteady(leader, List.of(generateStackEntity()));
        ReflectionTestUtils.setField(steady, "id", 1L);
        ReflectionTestUtils.setField(steady, "createdAt", LocalDateTime.of(2025, 12, 7, 11, 12));
        return steady;
    }

    public static SteadyPosition createSteadyPosition(Steady steady, Position position) {
        return SteadyPosition.builder().steady(steady).position(position).build();
    }

    public static List<SteadyQuestion> createSteadyQuestion(Steady steady, int size) {
        return Instancio.ofList(SteadyQuestion.class)
                .size(size)
                .set(field(SteadyQuestion::getSteady), steady)
                .generate(field(SteadyQuestion::getSteady), gen -> gen.intSeq().start(1))
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
                null,
                "false",
                null);
    }

    public static SteadySearchRequest createUnsatisfiedSteadySearchRequest() {
        return new SteadySearchRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "false",
                "말도 안 되는 검색조건입니다. 아무것도 이 조건에 걸리지 않습니다.");
    }

    public static SteadySearchRequest createOrderByDeadLineSteadySearchRequest() {
        return new SteadySearchRequest(
                null,
                "asc",
                "deadline",
                null,
                null,
                null,
                null,
                null,
                null,
                "false",
                null);
    }

    public static PageResponse<SteadyQueryResponse> createSteadyPageResponse(Steady steady, Pageable pageable) {
        Page<Steady> steadies = new PageImpl<>(List.of(steady), pageable, 1);
        return PageResponse.from(steadies.map(SteadyQueryResponse::from));
    }

    public static SteadyQuestionsResponse createSteadyQuestionsResponse() {
        return new SteadyQuestionsResponse(
                "스터디 제목",
                List.of(
                        new SteadyQuestionResponse(1L, "누구세요?", 1),
                        new SteadyQuestionResponse(2L, "뭐세요?", 2)
                ));
    }

    public static ParticipantsResponse createParticipantsResponse() {
        return new ParticipantsResponse(List.of(
                new ParticipantResponse(1L, "weonest", "url1", true),
                new ParticipantResponse(2L, "nayjk", "url2", false)
        ));
    }

        public static SliceResponse<MySteadyResponse> createMySteadyResponse() {
        return new SliceResponse<>(
                List.of(
                        new MySteadyResponse(1L, "스테디 제목", "email", true, LocalDateTime.of(2023, 12, 31, 11, 10)),
                        new MySteadyResponse(2L, "스테디 제목2", "email", false, LocalDateTime.of(2023, 12, 31, 11, 10))
                ),
                2,
                false
        );
    }

    private static Steady createSteadyWithLikeCount(User leader, List<Stack> stacks, int likeCount) {
        Steady steady = createSteady(leader, stacks);
        ReflectionTestUtils.setField(steady, "likeCount", likeCount);
        return steady;
    }

}
