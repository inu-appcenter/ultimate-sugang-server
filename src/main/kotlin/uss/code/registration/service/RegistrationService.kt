package uss.code.registration.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uss.code.course.domain.Course
import uss.code.course.infra.CourseValidator
import uss.code.course.repository.CourseRepository
import uss.code.global.exception.domain.ExceptionCode.*
import uss.code.global.exception.domain.RestApiException
import uss.code.member.domain.Member
import uss.code.member.repository.MemberRepository
import uss.code.registration.domain.Registration
import uss.code.registration.dto.response.RegistrationCourseResponse
import uss.code.registration.dto.response.RegistrationCoursesResponse
import uss.code.registration.dto.response.RegistrationResponse
import uss.code.registration.infra.RegistrationTypeResolver
import uss.code.registration.repository.RegistrationRepository

@Service
class RegistrationService(
    private val registrationRepository: RegistrationRepository,
    private val memberRepository: MemberRepository,
    private val courseRepository: CourseRepository,
) {
    @Transactional(readOnly = true)
    fun getRegistrationCourse(memberId: Long): RegistrationCoursesResponse {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw RestApiException(MEMBER_NOT_FOUND)

        val registrations = registrationRepository.findByMemberId(memberId)

        val registrationCourseResponses = registrations.map { registration ->
            RegistrationCourseResponse.of(
                registration,
                member.studentId,
                RegistrationTypeResolver.resolve(member, registration.course),
            )
        }

        return RegistrationCoursesResponse.of(registrationCourseResponses)
    }

    @Transactional
    fun registerCourse(
        memberId: Long,
        courseId: Long,
    ): RegistrationResponse {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw RestApiException(MEMBER_NOT_FOUND)

        val registrations = registrationRepository.findByMemberId(memberId)

        val course = courseRepository.findByIdOrNull(courseId)
            ?: throw RestApiException(COURSE_NOT_FOUND)

        validateCourseActive(course)
        validateCourseScheduleConflict(registrations, course)
        validateDuplicateSubject(registrations, course)
        validateDuplicateCourse(registrations, courseId)
        validateCreditLimit(registrations, member, course)
        validateCourseTypeLimit(registrations, course)

        increaseEnrollment(courseId)

        val registration = Registration.create(member, course)

        registrationRepository.save(registration)

        return RegistrationResponse.from(course)
    }

    @Transactional
    fun deleteRegisteredCourse(
        memberId: Long,
        courseId: Long,
    ) {
        val registration = registrationRepository.findByMemberIdAndCourseId(memberId, courseId)
            ?: throw RestApiException(REGISTERED_COURSE_NOT_FOUND)

        decreaseEnrollment(courseId)

        registrationRepository.delete(registration)
    }

    private fun validateCreditLimit(
        registrations: List<Registration>,
        member: Member,
        course: Course,
    ) {
        if (!CourseValidator.validateCreditLimit(registrations, member, course)) {
            throw RestApiException(CREDIT_LIMIT_EXCEEDED)
        }
    }

    private fun validateCourseActive(course: Course) {
        if (!course.isActive()) {
            throw RestApiException(COURSE_CLOSED)
        }
    }

    private fun increaseEnrollment(courseId: Long) {
        val affectedRows = courseRepository.increaseEnrollmentWithinCapacity(courseId)

        if (affectedRows == NO_AFFECTED_ROW) {
            throw RestApiException(COURSE_MAX_CAPACITY_EXCEEDED)
        }
    }

    private fun decreaseEnrollment(courseId: Long) {
        val affectedRows = courseRepository.decreaseEnrollmentAboveZero(courseId)

        if (affectedRows == NO_AFFECTED_ROW) {
            throw RestApiException(REGISTRATION_CANCEL_CONFLICT)
        }
    }

    private fun validateDuplicateCourse(
        registrations: List<Registration>,
        courseId: Long,
    ) {
        val exists = registrations.any { it.course.id == courseId }

        if (exists) {
            throw RestApiException(COURSE_ALREADY_REGISTERED)
        }
    }

    private fun validateDuplicateSubject(
        registrations: List<Registration>,
        course: Course,
    ) {
        val exists = registrations.any { it.course.titleKr == course.titleKr }

        if (exists) {
            throw RestApiException(DUPLICATE_SUBJECT_REGISTERED)
        }
    }

    private fun validateCourseScheduleConflict(
        registrations: List<Registration>,
        course: Course,
    ) {
        val registeredCourses = registrations.map { it.course }

        if (!CourseValidator.validateCourseScheduleNotConflict(registeredCourses, course)) {
            throw RestApiException(COURSE_SCHEDULE_CONFLICT)
        }
    }

    private fun validateCourseTypeLimit(
        registrations: List<Registration>,
        course: Course,
    ) {
        val registeredCourses = registrations.map { it.course }

        if (!CourseValidator.validateCourseTypeLimit(registeredCourses, course)) {
            throw RestApiException(COURSE_TYPE_LIMIT_EXCEEDED)
        }
    }

    companion object {
        private const val NO_AFFECTED_ROW = 0
    }
}
