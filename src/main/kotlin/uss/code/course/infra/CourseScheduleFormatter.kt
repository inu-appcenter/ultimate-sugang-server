package uss.code.course.infra

import uss.code.course.domain.CourseDay
import uss.code.course.domain.CourseSchedule

object CourseScheduleFormatter {
    private const val NO_SCHEDULE = ""
    private const val DAY_PERIOD_DELIMITER = " "
    private const val GROUP_DELIMITER = " "
    private const val CLASSROOM_PREFIX = " ("
    private const val CLASSROOM_SUFFIX = ")"

    @JvmStatic
    fun format(schedules: List<CourseSchedule>): String {
        if (schedules.isEmpty()) {
            return NO_SCHEDULE
        }

        val sorted = schedules.sortedWith(compareBy(CourseSchedule::dayOfWeek).thenBy(CourseSchedule::startTime))

        val groups = mutableListOf<String>()
        val periods = mutableListOf<String>()
        var groupHead = sorted.first()

        for (schedule in sorted) {
            if (!isSameGroup(groupHead, schedule)) {
                groups.add(formatGroup(groupHead.dayOfWeek, periods, groupHead.classroom))
                periods.clear()
                groupHead = schedule
            }
            periods.add(schedule.periodName)
        }
        groups.add(formatGroup(groupHead.dayOfWeek, periods, groupHead.classroom))

        return groups.joinToString(GROUP_DELIMITER)
    }

    private fun isSameGroup(
        groupHead: CourseSchedule,
        schedule: CourseSchedule,
    ): Boolean {
        return groupHead.dayOfWeek == schedule.dayOfWeek &&
            groupHead.classroom == schedule.classroom
    }

    private fun formatGroup(
        day: CourseDay,
        periods: List<String>,
        classroom: String,
    ): String {
        val joinedPeriods = periods.joinToString(DAY_PERIOD_DELIMITER)

        return "${day.displayName}$DAY_PERIOD_DELIMITER$joinedPeriods$CLASSROOM_PREFIX$classroom$CLASSROOM_SUFFIX"
    }
}
