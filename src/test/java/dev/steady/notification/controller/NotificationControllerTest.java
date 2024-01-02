package dev.steady.notification.controller;

import com.epages.restdocs.apispec.Schema;
import dev.steady.global.auth.Authentication;
import dev.steady.global.config.ControllerTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.notification.fixture.NotificationFixture.createNotificationsResponse;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest extends ControllerTestConfig {

    @Test
    @DisplayName("유저 정보를 통해 전체 알림을 조회할 수 있다.")
    void getNotificationsTest() throws Exception {
        // given
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);
        var notificationsResponse = createNotificationsResponse();

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        given(notificationService.getNotifications(userInfo)).willReturn(notificationsResponse);

        // when & then
        mockMvc.perform(get("/api/v1/notifications")
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-get-notifications",
                        resourceDetails().tag("알림").description("전체 알림 조회")
                                .requestSchema(Schema.schema("NotificationsResponse")),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        responseFields(
                                fieldWithPath("notifications[].id").type(NUMBER).description("알림 식별자"),
                                fieldWithPath("notifications[].name").type(STRING).description("알림 대상 스테디 이름"),
                                fieldWithPath("notifications[].type").type(STRING).description("알림 타입"),
                                fieldWithPath("notifications[].result").type(STRING).description("알림 결과"),
                                fieldWithPath("notifications[].isRead").type(BOOLEAN).description("읽음 여부"),
                                fieldWithPath("freshCount").type(NUMBER).description("읽지 않은 알림 개수")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(notificationsResponse)));
    }

    @Test
    @DisplayName("유저 정보와 알림 식별자를 통해 읽음으로 변경할 수 있다.")
    void readNotificationTest() throws Exception {
        // given
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);
        var notificationId = 1L;

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(notificationService).readNotification(notificationId, userInfo);

        // when & then
        mockMvc.perform(patch("/api/v1/notifications/{notificationId}", notificationId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-read-notification",
                        resourceDetails().tag("알림").description("알림 읽기"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )
                ))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("유저 정보를 통해 모든 알림을 읽음으로 변경할 수 있다.")
    void readNotificationsTest() throws Exception {
        // given
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(notificationService).readNotifications(userInfo);

        // when & then
        mockMvc.perform(patch("/api/v1/notifications/readAll")
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-readAll",
                        resourceDetails().tag("알림").description("모든 알림 읽기"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )
                ))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("유저 정보와 알림 식별자를 통해 알림을 삭제할 수 있다.")
    void deleteNotificationTest() throws Exception {
        // given
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);
        var notificationId = 1L;

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(notificationService).deleteNotification(notificationId, userInfo);

        // when & then
        mockMvc.perform(delete("/api/v1/notifications/{notificationId}", notificationId)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-delete-notification",
                        resourceDetails().tag("알림").description("알림 삭제"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )
                ))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("유저 정보를 통해 모든 알림을 삭제할 수 있다.")
    void deleteAllTest() throws Exception {
        // given
        var userId = 1L;
        var authentication = new Authentication(userId);
        var userInfo = createUserInfo(userId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        willDoNothing().given(notificationService).deleteAll(userInfo);

        // when & then
        mockMvc.perform(delete("/api/v1/notifications/deleteAll")
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("steady-deleteAll",
                        resourceDetails().tag("알림").description("모든 알림 삭제"),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        )
                ))
                .andExpect(status().isNoContent());
    }

}