package dev.steady.review.controller;

import com.epages.restdocs.apispec.Schema;
import dev.steady.global.auth.Authentication;
import dev.steady.global.config.ControllerTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.review.fixture.ReviewFixture.createReviewCreateRequest;
import static dev.steady.review.fixture.ReviewFixture.createReviewUpdateRequest;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest extends ControllerTestConfig {

    @Test
    @DisplayName("리뷰이 ID와 스테디 ID를 통해 리뷰를 생성할 수 있다.")
    void createReviewTest() throws Exception {
        // given
        var userId = 1L;
        var userInfo = createUserInfo(userId);
        var steadyId = 1L;
        var request = createReviewCreateRequest();
        var auth = new Authentication(userId);
        given(jwtResolver.getAuthentication(TOKEN)).willReturn(auth);
        given(reviewService.createReview(steadyId, request, userInfo)).willReturn(1L);

        // when, then
        mockMvc.perform(post("/api/v1/steadies/{steadyId}/review", steadyId)
                        .contentType(APPLICATION_JSON)
                        .header(AUTHORIZATION, TOKEN)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(document("review-v1-create",
                        resourceDetails().tag("리뷰").description("리뷰 생성")
                                .requestSchema(Schema.schema("ReviewCreateRequest")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        requestFields(
                                fieldWithPath("revieweeId").type(NUMBER).description("리뷰이 사용자 식별자"),
                                fieldWithPath("cardsId").type(ARRAY).description("카드 식별자 리스트"),
                                fieldWithPath("comment").type(STRING).description("리뷰 코멘트")
                        )
                ))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl(String.format("/api/v1/reviews/%d", steadyId, 1L)));
    }

    @Test
    @DisplayName("리뷰이는 자신이 받은 리뷰의 공개 여부를 설정할 수 있다.")
    void updateReviewIsPublicTest() throws Exception {
        // given
        var userId = 1L;
        var userInfo = createUserInfo(userId);
        var auth = new Authentication(userId);
        given(jwtResolver.getAuthentication(TOKEN)).willReturn(auth);

        var request = createReviewUpdateRequest(false);
        var reviewId = 1L;
        willDoNothing().given(reviewService).updateReviewIsPublic(reviewId, request, userInfo);

        // when, then
        mockMvc.perform(patch("/api/v1/reviews/{reviewId}", reviewId)
                        .contentType(APPLICATION_JSON)
                        .header(AUTHORIZATION, TOKEN)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(document("review-v1-update",
                        resourceDetails().tag("리뷰").description("리뷰 공개 여부 수정")
                                .requestSchema(Schema.schema("ReviewUpdateRequest")),
                        requestFields(
                                fieldWithPath("isPublic").type(BOOLEAN).description("수정할 리뷰 공개 상태")
                        )
                ))
                .andExpect(status().isOk());
    }

}