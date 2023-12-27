package dev.steady.user.controller;

import com.epages.restdocs.apispec.Schema;
import dev.steady.global.config.ControllerTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static dev.steady.user.fixture.UserFixtures.createStackResponses;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class StackControllerTest extends ControllerTestConfig {

    @Test
    @DisplayName("스택 전체 조회 요청을 통해 모든 스택을 가져올 수 있다.")
    void getStacksTest() throws Exception {
        var stackResponses = createStackResponses();

        given(stackService.getStacks()).willReturn(stackResponses);

        mockMvc.perform(get("/api/v1/stacks"))
                .andDo(document("stacks-get-stacks",
                                resourceDetails().tag("스택").description("스택 전체 조회")
                                        .responseSchema(Schema.schema("StackResponses")),
                                responseFields(
                                        fieldWithPath("stacks[].id").type(NUMBER).description("스택 식별자"),
                                        fieldWithPath("stacks[].name").type(STRING).description("스택 이름"),
                                        fieldWithPath("stacks[].imageUrl").type(STRING).description("스택 이미지 Url")
                                )
                        )
                ).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(stackResponses)));
    }

}
