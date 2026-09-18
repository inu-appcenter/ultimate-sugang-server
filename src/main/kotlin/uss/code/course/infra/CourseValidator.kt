package uss.code.course.infra

import uss.code.course.domain.Course
import uss.code.course.domain.CourseType.K_MOOC
import uss.code.course.domain.CourseType.OCU
import uss.code.member.domain.Member
import uss.code.registration.domain.Registration
import java.time.LocalTime

object CourseValidator {
    private const val MAX_OCU_COURSE_COUNT = 2
    private const val MAX_K_MOOC_COURSE_COUNT = 1

    fun validateCourseScheduleNotConflict(
        existingCourses: List<Course>,
        newCourse: Course,
    ): Boolean {
        if (existingCourses.isEmpty()) {
            return true
        }

        val newCourseSchedules = newCourse.schedules

        if (newCourseSchedules.isEmpty()) {
            return true
        }

        for (existingCourse in existingCourses) {
            val existingCourseSchedules = existingCourse.schedules

            for (newSchedule in newCourseSchedules) {
                val newCourseDay = newSchedule.dayOfWeek
                val newStartTime = newSchedule.startTime
                val newEndTime = newSchedule.endTime

                for (existingSchedule in existingCourseSchedules) {
                    val existingCourseDay = existingSchedule.dayOfWeek
                    val existingStartTime = existingSchedule.startTime
                    val existingEndTime = existingSchedule.endTime

                    if (newCourseDay == existingCourseDay &&
                        isTimeOverlap(newStartTime, newEndTime, existingStartTime, existingEndTime)
                    ) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun validateCourseTypeLimit(
        existingCourses: List<Course>,
        newCourse: Course,
    ): Boolean {
        val typeCode = newCourse.typeCode

        val sameTypeCount = existingCourses.count { it.typeCode == typeCode }

        if (OCU.code == typeCode) {
            return sameTypeCount < MAX_OCU_COURSE_COUNT
        }

        if (K_MOOC.code == typeCode) {
            return sameTypeCount < MAX_K_MOOC_COURSE_COUNT
        }

        return true
    }

    fun validateCreditLimit(
        registrations: List<Registration>,
        member: Member,
        newCourse: Course,
    ): Boolean {
        val maxCredit = member.maxCredit

        val currentCredit = registrations.sumOf { it.course.credits }

        return currentCredit + newCourse.credits <= maxCredit
    }

    private fun isTimeOverlap(
        newStart: LocalTime,
        newEnd: LocalTime,
        existingStart: LocalTime,
        existingEnd: LocalTime,
    ): Boolean {
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)
    }
}
