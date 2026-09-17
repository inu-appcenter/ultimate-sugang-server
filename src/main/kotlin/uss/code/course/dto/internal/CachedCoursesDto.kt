package uss.code.course.dto.internal

@JvmRecord
data class CachedCoursesDto(
    val courses: List<CachedCourseDto>,
) {
    companion object {
        @JvmStatic
        fun of(courses: List<CachedCourseDto>): CachedCoursesDto {
            return CachedCoursesDto(courses = courses)
        }
    }
}
