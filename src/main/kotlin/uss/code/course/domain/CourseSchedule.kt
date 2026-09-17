package uss.code.course.domain

import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "course_schedules")
class CourseSchedule private constructor(
    dayOfWeek: CourseDay,
    periodCode: String,
    periodName: String,
    classroom: String,
    startTime: LocalTime,
    endTime: LocalTime,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "course_id")
    lateinit var course: Course
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "day_of_week")
    var dayOfWeek: CourseDay = dayOfWeek
        protected set

    @Column(nullable = false, name = "period_code")
    var periodCode: String = periodCode
        protected set

    @Column(nullable = false, name = "period_name")
    var periodName: String = periodName
        protected set

    @Column(nullable = false, name = "classroom")
    var classroom: String = classroom
        protected set

    @Column(nullable = false, name = "start_time")
    var startTime: LocalTime = startTime
        protected set

    @Column(nullable = false, name = "end_time")
    var endTime: LocalTime = endTime
        protected set

    fun addCourse(course: Course) {
        this.course = course
    }

    fun is75MinLesson(): Boolean {
        return periodCode.startsWith(LONG_LESSON_CODE_PREFIX)
    }

    companion object {
        private const val LONG_LESSON_CODE_PREFIX = "B"

        @JvmStatic
        fun create(
            dayOfWeek: CourseDay,
            periodCode: String,
            periodName: String,
            classroom: String,
            startTime: LocalTime,
            endTime: LocalTime,
        ): CourseSchedule {
            return CourseSchedule(
                dayOfWeek = dayOfWeek,
                periodCode = periodCode,
                periodName = periodName,
                classroom = classroom,
                startTime = startTime,
                endTime = endTime,
            )
        }
    }
}
