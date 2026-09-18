package uss.code.course.fixture

import uss.code.course.domain.Course
import uss.code.course.domain.CourseDay
import uss.code.course.domain.CourseSchedule
import java.time.LocalTime

object CourseScheduleFixture {
    private const val DEFAULT_PERIOD_CODE = "B01"
    private const val DEFAULT_PERIOD_NAME = "1-2A"
    private const val DEFAULT_CLASSROOM = "07-401"

    fun createCourseSchedule(course: Course): CourseSchedule {
        return createCourseSchedule(
            course,
            CourseDay.MONDAY,
            DEFAULT_PERIOD_NAME,
            DEFAULT_CLASSROOM,
            LocalTime.of(9, 0),
            LocalTime.of(10, 30),
        )
    }

    fun createCourseSchedule(
        course: Course,
        dayOfWeek: CourseDay,
        startTime: LocalTime,
        endTime: LocalTime,
    ): CourseSchedule {
        return createCourseSchedule(
            course,
            dayOfWeek,
            DEFAULT_PERIOD_NAME,
            DEFAULT_CLASSROOM,
            startTime,
            endTime,
        )
    }

    fun createCourseScheduleWithPeriodCode(
        course: Course,
        periodCode: String,
    ): CourseSchedule {
        return create(
            course,
            CourseDay.MONDAY,
            periodCode,
            DEFAULT_PERIOD_NAME,
            DEFAULT_CLASSROOM,
            LocalTime.of(9, 0),
            LocalTime.of(10, 30),
        )
    }

    fun createCourseSchedule(
        course: Course,
        dayOfWeek: CourseDay,
        periodName: String,
        classroom: String,
        startTime: LocalTime,
        endTime: LocalTime,
    ): CourseSchedule {
        return create(
            course,
            dayOfWeek,
            DEFAULT_PERIOD_CODE,
            periodName,
            classroom,
            startTime,
            endTime,
        )
    }

    private fun create(
        course: Course,
        dayOfWeek: CourseDay,
        periodCode: String,
        periodName: String,
        classroom: String,
        startTime: LocalTime,
        endTime: LocalTime,
    ): CourseSchedule {
        val courseSchedule = CourseSchedule.create(
            dayOfWeek = dayOfWeek,
            periodCode = periodCode,
            periodName = periodName,
            classroom = classroom,
            startTime = startTime,
            endTime = endTime,
        )
        courseSchedule.addCourse(course)

        return courseSchedule
    }
}
