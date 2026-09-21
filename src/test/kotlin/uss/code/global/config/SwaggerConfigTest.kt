package uss.code.global.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import tools.jackson.databind.ObjectMapper
import uss.code.global.infra.IntegrationTest
import java.nio.charset.StandardCharsets

@IntegrationTest
class SwaggerConfigTest(
    private val webApplicationContext: WebApplicationContext,
    private val objectMapper: ObjectMapper,
) {
    @Nested
    inner class 강의_응답_스키마_테스트 {
        @Test
        fun is로_시작하는_Boolean_프로퍼티는_이름_그대로_문서에_나간다() {
            //given
            val mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

            //when
            val apiDocs = mockMvc.perform(get(API_DOCS_PATH))
                .andReturn()
                .response
                .getContentAsString(StandardCharsets.UTF_8)

            //then
            val properties = objectMapper.readTree(apiDocs).at("/components/schemas/CourseResponse/properties")
            assertThat(properties.propertyNames())
                .contains("isEnglish", "isNight", "isClosed")
                .doesNotContain("english", "night", "closed")
        }
    }

    companion object {
        private const val API_DOCS_PATH = "/v3/api-docs"
    }
}
