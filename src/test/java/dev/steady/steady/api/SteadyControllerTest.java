package dev.steady.steady.api;

import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.steady.steady.application.SteadyService;
import dev.steady.steady.domain.Promotion;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.fixture.SteadyFixtures;
import dev.steady.steadyForm.domain.SteadyForm;
import dev.steady.steadyForm.fixture.SteadyFormFixtures;
import dev.steady.user.domain.User;
import dev.steady.user.fixture.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;


import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(SteadyController.class)
class SteadyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SteadyService steadyService;

    @Test
    @DisplayName("새로운 스테디를 생성하고 조회 URI를 반환한다.")
    void createSteadyTest() throws Exception {
        // given
        SteadyCreateRequest steadyRequest = SteadyFixtures.createSteadyRequest();
        Promotion promotion = SteadyFixtures.createPromotion();
        User user = UserFixtures.createUser();
        SteadyForm steadyForm = SteadyFormFixtures.createForm(user);
        Steady steady = SteadyFixtures.createSteady(steadyRequest, promotion, steadyForm);

        given(steadyService.create(steadyRequest)).willReturn(steady.getId());

        mockMvc.perform(post("/api/v1/steadies")
                        .header(HttpHeaders.AUTHORIZATION, "token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(steadyRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", String.format("/api/v1/steadies/%d/detail", steady.getId())))
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
                                fieldWithPath("steadyFormId").type(JsonFieldType.NUMBER).description("스테디 신청서 폼")
                        )
                ))
                .andDo(print());
    }

}
