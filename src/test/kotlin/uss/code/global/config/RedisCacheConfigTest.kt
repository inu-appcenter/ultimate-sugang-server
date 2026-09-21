package uss.code.global.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import tools.jackson.databind.ObjectMapper
import uss.code.course.domain.Course
import uss.code.course.domain.CourseArea
import uss.code.course.domain.CourseClassification
import uss.code.course.domain.CourseDepartment
import uss.code.course.domain.CourseGrade
import uss.code.course.domain.CourseType
import uss.code.course.dto.internal.CachedCourseDto
import uss.code.course.dto.internal.CachedCoursesDto
import uss.code.course.fixture.CourseFixture
import uss.code.course.infra.CourseCacheLoader
import uss.code.course.repository.CourseRepository
import uss.code.global.infra.IntegrationTest
import java.nio.charset.StandardCharsets

@IntegrationTest
class RedisCacheConfigTest(
    private val cacheCustomizers: List<RedisCacheManagerBuilderCustomizer>,

    private val courseRepository: CourseRepository,

    private val objectMapper: ObjectMapper,
) {
    private fun serializationPairOf(cacheName: String): SerializationPair<Any> {
        val builder = RedisCacheManager.builder()
        cacheCustomizers.forEach { it.customize(builder) }

        return builder.getCacheConfigurationFor(cacheName).orElseThrow().valueSerializationPair
    }

    private fun createCourse(
        haksuCode: String,
        department: CourseDepartment,
        isEnglishCourse: Boolean,
    ): Course {
        return CourseFixture.createCourse(
            "캐시대상", "Cache Target", haksuCode.substring(0, 7), haksuCode,
            department.courseCollege,
            department,
            CourseClassification.MAJOR_CORE,
            CourseArea.MAJOR_CORE,
            CourseType.LECTURE,
            CourseGrade.SOPHOMORE,
            3, isEnglishCourse, 50, 30,
        )
    }

    @Nested
    inner class 강의_목록_캐시_직렬화_테스트 {
        private lateinit var cachedCourses: CachedCoursesDto

        @BeforeEach
        fun setUp() {
            val courses = courseRepository.saveAll(
                listOf(
                    createCourse("ENG001001", CourseDepartment.COMPUTER_ENGINEERING, true),
                    createCourse("NGT001001", CourseDepartment.ECONOMICS_NIGHT, false),
                )
            )

            cachedCourses = CachedCoursesDto.of(courses.map { CachedCourseDto.from(it) })
        }

        @Test
        fun 전공_과목_캐시에_직렬화한_값을_되읽으면_원래_값과_같다() {
            //given
            val pair = serializationPairOf(CourseCacheLoader.MAJOR_COURSES)

            //when
            val restored = pair.read(pair.write(cachedCourses))

            //then
            assertThat(restored).isEqualTo(cachedCourses)
        }

        @Test
        fun 교양_과목_캐시에_직렬화한_값을_되읽으면_원래_값과_같다() {
            //given
            val pair = serializationPairOf(CourseCacheLoader.GENERAL_EDUCATION_COURSES)

            //when
            val restored = pair.read(pair.write(cachedCourses))

            //then
            assertThat(restored).isEqualTo(cachedCourses)
        }

        @Test
        fun 원어강의_여부와_야간_여부는_is로_시작하는_키로_저장된다() {
            //given
            val pair = serializationPairOf(CourseCacheLoader.MAJOR_COURSES)

            //when
            val json = StandardCharsets.UTF_8.decode(pair.write(cachedCourses)).toString()

            //then
            val course = objectMapper.readTree(json).at("/courses/0")
            assertThat(course.propertyNames())
                .contains("isEnglish", "isNight")
                .doesNotContain("english", "night")
        }
    }
}
