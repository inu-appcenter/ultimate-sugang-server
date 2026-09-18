package uss.code.admin.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.admin.dto.request.SystemSemesterRequest
import uss.code.admin.fixture.SystemSemesterFixture
import uss.code.admin.repository.SystemSemesterRepository
import uss.code.course.domain.CourseTerm.SECOND
import uss.code.course.domain.CourseTerm.SUMMER
import uss.code.global.exception.domain.ExceptionCode.SYSTEM_SEMESTER_NOT_FOUND
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest

@IntegrationTest
class SystemSemesterServiceTest(
    private val systemSemesterService: SystemSemesterService,

    private val systemSemesterRepository: SystemSemesterRepository,
) {
    @Nested
    inner class 표시_학기_조회_테스트 {
        @Test
        fun 설정이_있으면_학년도와_학기를_반환한다() {
            //given
            systemSemesterRepository.save(SystemSemesterFixture.createSystemSemester(TEST_ACADEMIC_YEAR, SECOND))

            //when
            val response = systemSemesterService.getSystemSemester()

            //then
            assertThat(response.academicYear).isEqualTo(TEST_ACADEMIC_YEAR)
            assertThat(response.term).isEqualTo(SECOND)
        }

        @Test
        fun 설정이_없으면_예외가_발생한다() {
            //when & then
            assertThatThrownBy { systemSemesterService.getSystemSemester() }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", SYSTEM_SEMESTER_NOT_FOUND)
        }
    }

    @Nested
    inner class 표시_학기_변경_테스트 {
        @Test
        fun 변경하면_바뀐_값을_반환한다() {
            //given
            systemSemesterRepository.save(SystemSemesterFixture.createSystemSemester(TEST_ACADEMIC_YEAR, SECOND))

            //when
            val response = systemSemesterService.changeSystemSemester(
                SystemSemesterRequest(
                    academicYear = CHANGED_ACADEMIC_YEAR,
                    term = SUMMER,
                )
            )

            //then
            assertThat(response.academicYear).isEqualTo(CHANGED_ACADEMIC_YEAR)
            assertThat(response.term).isEqualTo(SUMMER)
        }

        @Test
        fun 변경한_값이_다음_조회에_반영된다() {
            //given
            systemSemesterRepository.save(SystemSemesterFixture.createSystemSemester(TEST_ACADEMIC_YEAR, SECOND))
            systemSemesterService.changeSystemSemester(
                SystemSemesterRequest(
                    academicYear = CHANGED_ACADEMIC_YEAR,
                    term = SUMMER,
                )
            )

            //when
            val response = systemSemesterService.getSystemSemester()

            //then
            assertThat(response.academicYear).isEqualTo(CHANGED_ACADEMIC_YEAR)
            assertThat(response.term).isEqualTo(SUMMER)
        }

        @Test
        fun 행이_늘어나지_않는다() {
            //given
            systemSemesterRepository.save(SystemSemesterFixture.createSystemSemester(TEST_ACADEMIC_YEAR, SECOND))

            //when
            systemSemesterService.changeSystemSemester(
                SystemSemesterRequest(
                    academicYear = CHANGED_ACADEMIC_YEAR,
                    term = SUMMER,
                )
            )

            //then
            assertThat(systemSemesterRepository.count()).isEqualTo(1L)
        }

        @Test
        fun 설정이_없으면_예외가_발생한다() {
            //given
            val request = SystemSemesterRequest(
                academicYear = CHANGED_ACADEMIC_YEAR,
                term = SUMMER,
            )

            //when & then
            assertThatThrownBy { systemSemesterService.changeSystemSemester(request) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", SYSTEM_SEMESTER_NOT_FOUND)
        }
    }

    companion object {
        private const val TEST_ACADEMIC_YEAR = 2026
        private const val CHANGED_ACADEMIC_YEAR = 2027
    }
}
