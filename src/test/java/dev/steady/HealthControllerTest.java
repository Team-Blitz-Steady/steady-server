package dev.steady;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;

@WebMvcTest(HealthController.class)
@AutoConfigureRestDocs
class HealthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("스웨거 + 레스트 닥스 테스트 확인용")
    void test() throws Exception {
        mockMvc.perform(get("/hello"))
                .andDo(document("health-check",
                        resourceDetails().tag("헬스 체크").description("무중단 배포 헬스 체크용")));

    }

}
