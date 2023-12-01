package dev.steady.user.controller;

import com.epages.restdocs.apispec.Schema;
import dev.steady.auth.domain.Platform;
import dev.steady.global.auth.Authentication;
import dev.steady.global.config.ControllerTestConfig;
import dev.steady.user.dto.response.UserNicknameExistResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static dev.steady.auth.domain.Platform.KAKAO;
import static dev.steady.auth.fixture.OAuthFixture.createAuthCodeRequestUrl;
import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.user.fixture.UserFixtures.createUserCreateRequest;
import static dev.steady.user.fixture.UserFixtures.createUserMyDetailResponse;
import static dev.steady.user.fixture.UserFixtures.createUserOtherDetailResponse;
import static dev.steady.user.fixture.UserFixtures.createUserUpdateRequest;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.responseBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends ControllerTestConfig {

    @Test
    @DisplayName("Account가 존재하면 User를 등록하고 OAuth 인가코드 요청 URL을 반환할 수 있다.")
    void createUser() throws Exception {
        // given
        var request = createUserCreateRequest();
        var authCodeRequestUrl = createAuthCodeRequestUrl();
        given(userService.createUser(request)).willReturn(1L);
        given(accountService.registerUser(request.accountId(), 1L)).willReturn(KAKAO);

        // when
        when(oAuthService.getAuthCodeRequestUrlProvider(KAKAO)).thenReturn(authCodeRequestUrl);

        // then
        mockMvc.perform(post("/api/v1/user/profile")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andDo(document("user-v1-create",
                        resourceDetails().tag("사용자").description("유저 프로필 생성")
                                .requestSchema(Schema.schema("UserCreateResponse")),
                        requestFields(
                                fieldWithPath("accountId").type(NUMBER).description("계정 id"),
                                fieldWithPath("nickname").type(STRING).description("사용자 닉네임"),
                                fieldWithPath("positionId").type(NUMBER).description("사용자 포지션"),
                                fieldWithPath("stacksId").type(ARRAY).description("사용자 기술 스택")
                        )
                ))
                .andExpect(header().string("Location", authCodeRequestUrl.toString()));
    }

    @Test
    @DisplayName("이미 존재하는 nickname에 대해서 true를 반환할 수 있다.")
    void existsByNickname() throws Exception {
        // given
        var nickname = "닉네임";
        var response = new UserNicknameExistResponse(true);
        given(userService.existsByNickname(nickname)).willReturn(response);

        // when, then
        mockMvc.perform(get("/api/v1/user/profile/exist")
                        .queryParam("nickname", nickname))
                .andDo(document("user-v1-check-nickname", resourceDetails().tag("사용자")
                                .description("유저 닉네임 중복 검사")
                                .responseSchema(Schema.schema("UserCheckNicknameResponse")),
                        queryParameters(
                                parameterWithName("nickname").description("닉네임")
                        ),
                        responseBody()
                ))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(response)));
    }

    @ParameterizedTest
    @EnumSource(Platform.class)
    @DisplayName("내 프로필을 조회할 수 있다.")
    void getMyUserDetail(Platform platform) throws Exception {
        // given
        var userId = 1L;
        var userInfo = createUserInfo(userId);
        var auth = new Authentication(userId);
        var response = createUserMyDetailResponse(platform);
        given(jwtResolver.getAuthentication(TOKEN)).willReturn(auth);
        given(userService.getMyUserDetail(userInfo)).willReturn(response);

        // when, then
        mockMvc.perform(get("/api/v1/user/profile")
                        .header(AUTHORIZATION, TOKEN)
                )
                .andDo(document("user-v1-get-myProfile",
                        resourceDetails().tag("사용자").description("내 프로필 조회")
                                .responseSchema(Schema.schema("UserMyDetailResponse")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        responseFields(
                                fieldWithPath("platform").type(STRING).description("소셜 계정 플랫폼"),
                                fieldWithPath("userId").type(NUMBER).description("사용자 식별자"),
                                fieldWithPath("profileImage").type(STRING).description("사용자 프로필 이미지 URL"),
                                fieldWithPath("nickname").type(STRING).description("사용자 닉네임"),
                                fieldWithPath("bio").type(STRING).description("사용자 한 줄 소개"),
                                fieldWithPath("position.id").type(NUMBER).description("관심 포지션 식별자"),
                                fieldWithPath("position.name").type(STRING).description("관심 포지션 이름"),
                                fieldWithPath("stacks[].id").type(NUMBER).description("관심 스택 식별자"),
                                fieldWithPath("stacks[].name").type(STRING).description("관심 스택 이름"),
                                fieldWithPath("stacks[].imageUrl").type(STRING).description("관심 스택 이미지 URL")
                        )
                )).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("타인의 프로필을 조회할 수 있다.")
    void getOtherUserDetail() throws Exception {
        // given
        var userId = 2L;
        var response = createUserOtherDetailResponse();
        given(userService.getOtherUserDetail(userId)).willReturn(response);

        // when, then
        mockMvc.perform(get("/api/v1/user/{userId}/profile", userId)
                        .contentType(APPLICATION_JSON)
                )
                .andDo(document("user-v1-get-otherProfile",
                        resourceDetails().tag("사용자").description("타인의 프로필 조회")
                                .responseSchema(Schema.schema("UserOtherDetailResponse")),
                        responseFields(
                                fieldWithPath("user.userId").type(NUMBER).description("사용자 식별자"),
                                fieldWithPath("user.profileImage").type(STRING).description("사용자 프로필 이미지 URL"),
                                fieldWithPath("user.nickname").type(STRING).description("사용자 닉네임"),
                                fieldWithPath("user.bio").type(STRING).description("사용자 한 줄 소개"),
                                fieldWithPath("user.position.id").type(NUMBER).description("관심 포지션 식별자"),
                                fieldWithPath("user.position.name").type(STRING).description("관심 포지션 이름"),
                                fieldWithPath("user.stacks[].id").type(NUMBER).description("관심 스택 식별자"),
                                fieldWithPath("user.stacks[].name").type(STRING).description("관심 스택 이름"),
                                fieldWithPath("user.stacks[].imageUrl").type(STRING).description("관심 스택 이미지 URL"),
                                fieldWithPath("userCards[].cardId").type(NUMBER).description("카드 식별자"),
                                fieldWithPath("userCards[].imageUrl").type(STRING).description("카드 이미지 URL"),
                                fieldWithPath("userCards[].count").type(NUMBER).description("사용자가 받은 카드 개수"),
                                fieldWithPath("reviews").type(ARRAY).description("사용자가 받은 리뷰 코멘트"),
                                fieldWithPath("isDeleted").type(BOOLEAN).description("탈퇴 유저 여부")
                        )
                )).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("내 프로필 정보를 수정할 수 있다.")
    void updateUserDetail() throws Exception {
        // given
        var userId = 1L;
        var userInfo = createUserInfo(userId);
        var auth = new Authentication(userId);
        given(jwtResolver.getAuthentication(TOKEN)).willReturn(auth);

        var request = createUserUpdateRequest();
        willDoNothing().given(userService).updateUser(request, userInfo);

        // when, then
        mockMvc.perform(patch("/api/v1/user/profile")
                        .contentType(APPLICATION_JSON)
                        .header(AUTHORIZATION, TOKEN)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(document("user-v1-update",
                        resourceDetails().tag("사용자").description("유저 프로필 수정")
                                .requestSchema(Schema.schema("UserUpdateRequest")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        requestFields(
                                fieldWithPath("profileImage").type(STRING).description("사용자 프로필 이미지 URL"),
                                fieldWithPath("nickname").type(STRING).description("사용자 닉네임"),
                                fieldWithPath("bio").type(STRING).description("사용자 한 줄 소개"),
                                fieldWithPath("positionId").type(NUMBER).description("사용자 포지션"),
                                fieldWithPath("stacksId").type(ARRAY).description("사용자 기술 스택")
                        )
                ))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원탈퇴를 진행할 수 있다.")
    void withdrawTest() throws Exception {
        // given
        var userId = 1L;
        var userInfo = createUserInfo(userId);
        var authentication = new Authentication(userId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(userService).withdrawUser(userInfo);

        // when, then
        mockMvc.perform(delete("/api/v1/user")
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("user-v1-withdraw", resourceDetails().tag("사용자")
                                .description("유저 탈퇴"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )
                )).andExpect(status().isNoContent());
    }

}
