package uss.code.course.dto.internal

data class CachedCoursesDto(
    val courses: List<CachedCourseDto>,
) {
    companion object {
        fun of(courses: List<CachedCourseDto>): CachedCoursesDto {
            return CachedCoursesDto(courses = courses)
        }
    }
}
