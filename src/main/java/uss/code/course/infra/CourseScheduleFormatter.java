package uss.code.course.infra;

import lombok.experimental.UtilityClass;
import uss.code.course.domain.CourseDay;
import uss.code.course.domain.CourseSchedule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@UtilityClass
public class CourseScheduleFormatter {

    private static final String NO_SCHEDULE = "";
    private static final String DAY_PERIOD_DELIMITER = " ";
    private static final String GROUP_DELIMITER = " ";
    private static final String CLASSROOM_PREFIX = " (";
    private static final String CLASSROOM_SUFFIX = ")";

    public static String format(final List<CourseSchedule> schedules) {
        if (schedules.isEmpty()) {
            return NO_SCHEDULE;
        }

        final List<CourseSchedule> sorted = schedules.stream()
                .sorted(Comparator.comparing(CourseSchedule::getDayOfWeek)
                        .thenComparing(CourseSchedule::getStartTime))
                .toList();

        final List<String> groups = new ArrayList<>();
        final List<String> periods = new ArrayList<>();
        CourseSchedule groupHead = sorted.get(0);

        for (final CourseSchedule schedule : sorted) {
            if (!isSameGroup(groupHead, schedule)) {
                groups.add(formatGroup(groupHead.getDayOfWeek(), periods, groupHead.getClassroom()));
                periods.clear();
                groupHead = schedule;
            }
            periods.add(schedule.getPeriodName());
        }
        groups.add(formatGroup(groupHead.getDayOfWeek(), periods, groupHead.getClassroom()));

        return String.join(GROUP_DELIMITER, groups);
    }

    private static boolean isSameGroup(
            final CourseSchedule groupHead,
            final CourseSchedule schedule
    ) {
        return groupHead.getDayOfWeek() == schedule.getDayOfWeek()
                && groupHead.getClassroom().equals(schedule.getClassroom());
    }

    private static String formatGroup(
            final CourseDay day,
            final List<String> periods,
            final String classroom
    ) {
        return day.getName()
                + DAY_PERIOD_DELIMITER
                + String.join(DAY_PERIOD_DELIMITER, periods)
                + CLASSROOM_PREFIX + classroom + CLASSROOM_SUFFIX;
    }
}
