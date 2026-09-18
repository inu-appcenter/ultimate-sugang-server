package uss.code.registration.fixture

import org.springframework.test.util.ReflectionTestUtils
import uss.code.course.domain.Course
import uss.code.member.domain.Member
import uss.code.registration.domain.Registration
import java.time.LocalDateTime

object RegistrationFixture {
    fun createRegistration(
        member: Member,
        course: Course,
    ): Registration {
        return createRegistration(member, course, LocalDateTime.now())
    }

    fun createRegistration(
        member: Member,
        course: Course,
        createdAt: LocalDateTime,
    ): Registration {
        val registration = Registration.create(
            member = member,
            course = course,
        )
        ReflectionTestUtils.setField(registration, "createdAt", createdAt)

        return registration
    }
}
