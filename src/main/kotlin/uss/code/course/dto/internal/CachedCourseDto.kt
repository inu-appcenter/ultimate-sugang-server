package uss.code.course.dto.internal

import uss.code.course.domain.Course
import uss.code.course.domain.CourseType
import uss.code.course.infra.CourseScheduleFormatter

@JvmRecord
data class CachedCourseDto(
    val id: Long,
    val code: String,
    val courseCode: String,
    val name: String,
    val nameEn: String,
    val englishCourseName: String,
    val professor: String,
    val credits: Int,
    val courseType: String,
    val courseArea: String,
    val areaCode: String,
    val department: String,
    val grade: String,
    val schedule: String,
    val tags: List<String>,
    val isEnglish: Boolean,
    val isNight: Boolean,
) {
    companion object {
        private const val NO_PROFESSOR = ""
        private const val NO_ENGLISH_COURSE_NAME = ""
        private const val NO_COURSE_AREA = ""
        private const val LONG_LESSON_TAG = "75분수업"

        @JvmStatic
        fun from(course: Course): CachedCourseDto {
            return CachedCourseDto(
                id = course.id,
                code = course.haksuCode,
                courseCode = course.courseCode,
                name = course.titleKr,
                nameEn = course.titleEn,
                englishCourseName = if (course.isEnglishCourse) course.englishName else NO_ENGLISH_COURSE_NAME,
                professor = NO_PROFESSOR,
                credits = course.credits,
                courseType = course.classificationName,
                courseArea = if (course.area.isGeneralEducationArea()) course.areaName else NO_COURSE_AREA,
                areaCode = course.areaCode,
                department = course.department.displayName,
                grade = course.gradeName,
                schedule = CourseScheduleFormatter.format(course.schedules),
                tags = buildTags(course),
                isEnglish = course.isEnglishCourse,
                isNight = course.department.isNight(),
            )
        }

        private fun buildTags(course: Course): List<String> {
            val tags = mutableListOf<String>()

            if (course.is75MinLesson()) {
                tags.add(LONG_LESSON_TAG)
            }
            if (CourseType.isTagType(course.typeCode)) {
                tags.add(course.typeName)
            }

            return tags.toList()
        }
    }
}
