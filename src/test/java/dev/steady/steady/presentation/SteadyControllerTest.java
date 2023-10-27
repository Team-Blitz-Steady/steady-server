package dev.steady.steady.presentation;

import com.epages.restdocs.apispec.Schema;
import dev.steady.global.config.ControllerTestConfig;
import dev.steady.steady.domain.Promotion;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.fixture.SteadyFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SteadyControllerTest extends ControllerTestConfig {

    @Test
    @DisplayName("새로운 스테디를 생성하고 조회 URI를 반환한다.")
    void createSteadyTest() throws Exception {
        // given
        SteadyCreateRequest steadyRequest = SteadyFixtures.createSteadyRequest();
        Promotion promotion = SteadyFixtures.createPromotion();
        Steady steady = SteadyFixtures.createSteady(steadyRequest, promotion);

        given(steadyService.create(steadyRequest)).willReturn(steady.getId());

        mockMvc.perform(post("/api/v1/steadies")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(steadyRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, String.format("/api/v1/steadies/%d/detail", steady.getId())))
                .andDo(document("steady-create",
                        resourceDetails().tag("스테디").description("스테디 생성 요청")
                                .requestSchema(Schema.schema("SteadyCreateRequest")),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("토큰")
                        ),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("스테디 이름"),
                                fieldWithPath("type").type(JsonFieldType.STRING).description("스테디 종류"),
                                fieldWithPath("recruitCount").type(JsonFieldType.NUMBER).description("모집 인원"),
                                fieldWithPath("steadyMode").type(JsonFieldType.STRING).description("스테디 진행 방식"),
                                fieldWithPath("openingDate").type(JsonFieldType.STRING).description("스테디 시작 예정일"),
                                fieldWithPath("deadline").type(JsonFieldType.STRING).description("모집 마감일"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("모집글 제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("모집글 내용"),
                                fieldWithPath("questions").type(JsonFieldType.ARRAY).description("스테디 질문 리스트")
                        )
                ));
    }

}