package uss.code.global.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

data class PageResponse<T>(
    @field:Schema(
        description = "현재 페이지 번호. 1부터 시작한다",
        example = "1"
    )
    val page: Int,

    @field:Schema(
        description = "전체 페이지 수",
        example = "8"
    )
    val totalPages: Int,

    @field:Schema(
        description = "다음 페이지 존재 여부",
        example = "true"
    )
    val hasNextPage: Boolean,

    @field:Schema(description = "조회된 리소스 목록")
    val content: List<T>,
) {
    companion object {
        private const val PAGE_NUMBER_OFFSET = 1

        fun <T> of(
            page: Page<*>,
            content: List<T>,
        ): PageResponse<T> {
            return PageResponse(
                page = page.number + PAGE_NUMBER_OFFSET,
                totalPages = page.totalPages,
                hasNextPage = page.hasNext(),
                content = content,
            )
        }
    }
}
