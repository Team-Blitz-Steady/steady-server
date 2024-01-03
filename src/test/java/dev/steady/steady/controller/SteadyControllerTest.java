package dev.steady.steady.controller;

import com.epages.restdocs.apispec.Schema;
import dev.steady.application.dto.response.SliceResponse;
import dev.steady.global.auth.Authentication;
import dev.steady.global.auth.UserInfo;
import dev.steady.global.config.ControllerTestConfig;
import dev.steady.steady.dto.FilterConditionDto;
import dev.steady.steady.dto.RankCondition;
import dev.steady.steady.dto.request.RankParams;
import dev.steady.steady.dto.request.SteadyPageRequest;
import dev.steady.steady.dto.request.SteadyQuestionUpdateRequest;
import dev.steady.steady.dto.response.MySteadyResponse;
import dev.steady.steady.dto.response.ParticipantsResponse;
import dev.steady.steady.dto.response.SteadyDetailResponse;
import dev.steady.steady.dto.response.SteadyQuestionsResponse;
import dev.steady.steady.dto.response.SteadyRankResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.steady.domain.SteadyStatus.RECRUITING;
import static dev.steady.steady.fixture.SteadyFixturesV2.createDefaultSteadySearchRequest;
import static dev.steady.steady.fixture.SteadyFixturesV2.createMySteadyResponse;
import static dev.steady.steady.fixture.SteadyFixturesV2.createParticipantsResponse;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteadyEntity;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteadyPageResponse;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteadyPosition;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteadyQuestionsResponse;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteadyRankResponses;
import static dev.steady.steady.fixture.SteadyFixturesV2.generateSteadyCreateRequest;
import static dev.steady.steady.fixture.SteadyFixturesV2.generateSteadyUpdateRequest;
import static dev.steady.user.fixture.UserFixturesV2.generatePositionEntity;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SteadyControllerTest extends ControllerTestConfig {

    @Test
    @DisplayName("새로운 스테디를 생성하고 생성된 스테디 조회 URL 을 반환한다.")
    void createSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);
        var steadyRequest = generateSteadyCreateRequest(1L, 1L);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        given(steadyService.create(steadyRequest, userInfo)).willReturn(steadyId);

        // when & then
        mockMvc.perform(post("/api/v1/steadies")
                        .header(AUTHORIZATION, TOKEN)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(steadyRequest)))
                .andDo(document("steady-create",
                        resourceDetails().tag("스테디").description("스테디 생성 요청")
                                .requestSchema(Schema.schema("SteadyCreateRequest")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        requestFields(
                                fieldWithPath("name").type(STRING).description("스테디 이름"),
                                fieldWithPath("bio").type(STRING).description("스테디 소개"),
                                fieldWithPath("contact").type(STRING).description("연락 수"),
                                fieldWithPath("type").type(STRING).description("스테디 종류"),
                                fieldWithPath("participantLimit").type(NUMBER).description("모집 정원"),
                                fieldWithPath("steadyMode").type(STRING).description("스테디 진행 방식"),
                                fieldWithPath("scheduledPeriod").type(STRING).description("스테디 시작 예정일"),
                                fieldWithPath("deadline").type(STRING).description("모집 마감일"),
                                fieldWithPath("title").type(STRING).description("모집글 제목"),
                                fieldWithPath("content").type(STRING).description("모집글 내용"),
                                fieldWithPath("positions").type(ARRAY).description("스테디 모집 분야"),
                                fieldWithPath("stacks").type(ARRAY).description("스테디 기술 스택"),
                                fieldWithPath("questions").type(ARRAY).description("스테디 질문 리스트")
                        )
                ))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl(String.format("/api/v1/steadies/%d", steadyId)));
    }

    @Test
    @DisplayName("전체 스테디를 반환한다.")
    void findMySteadiesTest() throws Exception {
        // given
        var userId = 1L;
        var request = new SteadyPageRequest(0, "desc");
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        Pageable pageable = request.toPageable();
        SliceResponse<MySteadyResponse> response = createMySteadyResponse();
        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        given(steadyService.findMySteadies(RECRUITING, userInfo, pageable)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/steadies/my")
                        .param("status", "RECRUITING")
                        .param("page", "0")
                        .param("direction", "desc")
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("search-my-steady",
                        resourceDetails().tag("스테디").description("내 스테디 전체 및 필터링 조회")
                                .responseSchema(Schema.schema("MySteadyResponse")),
                        queryParameters(
                                parameterWithName("status").description("스테디 상태"),
                                parameterWithName("page").description("페이지 넘버"),
                                parameterWithName("direction").description("내림/오름차순")
                        ),
                        responseFields(
                                fieldWithPath("content[]").type(ARRAY).description("내 스테디 목록"),
                                fieldWithPath("content[].steadyId").type(NUMBER).description("스테디 식별자"),
                                fieldWithPath("content[].name").type(STRING).description("스테디 제목"),
                                fieldWithPath("content[].contact").type(STRING).description("연락 수"),
                                fieldWithPath("content[].isLeader").type(BOOLEAN).description("리더 여부"),
                                fieldWithPath("content[].joinedAt").type(STRING).description("스테디 참여 시간"),
                                fieldWithPath("numberOfElements").type(NUMBER).description("조회된 스테디 갯수"),
                                fieldWithPath("hasNext").type(BOOLEAN).description("다음페이지 유무")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("조건에 맞는 인기 게시물 리스트를 반환합니다.")
    void findPopularStudyTest() throws Exception {
        // given
        var params = new RankParams(LocalDate.of(2023, 12, 31), 10, "ALL");

        RankCondition condition = params.toCondition();
        List<SteadyRankResponse> response = createSteadyRankResponses();
        given(steadyService.findPopularStudies(condition)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/steadies/rank")
                        .param("date", "2023-12-31")
                        .param("limit", "10")
                        .param("type", "ALL")
                )
                .andDo(document("popular-steady",
                        queryParameters(
                                parameterWithName("date").description("기준 날짜 이후 생성 스테디"),
                                parameterWithName("limit").description("제한 10개 이하 가능"),
                                parameterWithName("type").description("ALL, STUDY, PROJECT")
                        ),
                        responseFields(
                                fieldWithPath("[].steadyId").type(NUMBER).description("스테디 식별자"),
                                fieldWithPath("[].title").type(STRING).description("스테디 제목"),
                                fieldWithPath("[].type").type(STRING).description("스테디 타입"),
                                fieldWithPath("[].status").type(STRING).description("스테디 상태"),
                                fieldWithPath("[].deadline").type(STRING).description("마감일"),
                                fieldWithPath("[].viewCount").type(NUMBER).description("조회수"),
                                fieldWithPath("[].likeCount").type(NUMBER).description("좋아요 수")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("검색 조건에 따른 전체 조회 결과를 반환한다.")
    void getSteadiesByConditionTest() throws Exception {
        // given
        var searchRequest = createDefaultSteadySearchRequest();
        MultiValueMap params = new LinkedMultiValueMap<>() {{
            add("steadyType", null);
            add("page", null);
            add("direction", null);
            add("criteria", null);
            add("steadyMode", null);
            add("stack", null);
            add("position", null);
            add("status", null);
            add("like", "false");
            add("keyword", null);
        }};

        var pageable = searchRequest.toPageable();
        var condition = FilterConditionDto.from(searchRequest);
        var steady = createSteadyEntity();
        var response = createSteadyPageResponse(steady, pageable);

        given(steadyService.getSteadies(eq(new UserInfo(null)), any(FilterConditionDto.class), eq(pageable))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/steadies/search")
                        .queryParams(params))
                .andDo(document("steady-get-steadies-by-condition",
                        resourceDetails().tag("스테디").description("스테디 검색 및 필터링 조회")
                                .responseSchema(Schema.schema("PageResponse")),
                        queryParameters(
                                parameterWithName("page").description("요청 페이지 번호"),
                                parameterWithName("direction").description("내림/오름차순").optional(),
                                parameterWithName("criteria").description("정렬 조건").optional(),
                                parameterWithName("cursor").description("페이징 커서").optional(),
                                parameterWithName("steadyType").description("스테디 타입").optional(),
                                parameterWithName("steadyMode").description("스테디 진행 방식").optional(),
                                parameterWithName("stack").description("스테디 기술 스택").optional(),
                                parameterWithName("position").description("스테디 포지션").optional(),
                                parameterWithName("status").description("스테디 상태").optional(),
                                parameterWithName("like").description("내 좋아요"),
                                parameterWithName("keyword").description("검색 키워드").optional()
                        ),
                        responseFields(
                                fieldWithPath("content[].id").type(NUMBER).description("스테디 id"),
                                fieldWithPath("content[].nickname").type(STRING).description("스테디 리더 닉네임"),
                                fieldWithPath("content[].profileImage").type(STRING).description("스테디 리더 프로필 이미지"),
                                fieldWithPath("content[].title").type(STRING).description("모집글 제목"),
                                fieldWithPath("content[].type").type(STRING).description("스테디 분류"),
                                fieldWithPath("content[].status").type(STRING).description("스테디 상태"),
                                fieldWithPath("content[].deadline").type(STRING).description("모집 마감일"),
                                fieldWithPath("content[].createdAt").type(STRING).description("스테디 생성일"),
                                fieldWithPath("content[].promotedAt").type(STRING).description("스테디 끌어올린 시간"),
                                fieldWithPath("content[].participantLimit").type(NUMBER).description("모집 정원"),
                                fieldWithPath("content[].numberOfParticipants").type(NUMBER).description("스테디 참여 인원"),
                                fieldWithPath("content[].viewCount").type(NUMBER).description("조회수"),
                                fieldWithPath("content[].stacks[].id").type(NUMBER).description("기술 스택 id"),
                                fieldWithPath("content[].stacks[].name").type(STRING).description("기술 스택명"),
                                fieldWithPath("content[].stacks[].imageUrl").type(STRING).description("기술 스택 이미지"),
                                fieldWithPath("content[].likeCount").type(NUMBER).description("좋아요 수"),
                                fieldWithPath("numberOfElements").type(NUMBER).description("현재 페이지 조회된 개수"),
                                fieldWithPath("page").type(NUMBER).description("현재 페이지"),
                                fieldWithPath("size").type(NUMBER).description("페이지 크기"),
                                fieldWithPath("totalPages").type(NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("totalElements").type(NUMBER).description("전체 개수")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("스테디 Id를 통해 스테디를 상세 조회할 수 있다.")
    void getDetailSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        var position = generatePositionEntity();
        var steady = createSteadyEntity();
        var steadyPosition = createSteadyPosition(steady, position);
        var response = SteadyDetailResponse.of(
                steady,
                List.of(steadyPosition),
                true,
                1L,
                false);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        given(steadyService.getDetailSteady(steadyId, userInfo)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/steadies/{steadyId}", steadyId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-get-detail-steady",
                        resourceDetails().tag("스테디").description("스테디 상세조회")
                                .responseSchema(Schema.schema("SteadyDetailResponse")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰").optional()
                        ),
                        responseFields(
                                fieldWithPath("id").type(NUMBER).description("스테디 id"),
                                fieldWithPath("leaderResponse.id").type(NUMBER).description("스테디 리더 id"),
                                fieldWithPath("leaderResponse.nickname").type(STRING).description("스테디 리더 닉네임"),
                                fieldWithPath("leaderResponse.profileImage").type(STRING).description("스테디 리더 프로필 이미지"),
                                fieldWithPath("name").type(STRING).description("스테디 이름"),
                                fieldWithPath("bio").type(STRING).description("스테디 소개"),
                                fieldWithPath("contact").type(STRING).description("연락 수단"),
                                fieldWithPath("type").type(STRING).description("스테디 유형"),
                                fieldWithPath("status").type(STRING).description("스테디 상태"),
                                fieldWithPath("participantLimit").type(NUMBER).description("모집 정원"),
                                fieldWithPath("numberOfParticipants").type(NUMBER).description("스테디 참여 인원"),
                                fieldWithPath("steadyMode").type(STRING).description("스테디 진행 방식"),
                                fieldWithPath("scheduledPeriod").type(STRING).description("예상 기간"),
                                fieldWithPath("deadline").type(STRING).description("마감일"),
                                fieldWithPath("title").type(STRING).description("모집글 제목"),
                                fieldWithPath("content").type(STRING).description("모집글 내용"),
                                fieldWithPath("viewCount").type(NUMBER).description("조회수"),
                                fieldWithPath("positions[].id").type(NUMBER).description("포지션 id"),
                                fieldWithPath("positions[].name").type(STRING).description("포지션 이름"),
                                fieldWithPath("stacks[].id").type(NUMBER).description("기술 스택 id"),
                                fieldWithPath("stacks[].name").type(STRING).description("기술 스택명"),
                                fieldWithPath("stacks[].imageUrl").type(STRING).description("기술 스택 이미지"),
                                fieldWithPath("isLeader").type(BOOLEAN).description("리더 여부"),
                                fieldWithPath("applicationId").type(NUMBER).description("신청서 식별자"),
                                fieldWithPath("promotionCount").type(NUMBER).description("끌어올리가 남은 횟수"),
                                fieldWithPath("createdAt").type(STRING).description("스테디 생성일"),
                                fieldWithPath("finishedAt").type(STRING).description("스테디 종료일").optional(),
                                fieldWithPath("isReviewEnabled").type(BOOLEAN).description("리뷰 작성 가능 여부"),
                                fieldWithPath("isLiked").type(BOOLEAN).description("좋아요 여부")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("스테디 식별자를 통해 스테디 질문을 조회할 수 있다.")
    void getSteadyQuestionsTest() throws Exception {
        var steadyId = 1L;
        SteadyQuestionsResponse steadyQuestionsResponse = createSteadyQuestionsResponse();

        given(steadyService.getSteadyQuestions(steadyId)).willReturn(steadyQuestionsResponse);

        mockMvc.perform(get("/api/v1/steadies/{steadyId}/questions", steadyId))
                .andDo(document("steady-get-steadyQuestions",
                        resourceDetails().tag("스테디").description("스테디 질문 조회")
                                .responseSchema(Schema.schema("SteadyQuestionsResponse")),
                        responseFields(
                                fieldWithPath("steadyName").type(STRING).description("스테디 제목"),
                                fieldWithPath("steadyQuestions[].id").type(NUMBER).description("스테디 질문 식별자"),
                                fieldWithPath("steadyQuestions[].content").type(STRING).description("질문 내용"),
                                fieldWithPath("steadyQuestions[].sequence").type(NUMBER).description("질문 순서")
                        )
                )).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(steadyQuestionsResponse)));
    }

    @Test
    @DisplayName("스테디 식별자를 통해 스테디 참여자 목록을 조회할 수 있다.")
    void getSteadyParticipantsTest() throws Exception {
        var steadyId = 1L;
        ParticipantsResponse participantsResponse = createParticipantsResponse();

        given(steadyService.getSteadyParticipants(steadyId)).willReturn(participantsResponse);

        mockMvc.perform(get("/api/v1/steadies/{steadyId}/participants", steadyId))
                .andDo(document("steady-get-participants",
                        resourceDetails().tag("스테디").description("스테디 참여자 조회")
                                .responseSchema(Schema.schema("ParticipantsResponse")),
                        responseFields(
                                fieldWithPath("participants[].id").type(NUMBER).description("참여자 식별자"),
                                fieldWithPath("participants[].nickname").type(STRING).description("참여자 닉네임"),
                                fieldWithPath("participants[].profileImage").type(STRING).description("참여자 프로필 이미지"),
                                fieldWithPath("participants[].isLeader").type(BOOLEAN).description("리더 여부")
                        )
                )).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(participantsResponse)));
    }

    @Test
    @DisplayName("스테디 식별자를 통해 스테디 정보를 수정할 수 있다.")
    void updateSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);
        var request = generateSteadyUpdateRequest(1L, 1L);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).updateSteady(steadyId, request, userInfo);

        // when & then
        mockMvc.perform(patch("/api/v1/steadies/{steadyId}", steadyId)
                        .header(AUTHORIZATION, TOKEN)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(document("steady-update",
                        resourceDetails().tag("스테디").description("스테디 수정")
                                .requestSchema(Schema.schema("SteadyUpdateRequest")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        requestFields(
                                fieldWithPath("name").type(STRING).description("스테디 이름"),
                                fieldWithPath("bio").type(STRING).description("스테디 소개"),
                                fieldWithPath("contact").type(STRING).description("연락수단"),
                                fieldWithPath("type").type(STRING).description("스테디 종류"),
                                fieldWithPath("status").type(STRING).description("스테디 상태"),
                                fieldWithPath("participantLimit").type(NUMBER).description("모집 정원"),
                                fieldWithPath("steadyMode").type(STRING).description("스테디 진행 방식"),
                                fieldWithPath("scheduledPeriod").type(STRING).description("스테디 시작 예정일"),
                                fieldWithPath("deadline").type(STRING).description("모집 마감일"),
                                fieldWithPath("title").type(STRING).description("모집글 제목"),
                                fieldWithPath("content").type(STRING).description("모집글 내용"),
                                fieldWithPath("positions").type(ARRAY).description("스테디 모집 분야"),
                                fieldWithPath("stacks").type(ARRAY).description("스테디 기술 스택")
                        )
                ))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("스테디 식별자를 통해 스테디 질문을 수정할 수 있다.")
    void updateSteadyQuestionsTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var userInfo = createUserInfo(userId);
        var authentication = new Authentication(userId);
        var steadyQuestionUpdateRequest = new SteadyQuestionUpdateRequest(List.of("변경된 질문1", "변경된 질문2"));

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).updateSteadyQuestions(steadyId, steadyQuestionUpdateRequest, userInfo);

        //when & then
        mockMvc.perform(patch("/api/v1/steadies/{steadyId}/questions", steadyId)
                        .header(AUTHORIZATION, TOKEN)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(steadyQuestionUpdateRequest)))
                .andDo(document("steady-update-steady-questions",
                        resourceDetails().tag("스테디").description("스테디 질문 수정")
                                .requestSchema(Schema.schema("SteadyQuestionUpdateRequest")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        requestFields(
                                fieldWithPath("questions").type(ARRAY).description("질문 목록")
                        )
                )).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("스테디 리더는 스테디 참여자를 추방할 수 있다.")
    void expelParticipantTest() throws Exception {
        // given
        var steadyId = 1L;
        var leaderId = 1L;
        var memberId = 1L;
        var authentication = new Authentication(leaderId);
        var userInfo = createUserInfo(leaderId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).expelParticipant(steadyId, memberId, userInfo);

        // when & then
        mockMvc.perform(patch("/api/v1/steadies/{steadyId}/{memberId}", steadyId, memberId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-expel-participant",
                        resourceDetails().tag("스테디").description("스테디 참여자 추방하기"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("스테디 리더의 끌어올리기 요청을 통해 스테디를 끌어올릴 수 있다.")
    void promoteSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).promoteSteady(steadyId, userInfo);

        // when & then
        mockMvc.perform(patch("/api/v1/steadies/{steadyId}/promote", steadyId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-promote",
                        resourceDetails().tag("스테디").description("스테디 끌어올리기"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("스테디 리더의 종료 요청을 통해 스테디를 끌어올릴 수 있다.")
    void finishSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).promoteSteady(steadyId, userInfo);

        // when & then
        mockMvc.perform(patch("/api/v1/steadies/{steadyId}/finish", steadyId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-finish",
                        resourceDetails().tag("스테디").description("스테디 종료"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("스테디 삭제 요청을 통해 스테디를 삭제할 수 있다.")
    void deleteSteadyTest() throws Exception {
        // given
        var steadyId = 1L;
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(steadyService).promoteSteady(steadyId, userInfo);

        // when & then
        mockMvc.perform(delete("/api/v1/steadies/{steadyId}", steadyId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-delete",
                        resourceDetails().tag("스테디").description("스테디 삭제"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )))
                .andExpect(status().isNoContent());
    }

}
