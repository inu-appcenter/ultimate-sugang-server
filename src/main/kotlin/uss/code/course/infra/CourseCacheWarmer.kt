package uss.code.course.infra

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uss.code.course.domain.CourseClassification
import uss.code.course.domain.CourseDepartment
import uss.code.member.domain.MemberDepartment

@Component
@ConditionalOnProperty(name = ["spring.cache.type"], havingValue = "redis")
class CourseCacheWarmer(
    private val courseCacheLoader: CourseCacheLoader,
) {
    @EventListener(ApplicationReadyEvent::class)
    @Scheduled(cron = "\${cache.major-courses.refresh-cron}", zone = "\${cache.major-courses.refresh-zone}")
    fun warmMajorCourses() {
        MemberDepartment.entries
            .filter { CourseDepartment.ownedBy(it).isNotEmpty() }
            .forEach(courseCacheLoader::refreshMajorCourses)
    }

    @EventListener(ApplicationReadyEvent::class)
    @Scheduled(
        cron = "\${cache.general-education-courses.refresh-cron}",
        zone = "\${cache.general-education-courses.refresh-zone}",
    )
    fun warmGeneralEducationCourses() {
        CourseClassification.entries
            .filter(CourseClassification::isLiberalArtsScreen)
            .forEach(courseCacheLoader::refreshGeneralEducationCourses)
    }
}
