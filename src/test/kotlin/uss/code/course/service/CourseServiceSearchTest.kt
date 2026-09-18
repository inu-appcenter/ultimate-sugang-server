package uss.code.course.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.course.domain.CourseGrade.ALL
import uss.code.course.domain.CourseGrade.FRESHMAN
import uss.code.course.domain.CourseGrade.SENIOR
import uss.code.course.fixture.CourseFixture
import uss.code.course.repository.CourseRepository
import uss.code.global.infra.MySqlIntegrationTest

@MySqlIntegrationTest
class CourseServiceSearchTest(
    private val courseService: CourseService,

    private val courseRepository: CourseRepository,
) {
    @AfterEach
    fun tearDown() {
        courseRepository.deleteAllInBatch()
    }

    @Nested
    inner class 키워드_검색_정렬_테스트 {
        private val keyword = "정렬"

        @BeforeEach
        fun setUp() {
            val courses = listOf(
                CourseFixture.createCourseWithDetails("정렬과 정렬 응용과 정렬", "Advanced Sorting", "SRCH005", "SRCH005001", SENIOR),
                CourseFixture.createCourseWithDetails("자료구조", "Data Structure", "NONE001", "NONE001001", ALL),
                CourseFixture.createCourseWithDetails("정렬 기초", "Sorting Basics", "SRCH001", "SRCH001001", ALL),
                CourseFixture.createCourseWithDetails("정렬 입문", "Sorting Introduction", "SRCH004", "SRCH004001", FRESHMAN),
                CourseFixture.createCourseWithDetails("정렬과 정렬 응용", "Sorting Applications", "SRCH002", "SRCH002001", ALL),
                CourseFixture.createCourseWithDetails("운영체제", "Operating System", "NONE002", "NONE002001", FRESHMAN),
                CourseFixture.createCourseWithDetails("정렬 입문", "Sorting Introduction", "SRCH003", "SRCH003001", FRESHMAN),
            )
            courseRepository.saveAll(courses)
        }

        @Test
        fun 학년이_관련도보다_먼저_정렬된다() {
            //when
            val response = courseService.searchCourses(keyword)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.grade }
                .containsExactly("전학년", "전학년", "1학년", "1학년", "4학년")
        }

        @Test
        fun 같은_학년_안에서는_관련도가_높은_강의가_먼저_온다() {
            //when
            val response = courseService.searchCourses(keyword)

            //then
            val searchedCourses = response.courseResponses
            assertThat(searchedCourses.subList(0, 2))
                .extracting<String> { it.code }
                .containsExactly("SRCH002001", "SRCH001001")
        }

        @Test
        fun 관련도가_같으면_학수번호_순으로_정렬된다() {
            //when
            val response = courseService.searchCourses(keyword)

            //then
            val searchedCourses = response.courseResponses
            assertThat(searchedCourses.subList(2, 4))
                .extracting<String> { it.code }
                .containsExactly("SRCH003001", "SRCH004001")
        }

        @Test
        fun 검색어와_무관한_강의는_결과에_포함되지_않는다() {
            //when
            val response = courseService.searchCourses(keyword)

            //then
            assertThat(response.courseResponses)
                .hasSize(5)
                .extracting<String> { it.code }
                .doesNotContain("NONE001001", "NONE002001")
        }
    }

    @Nested
    inner class 검색어_정제_테스트 {
        @BeforeEach
        fun setUp() {
            courseRepository.saveAll(
                listOf(
                    CourseFixture.createCourseWithDetails("현장교육.실습(Ⅴ-1)", "Internship", "FLD001", "FLD001001", ALL),
                    CourseFixture.createCourseWithDetails("자료구조", "Data Structure", "NONE001", "NONE001001", ALL),
                )
            )
        }

        @Test
        fun 불리언_연산자가_섞인_검색어도_결과가_나온다() {
            //when
            val response = courseService.searchCourses("현장교육.실습(Ⅴ-1)")

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.code }
                .contains("FLD001001")
        }

        @Test
        fun 연산자만_있는_검색어는_빈_목록을_반환한다() {
            //when
            val response = courseService.searchCourses("+*-")

            //then
            assertThat(response.courseResponses).isEmpty()
        }
    }
}
