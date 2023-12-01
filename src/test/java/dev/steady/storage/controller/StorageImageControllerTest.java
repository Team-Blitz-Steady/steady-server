package dev.steady.storage.controller;

import com.epages.restdocs.apispec.Schema;
import dev.steady.global.auth.Authentication;
import dev.steady.global.config.ControllerTestConfig;
import dev.steady.storage.ImageUploadPurpose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.storage.fixture.StorageFixture.createPutObjectUrlResponse;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StorageImageControllerTest extends ControllerTestConfig {

    @ParameterizedTest
    @EnumSource(ImageUploadPurpose.class)
    @DisplayName("이미지 업로드용 Presigned Url을 반환할 수 있다.")
    void getImageUploadUrl(ImageUploadPurpose imageUploadPurpose) throws Exception {
        // given
        var userId = 1L;
        var userInfo = createUserInfo(userId);
        var authentication = new Authentication(userId);

        given(jwtResolver.getAuthentication(TOKEN)).willReturn(authentication);
        var response = createPutObjectUrlResponse();
        var fileName = "image.png";
        var purpose = imageUploadPurpose.getPurpose();
        var keyPattern = imageUploadPurpose.getKeyPattern();
        given(storageService.generatePutObjectUrl(fileName, keyPattern)).willReturn(response);

        // when, then
        mockMvc.perform(get("/api/v1/storage/image/{purpose}", purpose)
                        .queryParam("fileName", fileName)
                        .header(AUTHORIZATION, TOKEN))
                .andDo(document("storage-v1-get-PutObjectUrlResponse",
                        resourceDetails().tag("스토리지").description("이미지 업로드 URL 불러오기")
                                .responseSchema(Schema.schema("PutObjectUrlResponse")),
                        queryParameters(
                                parameterWithName("fileName").description("확장자를 포함한 이미지 파일 이름")
                        ),
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("토큰")
                        ),
                        responseFields(
                                fieldWithPath("presignedUrl").type(STRING).description("사용자 프로필 이미지 업로드 URL"),
                                fieldWithPath("objectUrl").type(STRING).description("업로드된 이미지 URL")
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

}
