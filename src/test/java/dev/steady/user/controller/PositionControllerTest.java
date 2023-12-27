package dev.steady.user.controller;

import com.epages.restdocs.apispec.Schema;
import dev.steady.global.config.ControllerTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static dev.steady.user.fixture.UserFixtures.createPositionResponses;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PositionControllerTest extends ControllerTestConfig {

    @Test
    @DisplayName("스택 전체 조회 요청을 통해 모든 스택을 가져올 수 있다.")
    void getStacksTest() throws Exception {
        var positionResponses = createPositionResponses();

        given(positionService.getPositions()).willReturn(positionResponses);

        mockMvc.perform(get("/api/v1/positions"))
                .andDo(document("position-get-positions",
                                resourceDetails().tag("포지션").description("포지션 전체 조회")
                                        .responseSchema(Schema.schema("PositionResponses")),
                                responseFields(
                                        fieldWithPath("positions[].id").type(NUMBER).description("포지션 식별자"),
                                        fieldWithPath("positions[].name").type(STRING).description("포지션 이름")
                                )
                        )
                ).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(positionResponses)));
    }

}