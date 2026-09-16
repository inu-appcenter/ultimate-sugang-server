package uss.code.course.dto.response

import uss.code.course.domain.Course
import uss.code.course.dto.internal.CachedCourseDto
import uss.code.course.dto.internal.CourseCapacityDto

@JvmRecord
data class CourseResponse(
    val id: String,

    val code: String,

    val courseCode: String,

    val name: String,

    val nameEn: String,

    val englishCourseName: String,

    val professor: String,

    val credits: Int,

    val capacity: Int,

    val enrolled: Int,

    val cartCount: Int,

    val courseType: String,

    val courseArea: String,

    val department: String,

    val grade: String,

    val schedule: String,

    val tags: List<String>,

    val isEnglish: Boolean,

    val isNight: Boolean,

    val isClosed: Boolean,
) {
    companion object {
        @JvmStatic
        fun of(
            course: CachedCourseDto,
            capacity: CourseCapacityDto,
        ): CourseResponse = of(
            course = course,
            capacity = capacity.maxCapacity,
            enrolled = capacity.currentEnrollment,
            cartCount = capacity.cartCount,
            isClosed = !capacity.isRegisterable(),
        )

        @JvmStatic
        fun from(course: Course): CourseResponse = of(
            course = CachedCourseDto.from(course),
            capacity = course.maxCapacity,
            enrolled = course.currentEnrollment,
            cartCount = course.cartCount,
            isClosed = !(course.isActive() && course.isRegisterable()),
        )

        private fun of(
            course: CachedCourseDto,
            capacity: Int,
            enrolled: Int,
            cartCount: Int,
            isClosed: Boolean,
        ): CourseResponse = CourseResponse(
            id = course.id.toString(),
            code = course.code,
            courseCode = course.courseCode,
            name = course.name,
            nameEn = course.nameEn,
            englishCourseName = course.englishCourseName,
            professor = course.professor,
            credits = course.credits,
            capacity = capacity,
            enrolled = enrolled,
            cartCount = cartCount,
            courseType = course.courseType,
            courseArea = course.courseArea,
            department = course.department,
            grade = course.grade,
            schedule = course.schedule,
            tags = course.tags,
            isEnglish = course.isEnglish,
            isNight = course.isNight,
            isClosed = isClosed,
        )
    }
}
